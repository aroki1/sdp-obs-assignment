package citypulse.city;

public interface CityNode {
    String id();

    <R> R accept(CityVisitor<R> visitor);
}
