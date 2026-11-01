package citypulse.city;

import java.util.List;

public record City(String id, List<District> districts) implements CityNode {

    public City {
        districts = List.copyOf(districts);
    }

    @Override
    public <R> R accept(CityVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
