package org.example.eventbus.rpc;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.serviceproxy.ServiceBinder;

public class DataVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        new ServiceBinder(vertx)
                .setAddress("sensor.data-service")
                .register(SensorDataService.class, SensorDataService.create(vertx));
        super.start(startPromise);
    }

}
