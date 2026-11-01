package citypulse;

import citypulse.city.AirMonitor;
import citypulse.city.Building;
import citypulse.city.City;
import citypulse.city.District;
import citypulse.city.StreetLight;
import citypulse.city.TrafficSignal;
import citypulse.city.WaterPump;

import java.util.List;

public final class TestCity {
    public static final District NORTH = new District("north", List.of(
            new Building("b1", List.of(
                    new StreetLight("l1", 100, 80, 800),
                    new AirMonitor("a1", 20, 30, "PM2.5"))),
            new TrafficSignal("t1", 150, 60, 4)));
    public static final District SOUTH = new District("south", List.of(
            new Building("b2", List.of(
                    new WaterPump("p1", 500, 50, 200),
                    new StreetLight("l2", 100, 20, 800)))));
    public static final City CITY = new City("city", List.of(NORTH, SOUTH));

    private TestCity() {
    }
}
