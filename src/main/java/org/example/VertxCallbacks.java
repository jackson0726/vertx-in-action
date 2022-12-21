package org.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.example.rx.ReactiveX;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class VertxCallbacks extends AbstractVerticle {

    private final Logger logger = LoggerFactory.getLogger(ReactiveX.class);

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new VertxCallbacks());
    }

    @Override
    public void start(Promise<Void> startPromise) {
        Promise<String> promise = Promise.promise();
        vertx.setTimer(5000, id -> {
            if (System.currentTimeMillis() % 2L == 0L) {
                promise.complete("Ok!");
            } else {
                promise.fail(new RuntimeException("Bad luck..."));
            }
        });

        Future<String> future = promise.future();
        future.onSuccess(logger::info)
                .onFailure(err -> logger.error(err.getMessage()));

        promise.future()
                .recover(err -> Future.succeededFuture("Let's say it's ok!"))
                .map(String::toUpperCase)
                .flatMap(str -> {
                    Promise<String> next = Promise.promise();
                    vertx.setTimer(3000, id -> next.complete(">>> " + str));
                    return next.future();
                }).onSuccess(logger::info);

        CompletionStage<String> cs = promise.future().toCompletionStage();
        cs.thenApply(String::toUpperCase) .thenApply(str -> "~~~ " + str) .whenComplete((str, err) -> {
            if (err == null) {
                logger.info(str);
            } else {
                logger.error("Oh... " + err.getMessage());
            }
        });

        CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "5 seconds have elapsed";
        });

        Future.fromCompletionStage(cf, vertx.getOrCreateContext())
                .onSuccess(logger::info)
                .onFailure(Throwable::printStackTrace);

    }
}
