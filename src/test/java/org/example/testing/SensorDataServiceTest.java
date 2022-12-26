package org.example.testing;

import io.vertx.core.Vertx;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.example.eventbus.rpc.DataVerticle;
import org.example.eventbus.rpc.SensorDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.withinPercentage;

@ExtendWith(VertxExtension.class)
class SensorDataServiceTest {

    private SensorDataService dataService;

    @BeforeEach
    void prepare(Vertx vertx, VertxTestContext context) {
        vertx.deployVerticle(new DataVerticle(), context.succeeding(id -> {
            dataService = SensorDataService.createProxy(vertx, "sensor.data-service");
            context.completeNow();
        }));
    }

    /**
     * This test case assumes that no sensor has been deployed.
     * So trying to get any sensor value must fail.
     * We check this behavior by looking for the temperature value of sensor abc, which does not exist.
     * We then check that the average value is 0.
     * When all declared checkpoints have been flagged, the test completes successfully.
     */
    @Test
    void noSensor(VertxTestContext context) {
        Checkpoint failsToGet = context.checkpoint();
        Checkpoint zeroAvg = context.checkpoint();

        dataService.valueFor("abc", context.failing(err -> context.verify(() -> {
            assertThat(err.getMessage()).startsWith("No value has been observed for");
            failsToGet.flag();
        })));

        dataService.average(context.succeeding(data -> context.verify(() -> {
            double avg = data.getDouble("average");
            assertThat(avg).isCloseTo(0.0d, withinPercentage(1.0d));
            zeroAvg.flag();
        })));
    }

}
