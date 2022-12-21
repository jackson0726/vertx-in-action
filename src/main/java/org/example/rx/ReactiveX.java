package org.example.rx;

import io.reactivex.rxjava3.core.Observable;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 *     Type                      Description                                  Example
 *
 *  Observable<T>    A stream of events of type T. Does       Timer events, observable source where we
 *                   not support back-pressure.               cannot apply back-pressure like GUI events
 *
 *  Flowable<T>      A stream of events of type T where       Network data, filesystem inputs
 *                   back-pressure can be applied
 *
 *  Single<T>        A source that emits exactly one          Fetching an entry from a data store by key
 *                   event of type T
 *
 *  Maybe<T>         A source that may emit one event of      Fetching an entry from a data store by key,
 *                   type T, or none                          but the key may not exist
 *
 *  Completable      A source that notifies of some           Deleting files
 *                   action having completed, but no
 *                   value is being given
 *
 */
public class ReactiveX extends AbstractVerticle {

    private final Logger logger = LoggerFactory.getLogger(ReactiveX.class);

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new ReactiveX());
    }

    @Override
    public void start(Promise<Void> startPromise) {
        Observable.just(1, 2, 3) .map(Object::toString)
                .map(s -> "@" + s)
                .subscribe(logger::info);

        Observable.<String>error(() -> new RuntimeException("Woops"))
                .map(String::toUpperCase)
                .subscribe(logger::info, Throwable::printStackTrace);

        // Dealing with all life-cycle events in RxJava

        //Actions can be inserted, such as when a subscription happens.
        Observable.just("--", "this", "is", "--", "a", "sequence", "of", "items", "!")
                .doOnSubscribe(d -> logger.info("Subscribed!"))
                .delay(5, TimeUnit.SECONDS) //This delays emitting events by five seconds.
                .filter(s -> !s.startsWith("--")) //Another action, here called for each item flowing in the stream
                .doOnNext(logger::info)
                .map(String::toUpperCase)
                .buffer(2) //This groups events 2 by 2.
                .subscribe(
                        str -> logger.info(str.toString()),
                        Throwable::printStackTrace,
                        () -> logger.info(">>> Done"));
    }
}
