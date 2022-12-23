package org.example.stream;

import io.vertx.core.Vertx;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;

public class ReadingFileUsingVertxStreams {

    public static void nioReadFile(String filePath) {
        Vertx vertx = Vertx.vertx();
        OpenOptions options = new OpenOptions().setRead(true);
        vertx.fileSystem().open(filePath, options, ar -> {
            if (ar.succeeded()) {
                AsyncFile asyncFile = ar.result();
                asyncFile.handler(System.out::println)
                        .exceptionHandler(Throwable::printStackTrace)
                        .endHandler(done -> {
                            System.out.println("\n-- DONE");
                            vertx.close();
                        });
            } else {
                ar.cause().printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        var path = "build.gradle.kts";
        nioReadFile(path);
    }

}
