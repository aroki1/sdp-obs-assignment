package citypulse.listener;

import citypulse.event.SensorReading;
import citypulse.event.SensorType;
import citypulse.observer.Observer;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class StatisticsCollector implements Observer<SensorReading> {
    private final Map<SensorType, Stats> statsByType = new ConcurrentHashMap<>();

    @Override
    public void onEvent(SensorReading reading) {
        statsByType.merge(reading.type(), Stats.of(reading.value()), Stats::combine);
    }

    public Optional<Stats> stats(SensorType type) {
        return Optional.ofNullable(statsByType.get(type));
    }

    public long totalCount() {
        return statsByType.values().stream().mapToLong(Stats::count).sum();
    }

    public record Stats(long count, double sum, double min, double max) {

        static Stats of(double value) {
            return new Stats(1, value, value, value);
        }

        Stats combine(Stats other) {
            return new Stats(count + other.count, sum + other.sum, Math.min(min, other.min), Math.max(max, other.max));
        }

        public double average() {
            return sum / count;
        }
    }
}
