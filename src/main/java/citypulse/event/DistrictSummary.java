package citypulse.event;

import citypulse.observer.Event;

public record DistrictSummary(SensorType type, String districtId, double average, int samples)
        implements Event<SensorType> {
}
