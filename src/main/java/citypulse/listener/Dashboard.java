package citypulse.listener;

import citypulse.event.SensorReading;
import citypulse.event.SensorType;
import citypulse.observer.Observer;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class Dashboard implements Observer<SensorReading> {
    private final Map<SensorKey, Double> latestValues = new ConcurrentHashMap<>();

    @Override
    public void onEvent(SensorReading reading) {
        latestValues.put(new SensorKey(reading.districtId(), reading.type()), reading.value());
    }

    public Optional<Double> latest(String districtId, SensorType type) {
        return Optional.ofNullable(latestValues.get(new SensorKey(districtId, type)));
    }
}
