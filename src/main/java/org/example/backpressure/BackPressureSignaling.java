package org.example.backpressure;

/**
 * ReadStream back-pressure management methods:
 * pause(): Pauses the stream, preventing further data from being sent to the handler.
 * resume(): Starts reading data again and sending it to the handler.
 * fetch(n): Demands a number, n, of elements to be read (at most). The stream must be paused before calling fetch(n).
 *
 * WriteStream back-pressure management methods:
 * setWriteQueueMaxSize(int): Defines what the maximum write buffer queue size should be before being considered full.
 *                            This is a size in terms of queued Vert.x buffers to be written, not a size in terms of actual bytes, because the queued buffers may be of different sizes.
 * boolean writeQueueFull(): Indicates when the write buffer queue size is full.
 * drainHandler(Handler<Void>): Defines a callback indicating when the write buffer queue has been drained (typically when it is back to half of its maximum size).
 */
public class BackPressureSignaling {

    public static void main(String[] args) {

    }

}
