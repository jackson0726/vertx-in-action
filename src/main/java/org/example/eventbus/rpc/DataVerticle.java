package org.example.eventbus.rpc;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.serviceproxy.ServiceBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataVerticle extends AbstractVerticle {

    private final Logger logger = LoggerFactory.getLogger(DataVerticle.class);

    @Override
    public void start(Promise<Void> startPromise) {
        new ServiceBinder(vertx)
                .setAddress("sensor.data-service")
                .register(SensorDataService.class, SensorDataService.create(vertx));

        SensorDataService service = SensorDataService.createProxy(vertx, "sensor.data-service");
        service.average(ar -> {
            if (ar.succeeded()) {
                logger.info("Average = " + ar.result());
            } else {
                ar.cause().printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new DataVerticle());
    }

}
