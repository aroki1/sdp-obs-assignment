package citypulse.city;

public record AirMonitor(String id, double watts, int health, String pollutant) implements CityNode {

    @Override
    public <R> R accept(CityVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
