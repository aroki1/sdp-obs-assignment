package citypulse.city;

import java.util.List;

public record Building(String id, List<CityNode> devices) implements CityNode {

    public Building {
        devices = List.copyOf(devices);
    }

    @Override
    public <R> R accept(CityVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
