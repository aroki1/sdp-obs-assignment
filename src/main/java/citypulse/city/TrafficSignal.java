package citypulse.city;

public record TrafficSignal(String id, double watts, int health, int phases) implements CityNode {

    @Override
    public <R> R accept(CityVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
