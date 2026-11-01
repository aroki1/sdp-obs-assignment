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

public final class InspectionVisitor implements CityVisitor<List<CityNode>> {
    private final int minHealth;

    public InspectionVisitor(int minHealth) {
        this.minHealth = minHealth;
    }

    @Override
    public List<CityNode> visit(City city) {
        return collect(city.districts());
    }

    @Override
    public List<CityNode> visit(District district) {
        return collect(district.children());
    }

    @Override
    public List<CityNode> visit(Building building) {
        return collect(building.devices());
    }

    @Override
    public List<CityNode> visit(StreetLight light) {
        return inspect(light, light.health());
    }

    @Override
    public List<CityNode> visit(TrafficSignal signal) {
        return inspect(signal, signal.health());
    }

    @Override
    public List<CityNode> visit(AirMonitor monitor) {
        return inspect(monitor, monitor.health());
    }

    @Override
    public List<CityNode> visit(WaterPump pump) {
        return inspect(pump, pump.health());
    }

    @Override
    public List<CityNode> visit(ChargingStation station) {
        return inspect(station, station.health());
    }

    private List<CityNode> collect(List<? extends CityNode> nodes) {
        return nodes.stream().flatMap(node -> node.accept(this).stream()).toList();
    }

    private List<CityNode> inspect(CityNode device, int health) {
        return health < minHealth ? List.of(device) : List.of();
    }
}
