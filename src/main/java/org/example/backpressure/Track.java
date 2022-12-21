package org.example.backpressure;


import io.vertx.core.file.AsyncFile;

public class Track {
    public final String name;
    public AsyncFile file;
    public boolean endBuffer;
    public long position;

    public Track(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Track{" +
                "name='" + name + '\'' +
                ", endStream=" + endBuffer +
                '}';
    }

    public void end() {
        position = 0;
        endBuffer = true;
        close();
    }

    public void close() {
        if (file != null) file.close();
    }

    public void resetStream(AsyncFile file) {
        this.file = file;
        this.position = 0;
        this.endBuffer = false;
    }

    public void updatePosition(int length) {
        position += length;
        if (length == 0) {
            end();
        }
    }
}
