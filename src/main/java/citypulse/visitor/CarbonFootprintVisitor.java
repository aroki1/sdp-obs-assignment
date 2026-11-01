package citypulse.visitor;

import citypulse.city.AirMonitor;
import citypulse.city.Building;
import citypulse.city.City;
import citypulse.city.CityNode;
import citypulse.city.CityVisitor;
import citypulse.city.District;
import citypulse.city.StreetLight;
import citypulse.city.TrafficSignal;
import citypulse.city.WaterPump;

import java.util.List;

public final class CarbonFootprintVisitor implements CityVisitor<Double> {
    private static final double LIGHT_HOURS_PER_DAY = 12;
    private static final double ALWAYS_ON_HOURS = 24;
    private static final double PUMP_HOURS_PER_DAY = 8;

    private final double kgCo2PerKwh;

    public CarbonFootprintVisitor(double kgCo2PerKwh) {
        this.kgCo2PerKwh = kgCo2PerKwh;
    }

    @Override
    public Double visit(City city) {
        return sum(city.districts());
    }

    @Override
    public Double visit(District district) {
        return sum(district.children());
    }

    @Override
    public Double visit(Building building) {
        return sum(building.devices());
    }

    @Override
    public Double visit(StreetLight light) {
        return dailyKg(light.watts(), LIGHT_HOURS_PER_DAY);
    }

    @Override
    public Double visit(TrafficSignal signal) {
        return dailyKg(signal.watts(), ALWAYS_ON_HOURS);
    }

    @Override
    public Double visit(AirMonitor monitor) {
        return dailyKg(monitor.watts(), ALWAYS_ON_HOURS);
    }

    @Override
    public Double visit(WaterPump pump) {
        return dailyKg(pump.watts(), PUMP_HOURS_PER_DAY);
    }

    private double sum(List<? extends CityNode> nodes) {
        return nodes.stream().mapToDouble(node -> node.accept(this)).sum();
    }

    private double dailyKg(double watts, double hoursPerDay) {
        return watts * hoursPerDay / 1000 * kgCo2PerKwh;
    }
}
