package citypulse.simulation;

import citypulse.event.DistrictSummary;
import citypulse.event.SensorReading;
import citypulse.event.SensorType;
import citypulse.listener.DistrictAggregator;
import citypulse.listener.StatisticsCollector;
import citypulse.observer.EventChannel;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CitySimulationTest {
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    @Test
    void fourThreadsTimesThousandEventsAreAllDelivered() throws InterruptedException {
        EventChannel<SensorType, SensorReading> feed = new EventChannel<>();
        EventChannel<SensorType, DistrictSummary> summaries = new EventChannel<>();
        StatisticsCollector statistics = new StatisticsCollector();
        DistrictAggregator aggregator = new DistrictAggregator(10, summaries);
        AtomicInteger summaryCount = new AtomicInteger();
        feed.subscribe(SensorType.PM25, statistics);
        feed.subscribe(SensorType.PM25, aggregator);
        aggregator.subscribe(SensorType.PM25, summary -> summaryCount.incrementAndGet());
        List<SensorProducer> producers = IntStream.range(0, 4)
                .mapToObj(seed -> new SensorProducer(feed, List.of(SensorType.PM25), List.of("north"), 1_000,
                        new Random(seed)))
                .toList();
        ExecutorService executor = Executors.newFixedThreadPool(4);

        new CitySimulation(executor, TIMEOUT).run(producers);

        assertEquals(4_000, statistics.totalCount());
        assertEquals(400, summaryCount.get());
        assertTrue(executor.isTerminated());
    }

    @Test
    void producerFailureIsReportedAndExecutorStillShutsDown() {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        Runnable failing = () -> {
            throw new IllegalArgumentException("sensor offline");
        };

        assertThrows(IllegalStateException.class,
                () -> new CitySimulation(executor, TIMEOUT).run(List.of(failing, () -> { }, () -> { })));
        assertTrue(executor.isShutdown());
    }
}
