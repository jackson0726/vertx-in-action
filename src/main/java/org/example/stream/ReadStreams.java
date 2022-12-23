package org.example.stream;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.parsetools.RecordParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadStreams extends AbstractVerticle {

    private final Logger logger = LoggerFactory.getLogger(ReadStreams.class);

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new ReadStreams());
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        AsyncFile file = vertx.fileSystem()
                .openBlocking("sample.txt", new OpenOptions().setRead(true));

        RecordParser parser = RecordParser.newFixed(4, file);
        parser.handler(header -> readMagicNumber(header, parser));
    }

    private void readMagicNumber(Buffer header, RecordParser parser) {
        logger.info("Magic number: {}:{}:{}:{}",
                header.getByte(0),
                header.getByte(1),
                header.getByte(2),
                header.getByte(3)
        );
        parser.handler(version -> readVersion(version, parser));
    }

    private void readVersion(Buffer header, RecordParser parser) {
        logger.info("Version: {}", header.getInt(0));
        parser.delimitedMode("\n");
        parser.handler(name -> readName(name, parser));
    }

    private void readName(Buffer name, RecordParser parser) {
        logger.info("Name: {}", name.toString());
        parser.fixedSizeMode(4);
        parser.handler(keyLength -> readKey(keyLength, parser));
    }

    private void readKey(Buffer keyLength, RecordParser parser) {
        parser.fixedSizeMode(keyLength.getInt(0));
        parser.handler(key -> readValue(key.toString(), parser));
    }

    private void readValue(String key, RecordParser parser) {
        parser.fixedSizeMode(4);
        parser.handler(valueLength -> finishEntry(key, valueLength, parser));
    }

    private void finishEntry(String key, Buffer valueLength, RecordParser parser) {
        parser.fixedSizeMode(valueLength.getInt(0));
        parser.handler(value -> {
            logger.info("Key: {} / Value: {}", key, value);
            parser.fixedSizeMode(4);
            parser.handler(keyLength -> readKey(keyLength, parser));
        });
    }
}
