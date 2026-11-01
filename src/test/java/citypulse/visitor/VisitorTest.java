package citypulse.visitor;

import citypulse.TestCity;
import citypulse.city.AirMonitor;
import citypulse.city.CityNode;
import citypulse.city.District;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VisitorTest {

    @Test
    void energyUsageSumsWholeCity() {
        assertEquals(870.0, TestCity.CITY.accept(new EnergyUsageVisitor()));
    }

    @Test
    void energyUsageOfSubtreeOnly() {
        assertEquals(270.0, TestCity.NORTH.accept(new EnergyUsageVisitor()));
    }

    @Test
    void maintenanceCostAccumulatesPerDistrict() {
        MaintenanceCostVisitor visitor = new MaintenanceCostVisitor();
        TestCity.CITY.accept(visitor);

        // north: 50*0.2 + 150*0.7 + 200*0.4 = 195, south: 400*0.5 + 50*0.8 = 240
        assertEquals(Map.of("north", 195.0, "south", 240.0), visitor.costByDistrict());
        assertEquals(435.0, visitor.totalCost());
    }

    @Test
    void inspectionReturnsUnhealthyDevicesInTreeOrder() {
        List<String> ids = TestCity.CITY.accept(new InspectionVisitor(40)).stream().map(CityNode::id).toList();

        assertEquals(List.of("a1", "l2"), ids);
    }

    @Test
    void inspectionOfSubtreeIgnoresOtherDistricts() {
        List<String> ids = TestCity.SOUTH.accept(new InspectionVisitor(60)).stream().map(CityNode::id).toList();

        assertEquals(List.of("p1", "l2"), ids);
    }

    @Test
    void jsonExportNestsContainers() {
        String expected = "{\"type\":\"district\",\"id\":\"south\",\"children\":["
                + "{\"type\":\"building\",\"id\":\"b2\",\"children\":["
                + "{\"type\":\"waterPump\",\"id\":\"p1\",\"watts\":500.0,\"health\":50,\"litresPerMinute\":200.0},"
                + "{\"type\":\"streetLight\",\"id\":\"l2\",\"watts\":100.0,\"health\":20,\"lumens\":800}]}]}";

        assertEquals(expected, TestCity.SOUTH.accept(new JsonExportVisitor()));
    }

    @Test
    void jsonExportEscapesStrings() {
        District district = new District("a\"b", List.of(new AirMonitor("m\\1", 5, 90, "NO\n2")));

        assertEquals("{\"type\":\"district\",\"id\":\"a\\\"b\",\"children\":["
                        + "{\"type\":\"airMonitor\",\"id\":\"m\\\\1\",\"watts\":5.0,\"health\":90,\"pollutant\":\"NO\\n2\"}]}",
                district.accept(new JsonExportVisitor()));
    }
}
