package org.example.backpressure;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetSocket;
import io.vertx.core.parsetools.RecordParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * a text-based TCP protocol for remotely controlling the jukebox application.
 */
public class NetControl extends AbstractVerticle {

    private final Logger logger = LoggerFactory.getLogger(NetControl.class);

    @Override
    public void start(Promise<Void> startPromise) {
        vertx.createNetServer()
                .connectHandler(this::handleClient)
                .listen(3001)
                .onComplete(res -> {
                    if (res.succeeded()) {
                        logger.info("Open NetControl http://localhost:3001/");
                        startPromise.complete();
                    } else {
                        logger.error("Start server failed", res.cause());
                        startPromise.fail(res.cause());
                    }
                });
    }

    private void handleClient(NetSocket netSocket) {
        RecordParser.newDelimited("\n", netSocket) //Parse by looking for new lines.
                .handler(buffer -> handleBuffer(netSocket, buffer)) //buffers are lines.
                .endHandler(v -> logger.info("Connection ended."));
    }

    private void handleBuffer(NetSocket netSocket, Buffer buffer) {
        String command = buffer.toString(); //Buffer-to-string decoding with the default charset
        logger.info("Command: {}", command);
        switch (command) {
            case "/list":
                listCommand(netSocket);
                break;
            case "/play":
                playCommand();
                break;
            case "/pause":
                pauseCommand();
                break;
            default:
                if (command.startsWith("/schedule ")) {
                    scheduleCommand(command.trim());
                } else {
                    if (!command.equals("\n") && !command.isBlank()) {
                        netSocket.write("Unknown command\n");
                    }
                }
        }
    }

    private void scheduleCommand(String command) {
        String file = command.split(" ")[1];
        var request = new JsonObject().put("file", file);
        vertx.eventBus().send(Jukebox.buildCmdEvent.apply(Jukebox.SCHEDULE_CMD), request);
    }

    private void pauseCommand() {
        vertx.eventBus().send(Jukebox.buildCmdEvent.apply(Jukebox.PAUSE_CMD), "");
    }

    private void playCommand() {
        vertx.eventBus().send(Jukebox.buildCmdEvent.apply(Jukebox.PLAY_CMD), "");
    }

    private void listCommand(NetSocket netSocket) {
        vertx.eventBus().<JsonObject>request(Jukebox.buildCmdEvent.apply(Jukebox.LIST_CMD), "", ar -> {
            if (ar.succeeded()) {
                JsonObject data = ar.result().body();
                data.getJsonArray("files").stream()
                        .forEach(fileName -> netSocket.write(fileName + "\n"));
            } else {
                logger.error(Jukebox.LIST_CMD + " command error", ar.cause());
            }
        });
    }

}
