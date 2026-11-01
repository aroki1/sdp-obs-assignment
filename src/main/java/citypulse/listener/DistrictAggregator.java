package citypulse.listener;

import citypulse.event.DistrictSummary;
import citypulse.event.SensorReading;
import citypulse.event.SensorType;
import citypulse.observer.EventChannel;
import citypulse.observer.Observer;
import citypulse.observer.Subject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DistrictAggregator implements Observer<SensorReading>, Subject<SensorType, DistrictSummary> {
    private final int windowSize;
    private final EventChannel<SensorType, DistrictSummary> summaries;
    private final Map<SensorKey, Window> windows = new ConcurrentHashMap<>();

    public DistrictAggregator(int windowSize, EventChannel<SensorType, DistrictSummary> summaries) {
        this.windowSize = windowSize;
        this.summaries = summaries;
    }

    @Override
    public void onEvent(SensorReading reading) {
        SensorKey key = new SensorKey(reading.districtId(), reading.type());
        Window[] completed = new Window[1];
        windows.compute(key, (k, window) -> {
            Window next = (window == null ? Window.EMPTY : window).add(reading.value());
            if (next.count() < windowSize) {
                return next;
            }
            completed[0] = next;
            return null;
        });
        if (completed[0] != null) {
            summaries.publish(new DistrictSummary(key.type(), key.districtId(), completed[0].average(), windowSize));
        }
    }

    @Override
    public void subscribe(SensorType type, Observer<? super DistrictSummary> observer) {
        summaries.subscribe(type, observer);
    }

    @Override
    public void unsubscribe(SensorType type, Observer<? super DistrictSummary> observer) {
        summaries.unsubscribe(type, observer);
    }

    private record Window(int count, double sum) {
        static final Window EMPTY = new Window(0, 0);

        Window add(double value) {
            return new Window(count + 1, sum + value);
        }

        double average() {
            return sum / count;
        }
    }
}
