package citypulse.city;

public record StreetLight(String id, double watts, int health, int lumens) implements CityNode {

    @Override
    public <R> R accept(CityVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
