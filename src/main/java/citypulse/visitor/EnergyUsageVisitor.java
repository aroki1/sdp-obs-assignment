package citypulse.visitor;

import citypulse.city.AirMonitor;
import citypulse.city.Building;
import citypulse.city.ChargingStation;
import citypulse.city.City;
import citypulse.city.CityNode;
import citypulse.city.CityVisitor;
import citypulse.city.District;
import citypulse.city.StreetLight;
import citypulse.city.TrafficSignal;
import citypulse.city.WaterPump;

import java.util.List;

public final class EnergyUsageVisitor implements CityVisitor<Double> {

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
        return light.watts();
    }

    @Override
    public Double visit(TrafficSignal signal) {
        return signal.watts();
    }

    @Override
    public Double visit(AirMonitor monitor) {
        return monitor.watts();
    }

    @Override
    public Double visit(WaterPump pump) {
        return pump.watts();
    }

    @Override
    public Double visit(ChargingStation station) {
        return station.watts();
    }

    private double sum(List<? extends CityNode> nodes) {
        return nodes.stream().mapToDouble(node -> node.accept(this)).sum();
    }
}
