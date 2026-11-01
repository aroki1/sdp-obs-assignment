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

import java.util.List;
import java.util.stream.Collectors;

public final class JsonExportVisitor implements CityVisitor<String> {

    @Override
    public String visit(City city) {
        return container("city", city.id(), city.districts());
    }

    @Override
    public String visit(District district) {
        return container("district", district.id(), district.children());
    }

    @Override
    public String visit(Building building) {
        return container("building", building.id(), building.devices());
    }

    @Override
    public String visit(StreetLight light) {
        return device("streetLight", light.id(), light.watts(), light.health(), field("lumens", light.lumens()));
    }

    @Override
    public String visit(TrafficSignal signal) {
        return device("trafficSignal", signal.id(), signal.watts(), signal.health(), field("phases", signal.phases()));
    }

    @Override
    public String visit(AirMonitor monitor) {
        return device("airMonitor", monitor.id(), monitor.watts(), monitor.health(),
                field("pollutant", quote(monitor.pollutant())));
    }

    @Override
    public String visit(WaterPump pump) {
        return device("waterPump", pump.id(), pump.watts(), pump.health(),
                field("litresPerMinute", pump.litresPerMinute()));
    }

    private String container(String type, String id, List<? extends CityNode> children) {
        String items = children.stream().map(child -> child.accept(this)).collect(Collectors.joining(","));
        return object(field("type", quote(type)), field("id", quote(id)), field("children", "[" + items + "]"));
    }

    private static String device(String type, String id, double watts, int health, String specificField) {
        return object(field("type", quote(type)), field("id", quote(id)),
                field("watts", watts), field("health", health), specificField);
    }

    private static String object(String... fields) {
        return "{" + String.join(",", fields) + "}";
    }

    private static String field(String name, Object renderedValue) {
        return quote(name) + ":" + renderedValue;
    }

    private static String quote(String text) {
        StringBuilder json = new StringBuilder("\"");
        for (char c : text.toCharArray()) {
            switch (c) {
                case '"' -> json.append("\\\"");
                case '\\' -> json.append("\\\\");
                case '\n' -> json.append("\\n");
                default -> json.append(c < 0x20 ? String.format("\\u%04x", (int) c) : String.valueOf(c));
            }
        }
        return json.append('"').toString();
    }
}
