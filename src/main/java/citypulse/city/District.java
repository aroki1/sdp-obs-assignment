package citypulse.city;

import java.util.List;

public record District(String id, List<CityNode> children) implements CityNode {

    public District {
        children = List.copyOf(children);
    }

    @Override
    public <R> R accept(CityVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
