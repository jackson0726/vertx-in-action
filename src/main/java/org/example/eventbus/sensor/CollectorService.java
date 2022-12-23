package org.example.eventbus.sensor;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.ext.web.codec.BodyCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CollectorService extends AbstractVerticle {

    private final Logger logger = LoggerFactory.getLogger(CollectorService.class);

    private WebClient webClient;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        webClient = WebClient.create(vertx);
        vertx.createHttpServer()
                .requestHandler(this::handleRequestByFuture)
                .listen(8082);
    }

    private Future<JsonObject> fetchTemperature(int port) {
        return webClient
                .get(port, "localhost", "/")
                .expect(ResponsePredicate.SC_SUCCESS)
                .as(BodyCodec.jsonObject())
                .send()
                .map(HttpResponse::body);
    }

    private void handleRequestByFuture(HttpServerRequest request) {
        List<Future> futureList = new ArrayList<>(3);
        for (int i = 1; i <= 3; i++) {
            int port = 3000 + i;
            logger.info("Port: {}", port);
            futureList.add(fetchTemperature(port));
        }
        CompositeFuture.all(futureList)
                .flatMap(this::sendToSnapshot)
                .onSuccess(data -> sendResponse(request, data))
                .onFailure(err -> {
                    logger.error("Something went wrong", err);
                    request.response().setStatusCode(500).end();
                });
    }

    private Future<JsonObject> sendToSnapshot(CompositeFuture temps) {
        List<JsonObject> tempData = temps.list();
        JsonObject data = new JsonObject()
                .put("data", new JsonArray()
                        .add(tempData.get(0))
                        .add(tempData.get(1))
                        .add(tempData.get(2))
                );
        return webClient
                .post(4000, "localhost", "/")
                .expect(ResponsePredicate.SC_SUCCESS)
                .sendJson(data)
                .map(response -> data);
    }

    private void handleRequest(HttpServerRequest request) {
        List<JsonObject> responses = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(0);
        for (int i = 1; i <= 3; i++) {
            int port = 3000 + i;
            logger.info("Port: {}", port);
            webClient
                    .get(port, "localhost", "/")
                    .expect(ResponsePredicate.SC_SUCCESS)
                    .as(BodyCodec.jsonObject())
                    .send(ar -> {
                        if (ar.succeeded()) {
                            responses.add(ar.result().body());
                        } else {
                            logger.error(port + " Sensor down?", ar.cause());
                        }
                        if (counter.incrementAndGet() == 3) {
                            JsonObject data = new JsonObject().put("data", new JsonArray(responses));
                            sendToSnapshot(request, data);
                        }
                    });
        }
    }

    private void sendToSnapshot(HttpServerRequest request, JsonObject data) {
        webClient.post(4000, "localhost", "/")
                .expect(ResponsePredicate.SC_SUCCESS)
                .sendJsonObject(data, ar -> {
                    if (ar.succeeded()) {
                        sendResponse(request, data);
                    } else {
                        logger.error("Snapshot down?", ar.cause());
                        request.response().setStatusCode(500).end();
                    }
                });
    }

    private void sendResponse(HttpServerRequest request, JsonObject data) {
        request.response()
                .putHeader("Content-Type", "application/json")
                .end(data.encode());
    }
}
