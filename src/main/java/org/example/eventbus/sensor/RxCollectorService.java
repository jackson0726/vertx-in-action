package org.example.eventbus.sensor;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.rxjava3.core.http.HttpServerRequest;
import io.vertx.rxjava3.ext.web.client.HttpResponse;
import io.vertx.rxjava3.ext.web.client.WebClient;
import io.vertx.rxjava3.ext.web.client.predicate.ResponsePredicate;
import io.vertx.rxjava3.ext.web.codec.BodyCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RxCollectorService extends AbstractVerticle {

    private final Logger logger = LoggerFactory.getLogger(RxCollectorService.class);

    private WebClient webClient;

    @Override
    public Completable rxStart() {
        webClient = WebClient.create(vertx);
        return vertx.createHttpServer()
                .requestHandler(this::handleRequest)
                .rxListen(8082)
                .ignoreElement();
    }

    private Single<HttpResponse<JsonObject>> fetchTemperature(int port) {
        return webClient
                .get(port, "localhost", "/")
                .expect(ResponsePredicate.SC_SUCCESS)
                .as(BodyCodec.jsonObject())
                .rxSend();
    }

    private Single<JsonObject> collectTemperatures() {
        Single<HttpResponse<JsonObject>> r1 = fetchTemperature(3001);
        Single<HttpResponse<JsonObject>> r2 = fetchTemperature(3002);
        Single<HttpResponse<JsonObject>> r3 = fetchTemperature(3003);

        return Single.zip(r1, r2, r3, (j1, j2, j3) -> {
            JsonArray array = new JsonArray()
                    .add(j1.body())
                    .add(j2.body())
                    .add(j3.body());
            return new JsonObject().put("data", array);
        });
    }

    private void handleRequest(HttpServerRequest request) {
        Single<JsonObject> data = collectTemperatures();
        sendToSnapshot(data).subscribe(json -> {
            request.response()
                    .putHeader("Content-Type", "application/json")
                    .end(json.encode());
        }, err -> {
            logger.error("Something went wrong", err);
            request.response().setStatusCode(500).end();
        });
    }

    private Single<JsonObject> sendToSnapshot(Single<JsonObject> data) {
        return data.flatMap(json -> webClient
                .post(4000, "localhost", "")
                .expect(ResponsePredicate.SC_SUCCESS)
                .rxSendJsonObject(json)
                .flatMap(resp -> Single.just(json)));
    }

}
