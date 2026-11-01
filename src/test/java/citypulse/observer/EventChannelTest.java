package citypulse.observer;

import citypulse.event.SensorReading;
import citypulse.event.SensorType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventChannelTest {
    private final EventChannel<SensorType, SensorReading> channel = new EventChannel<>();

    @Test
    void deliversOnlyEventsOfSubscribedType() {
        List<SensorReading> received = new ArrayList<>();
        channel.subscribe(SensorType.PM25, received::add);

        channel.publish(reading(SensorType.PM25));
        channel.publish(reading(SensorType.NOISE));

        assertEquals(List.of(reading(SensorType.PM25)), received);
    }

    @Test
    void unsubscribedObserverReceivesNothing() {
        AtomicInteger calls = new AtomicInteger();
        Observer<SensorReading> observer = event -> calls.incrementAndGet();
        channel.subscribe(SensorType.PM25, observer);

        channel.publish(reading(SensorType.PM25));
        channel.unsubscribe(SensorType.PM25, observer);
        channel.publish(reading(SensorType.PM25));

        assertEquals(1, calls.get());
    }

    @Test
    void observerSubscribedInsideHandlerReceivesOnlyLaterEvents() {
        AtomicInteger lateCalls = new AtomicInteger();
        Observer<SensorReading> late = event -> lateCalls.incrementAndGet();
        channel.subscribe(SensorType.PM25, event -> channel.subscribe(SensorType.PM25, late));

        channel.publish(reading(SensorType.PM25));
        channel.publish(reading(SensorType.PM25));

        assertEquals(1, lateCalls.get());
    }

    @Test
    void unsubscribingWhilePublishingFromManyThreadsIsSafe() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        List<Observer<SensorReading>> observers = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Observer<SensorReading> observer = event -> calls.incrementAndGet();
            observers.add(observer);
            channel.subscribe(SensorType.PM25, observer);
        }
        ExecutorService executor = Executors.newFixedThreadPool(4);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> publishers = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            publishers.add(executor.submit(() -> {
                start.await();
                for (int n = 0; n < 1_000; n++) {
                    channel.publish(reading(SensorType.PM25));
                }
                return null;
            }));
        }
        Future<?> remover = executor.submit(() -> {
            start.await();
            observers.forEach(observer -> channel.unsubscribe(SensorType.PM25, observer));
            return null;
        });

        start.countDown();
        for (Future<?> publisher : publishers) {
            publisher.get();
        }
        remover.get();
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        int before = calls.get();
        channel.publish(reading(SensorType.PM25));
        assertEquals(before, calls.get());
    }

    private static SensorReading reading(SensorType type) {
        return new SensorReading(type, "north", 10);
    }
}
