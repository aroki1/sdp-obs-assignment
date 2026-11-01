package citypulse.listener;

import citypulse.event.SensorReading;
import citypulse.event.SensorType;
import citypulse.observer.Observer;
import citypulse.observer.Subject;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;

public final class CalibrationProbe implements Observer<SensorReading> {
    private final Subject<SensorType, SensorReading> feed;
    private final SensorType type;
    private final int sampleSize;
    private final AtomicInteger claimed = new AtomicInteger();
    private final AtomicInteger recorded = new AtomicInteger();
    private final DoubleAdder sum = new DoubleAdder();

    public CalibrationProbe(Subject<SensorType, SensorReading> feed, SensorType type, int sampleSize) {
        this.feed = feed;
        this.type = type;
        this.sampleSize = sampleSize;
    }

    @Override
    public void onEvent(SensorReading reading) {
        int sample = claimed.incrementAndGet();
        if (sample > sampleSize) {
            return;
        }
        sum.add(reading.value());
        recorded.incrementAndGet();
        if (sample == sampleSize) {
            feed.unsubscribe(type, this);
        }
    }

    public boolean isCalibrated() {
        return recorded.get() == sampleSize;
    }

    public double baseline() {
        if (!isCalibrated()) {
            throw new IllegalStateException("Calibration of " + type + " is not finished");
        }
        return sum.sum() / sampleSize;
    }
}
