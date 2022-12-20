package org.example.backpressure;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * the main music-streaming logic and HTTP server interface for music players to connect to.
 */
public class Jukebox extends AbstractVerticle {

    private final Logger logger = LoggerFactory.getLogger(Jukebox.class);

    private enum State {PLAYING, PAUSED}

    private State currentMode = State.PAUSED;

    private final Queue<String> playlist = new ArrayDeque<>();

    private final Set<HttpServerResponse> streamers = new HashSet<>();

    @Override
    public void start(Promise<Void> startPromise) {
        EventBus eventBus = vertx.eventBus();
        eventBus.consumer("jukebox.list", this::list);
        eventBus.consumer("jukebox.schedule", this::schedule);
        eventBus.consumer("jukebox.pause", this::pause);
        eventBus.consumer("jukebox.play", this::play);

        var router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.get("/audio-stream").handler(this::openAudioStream);
        router.get("/download/:file_name").handler(this::downloadFile);
        router.get("/playlist").handler(this::playlist);

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080)
                .onComplete(res -> {
                    if (res.succeeded()) {
                        logger.info("Open http://localhost:8080/");
                        startPromise.complete();
                    } else {
                        logger.error("Start server failed", res.cause());
                        startPromise.fail(res.cause());
                    }
                });
    }

    private void playlist(RoutingContext context) {
        HttpServerRequest request = context.request();
        playlist().future().onComplete(ar -> {
            if (ar.succeeded()) {
                request.response()
                        .setStatusCode(200)
                        .end(Buffer.buffer(ar.result().toString()));
            } else {
                request.response()
                        .setStatusCode(500)
                        .end(ar.cause().getMessage());
            }
        });
    }

    private void downloadFile(RoutingContext context) {
        HttpServerRequest request = context.request();
        String filePath = request.getParam("file_name");
        String file = "tracks/" + filePath;

        if (!vertx.fileSystem().existsBlocking(file)) {
            request.response().setStatusCode(404).end();
            return;
        }

        OpenOptions options = new OpenOptions().setRead(true);
        vertx.fileSystem().open(file, options, ar -> {
            if (ar.succeeded()) {
                downloadFile(request, ar.result());
            } else {
                logger.error("Read file failed", ar.cause());
                request.response().setStatusCode(500).end();
            }
        });
    }

    private void downloadFile(HttpServerRequest request, AsyncFile file) {
        HttpServerResponse response = request.response()
                .setStatusCode(200)
                .putHeader("Content-Type", "audio/mpeg")
                .setChunked(true);

        file.handler(buffer -> {
            response.write(buffer);
            if (response.writeQueueFull()) {
                logger.warn("writeQueueFull --> pause the read stream.");
                file.pause();
                response.drainHandler(v -> {
                    logger.warn("resume the read stream.");
                    file.resume();
                });
            }
        });

        file.endHandler(v -> response.end());

        //file.pipeTo(response);
    }

    private void openAudioStream(RoutingContext context) {
        HttpServerRequest request = context.request();
        HttpServerResponse response = request.response()
                .putHeader("Content-Type", "audio/mpeg")
                .setChunked(true);
        streamers.add(response);
        response.endHandler(v -> {
            streamers.remove(response);
            logger.info("A streamer left");
        });
    }

    private Promise<JsonObject> playlist() {
        Promise<JsonObject> promise = Promise.promise();
        vertx.fileSystem().readDir("tracks", ".*mp3$", ar -> {
            if (ar.succeeded()) {
                List<String> files = ar.result().stream()
                        .map(File::new)
                        .map(File::getName)
                        .collect(Collectors.toList());
                var response = new JsonObject().put("files", new JsonArray(files));
                promise.complete(response);
            } else {
                logger.error("readDir tracks failed", ar.cause());
                promise.fail(ar.cause());
            }
        });
        return promise;
    }

    private void list(Message<?> message) {
        playlist().future().onComplete(ar -> {
            if (ar.succeeded()) {
                message.reply(ar.result());
            } else {
                message.fail(500, ar.cause().getMessage());
            }
        });
    }

    private void schedule(Message<JsonObject> request) {
        String file = request.body().getString("file");
        if (playlist.isEmpty() && currentMode == State.PAUSED) {
            play(request);
        }
        playlist.offer(file);
    }

    private void pause(Message<?> message) {
        currentMode = State.PAUSED;
    }

    private void play(Message<?> message) {
        currentMode = State.PLAYING;
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new Jukebox());
    }

}
