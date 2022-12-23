package org.example.eventbus.sensor;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.UUID;

public class HeatSensor extends AbstractVerticle {

    private final Logger logger = LoggerFactory.getLogger(HeatSensor.class);

    private final Random random = new Random();
    private final String sensorId = UUID.randomUUID().toString();
    private double temperature = 28.0;

    public static void main(String[] args) {
//        eventBus();
        webClient();
    }

    public static void eventBus() {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(HeatSensor.class.getName(), new DeploymentOptions().setInstances(4));
        vertx.deployVerticle(Listener.class.getName());
        vertx.deployVerticle(SensorData.class.getName());
        vertx.deployVerticle(HttpServer.class.getName());
    }

    public static void webClient() {
        Vertx vertx = Vertx.vertx();

        vertx.deployVerticle(HeatSensor.class.getName(),
                new DeploymentOptions().setConfig(new JsonObject()
                        .put("http.port", 3001)));

        vertx.deployVerticle(HeatSensor.class.getName(),
                new DeploymentOptions().setConfig(new JsonObject()
                .put("http.port", 3002)));

        vertx.deployVerticle(HeatSensor.class.getName(),
                new DeploymentOptions().setConfig(new JsonObject()
                        .put("http.port", 3003)));

        vertx.deployVerticle(SnapshotService.class.getName());

//        vertx.deployVerticle(CollectorService.class.getName());
        vertx.deployVerticle(RxCollectorService.class.getName());

    }

    @Override
    public void start() {
        var port = config().getInteger("http.port", 3000);
        vertx.createHttpServer()
                .requestHandler(this::handleRequest)
                .listen(port).onComplete(ar -> {
                    if (ar.succeeded()) {
                        logger.info("Started server on port {}", port);
                    } else {
                        logger.info("Start server failed", ar.cause());
                    }
        });
        scheduleNextUpdate();
    }

    private void scheduleNextUpdate() {
        vertx.setTimer(random.nextInt(5000) + 1000L, this::update);
    }

    private void update(long timerId) {
        temperature = temperature + (delta() / 10);
        JsonObject payload = new JsonObject()
                .put("id", sensorId)
                .put("temp", temperature);
        vertx.eventBus().publish("sensor.updates", payload);
        scheduleNextUpdate();
    }

    private double delta() {
        if (random.nextInt() > 0) {
            return random.nextGaussian();
        } else {
            return -random.nextGaussian();
        }
    }

    private void handleRequest(HttpServerRequest req) {
        JsonObject data = new JsonObject()
                .put("id", sensorId)
                .put("temp", temperature);
        req.response()
                .putHeader("Content-Type", "application/json")
                .end(data.encode());
    }

}
