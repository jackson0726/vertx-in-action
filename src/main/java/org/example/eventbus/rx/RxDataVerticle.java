package org.example.eventbus.rx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.serviceproxy.ServiceBinder;
import org.example.rxjava3.eventbus.rpc.SensorDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class RxDataVerticle extends AbstractVerticle {

    private final Logger logger = LoggerFactory.getLogger(RxDataVerticle.class);

    @Override
    public void start(Promise<Void> startPromise) {
        new ServiceBinder(vertx)
                .setAddress("sensor.data-service")
                .register(org.example.eventbus.rpc.SensorDataService.class,
                        org.example.eventbus.rpc.SensorDataService.create(vertx));

        SensorDataService service = SensorDataService.createProxy(
                io.vertx.rxjava3.core.Vertx.newInstance(vertx), "sensor.data-service");

        service.rxAverage()
                .delaySubscription(3, TimeUnit.SECONDS)
                .repeat()
                .map(data -> "avg = " + data.getDouble("average"))
                .subscribe(avg -> logger.info("Average = {}", avg));

    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new RxDataVerticle());
    }

}
