package citypulse.simulation;

import citypulse.event.SensorReading;
import citypulse.event.SensorType;
import citypulse.observer.EventChannel;

import java.util.List;
import java.util.random.RandomGenerator;

public final class SensorProducer implements Runnable {
    private static final double MAX_VALUE = 100;

    private final EventChannel<SensorType, SensorReading> feed;
    private final List<SensorType> types;
    private final List<String> districtIds;
    private final int events;
    private final RandomGenerator random;

    public SensorProducer(EventChannel<SensorType, SensorReading> feed, List<SensorType> types,
                          List<String> districtIds, int events, RandomGenerator random) {
        this.feed = feed;
        this.types = List.copyOf(types);
        this.districtIds = List.copyOf(districtIds);
        this.events = events;
        this.random = random;
    }

    @Override
    public void run() {
        for (int i = 0; i < events; i++) {
            SensorType type = types.get(i % types.size());
            String districtId = districtIds.get(random.nextInt(districtIds.size()));
            feed.publish(new SensorReading(type, districtId, random.nextDouble(MAX_VALUE)));
        }
    }
}
