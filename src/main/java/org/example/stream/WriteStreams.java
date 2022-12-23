package org.example.stream;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WriteStreams extends AbstractVerticle {

    private final Logger logger = LoggerFactory.getLogger(WriteStreams.class);

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new WriteStreams());
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        AsyncFile file = vertx.fileSystem()
                .openBlocking("sample.txt", new OpenOptions().setWrite(true).setCreate(true));
        Buffer buffer = Buffer.buffer();

        buffer.appendBytes(new byte[]{1, 2, 3, 4}); //magic number
        buffer.appendInt(2); //version
        buffer.appendString("Sample database\n"); //database name

        String key = "abc";
        String value = "123456-abcdef";
        buffer.appendInt(key.length())
                .appendString(key)
                .appendInt(value.length())
                .appendString(value)
        ;

        key = "foo@bar";
        value = "Foo Bar Baz";
        buffer.appendInt(key.length())
                .appendString(key)
                .appendInt(value.length())
                .appendString(value);

        file.end(buffer, ar -> {
            if (ar.failed()) {
                logger.error("file failed", ar.cause());
            } else {
                logger.info("write success");
            }
            vertx.close();
        });
    }
}
