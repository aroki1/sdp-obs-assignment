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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Stateful: remembers the enclosing district while walking, so use one instance per traversal.
public final class MaintenanceCostVisitor implements CityVisitor<Void> {
    public static final String UNASSIGNED = "unassigned";
    private static final double LIGHT_RATE = 50;
    private static final double SIGNAL_RATE = 200;
    private static final double MONITOR_RATE = 150;
    private static final double PUMP_RATE = 400;

    private final Map<String, Double> costByDistrict = new HashMap<>();
    private String currentDistrict = UNASSIGNED;

    public Map<String, Double> costByDistrict() {
        return Map.copyOf(costByDistrict);
    }

    public double totalCost() {
        return costByDistrict.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    @Override
    public Void visit(City city) {
        return visitAll(city.districts());
    }

    @Override
    public Void visit(District district) {
        String enclosing = currentDistrict;
        currentDistrict = district.id();
        costByDistrict.putIfAbsent(currentDistrict, 0.0);
        visitAll(district.children());
        currentDistrict = enclosing;
        return null;
    }

    @Override
    public Void visit(Building building) {
        return visitAll(building.devices());
    }

    @Override
    public Void visit(StreetLight light) {
        return addCost(LIGHT_RATE, light.health());
    }

    @Override
    public Void visit(TrafficSignal signal) {
        return addCost(SIGNAL_RATE, signal.health());
    }

    @Override
    public Void visit(AirMonitor monitor) {
        return addCost(MONITOR_RATE, monitor.health());
    }

    @Override
    public Void visit(WaterPump pump) {
        return addCost(PUMP_RATE, pump.health());
    }

    private Void visitAll(List<? extends CityNode> nodes) {
        nodes.forEach(node -> node.accept(this));
        return null;
    }

    private Void addCost(double rate, int health) {
        costByDistrict.merge(currentDistrict, rate * (100 - health) / 100, Double::sum);
        return null;
    }
}
