package org.example.eventbus.sensor;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.Random;
import java.util.UUID;

public class HeatSensor extends AbstractVerticle {

    private final Random random = new Random();
    private final String sensorId = UUID.randomUUID().toString();
    private double temperature = 28.0;

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(HeatSensor.class.getName(), new DeploymentOptions().setInstances(4));
        vertx.deployVerticle(Listener.class.getName());
        vertx.deployVerticle(SensorData.class.getName());
        vertx.deployVerticle(HttpServer.class.getName());
    }

    @Override
    public void start() {
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
}
