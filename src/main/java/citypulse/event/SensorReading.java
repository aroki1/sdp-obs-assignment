package citypulse.event;

import citypulse.observer.Event;

public record SensorReading(SensorType type, String districtId, double value) implements Event<SensorType> {
}
