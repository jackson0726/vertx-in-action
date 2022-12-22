package org.example.cluster;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import org.example.eventbus.sensor.HeatSensor;
import org.example.eventbus.sensor.HttpServer;
import org.example.eventbus.sensor.Listener;
import org.example.eventbus.sensor.SensorData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecondInstance {

    private static final Logger logger = LoggerFactory.getLogger(SecondInstance.class);

    public static void main(String[] args) {
        Vertx.clusteredVertx(new VertxOptions(), ar -> {
        if (ar.succeeded()) {
            logger.info("Second instance has been started");
            Vertx vertx = ar.result();
            vertx.deployVerticle(HeatSensor.class.getName(), new DeploymentOptions().setInstances(4));
            vertx.deployVerticle(Listener.class.getName());
            vertx.deployVerticle(SensorData.class.getName());

            JsonObject conf = new JsonObject().put("port", 8082);
            vertx.deployVerticle(HttpServer.class.getName(), new DeploymentOptions().setConfig(conf));
        } else {
            logger.error("Could not start", ar.cause());
        } });
    }

}
