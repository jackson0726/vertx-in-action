package org.example.eventbus.rpc;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.stream.Collectors;

public class SensorDataServiceImpl implements SensorDataService {

    private final Logger logger = LoggerFactory.getLogger(SensorDataServiceImpl.class);

    private final HashMap<String, Double> lastValues = new HashMap<>();

    public SensorDataServiceImpl(Vertx vertx) {
        vertx.eventBus().<JsonObject>consumer("sensor.updates", message -> {
            JsonObject json = message.body();
            logger.info("updates: {}", json);
            lastValues.put(json.getString("id"), json.getDouble("temp"));
        });

        vertx.eventBus().<JsonObject>consumer("sensor.average", message -> {
            double avg = lastValues.values().stream()
                    .collect(Collectors.averagingDouble(Double::doubleValue));
            JsonObject json = new JsonObject().put("average", avg);
            message.reply(json);
        });
    }

    @Override
    public void valueFor(String sensorId, Handler<AsyncResult<JsonObject>> handler) {
        logger.info("Value for sensor id = '{}'", sensorId);
        if (lastValues.containsKey(sensorId)) {
            JsonObject data = new JsonObject()
                    .put("sensorId", sensorId)
                    .put("value", lastValues.get(sensorId));
            handler.handle(Future.succeededFuture(data));
        } else {
            handler.handle(Future.failedFuture("No value has been observed for " + sensorId));
        }
    }

    @Override
    public void average(Handler<AsyncResult<JsonObject>> handler) {
        double avg = lastValues.values().stream()
                .collect(Collectors.averagingDouble(Double::doubleValue));
        JsonObject data = new JsonObject().put("average", avg);
        handler.handle(Future.succeededFuture(data));
    }

}
