package org.example.rx;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.rxjava3.core.RxHelper;
import io.vertx.rxjava3.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;


public class VertxIntro extends AbstractVerticle {

    private final Logger logger = LoggerFactory.getLogger(VertxIntro.class);

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new VertxIntro());
    }

    @Override
    public Completable rxStart() {
        Observable
                .interval(1, TimeUnit.SECONDS, RxHelper.scheduler(vertx))
                .subscribe(n -> logger.info("tick"));

        return vertx.createHttpServer()
                .requestHandler(r -> r.response().end("Ok"))
                .rxListen(8082)
                .ignoreElement();
    }

}
