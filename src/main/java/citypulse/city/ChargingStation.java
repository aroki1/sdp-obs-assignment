package citypulse.city;

public record ChargingStation(String id, double watts, int health, int connectors) implements CityNode {

    @Override
    public <R> R accept(CityVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
