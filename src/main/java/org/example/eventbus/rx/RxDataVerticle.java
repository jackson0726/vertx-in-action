package org.example.eventbus.rx;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.rxjava3.core.AbstractVerticle;
import org.example.eventbus.rpc.DataVerticle;
import org.example.eventbus.sensor.HeatSensor;
import org.example.rxjava3.eventbus.rpc.SensorDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class RxDataVerticle extends AbstractVerticle {

    private final Logger logger = LoggerFactory.getLogger(RxDataVerticle.class);

    @Override
    public void start() {
        SensorDataService service = SensorDataService.createProxy(vertx, "sensor.data-service");
        service.rxAverage()
                .delaySubscription(3, TimeUnit.SECONDS)
                .repeat()
                .map(data -> "avg = " + data.getDouble("average"))
                .subscribe(avg -> logger.info("Average = {}", avg));
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(HeatSensor.class.getName(), new DeploymentOptions().setInstances(4));
        vertx.deployVerticle(new DataVerticle());
        vertx.deployVerticle(new RxDataVerticle());
    }

}
