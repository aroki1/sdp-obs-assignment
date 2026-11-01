package citypulse.city;

public record WaterPump(String id, double watts, int health, double litresPerMinute) implements CityNode {

    @Override
    public <R> R accept(CityVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
