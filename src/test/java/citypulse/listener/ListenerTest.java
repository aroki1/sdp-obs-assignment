package citypulse.listener;

import citypulse.TestCity;
import citypulse.event.DistrictSummary;
import citypulse.event.MaintenanceTicket;
import citypulse.event.SensorReading;
import citypulse.event.SensorType;
import citypulse.event.TicketPriority;
import citypulse.observer.EventChannel;
import citypulse.visitor.InspectionVisitor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListenerTest {
    private final EventChannel<SensorType, SensorReading> feed = new EventChannel<>();
    private final EventChannel<SensorType, DistrictSummary> summaries = new EventChannel<>();
    private final EventChannel<TicketPriority, MaintenanceTicket> tickets = new EventChannel<>();

    @Test
    void dashboardKeepsLatestValuePerDistrictAndType() {
        Dashboard dashboard = new Dashboard();
        feed.subscribe(SensorType.NOISE, dashboard);

        publish(SensorType.NOISE, "north", 40, 55);
        publish(SensorType.NOISE, "south", 70);

        assertEquals(Optional.of(55.0), dashboard.latest("north", SensorType.NOISE));
        assertEquals(Optional.of(70.0), dashboard.latest("south", SensorType.NOISE));
        assertEquals(Optional.empty(), dashboard.latest("north", SensorType.PM25));
    }

    @Test
    void statisticsTrackCountAverageMinAndMax() {
        StatisticsCollector statistics = new StatisticsCollector();
        feed.subscribe(SensorType.PM25, statistics);

        publish(SensorType.PM25, "north", 10, 40, 25);

        StatisticsCollector.Stats stats = statistics.stats(SensorType.PM25).orElseThrow();
        assertEquals(3, stats.count());
        assertEquals(25.0, stats.average(), 1e-9);
        assertEquals(10.0, stats.min());
        assertEquals(40.0, stats.max());
    }

    @Test
    void calibrationProbeUnsubscribesItselfAfterSampleSize() {
        CalibrationProbe probe = new CalibrationProbe(feed, SensorType.NOISE, 2);
        StatisticsCollector statistics = new StatisticsCollector();
        feed.subscribe(SensorType.NOISE, probe);
        feed.subscribe(SensorType.NOISE, statistics);

        assertThrows(IllegalStateException.class, probe::baseline);
        publish(SensorType.NOISE, "north", 30, 50, 1000);

        assertTrue(probe.isCalibrated());
        assertEquals(40.0, probe.baseline(), 1e-9);
        assertEquals(3, statistics.totalCount());
    }

    @Test
    void aggregatorRepublishesAverageOncePerFullWindow() {
        DistrictAggregator aggregator = new DistrictAggregator(3, summaries);
        feed.subscribe(SensorType.PM25, aggregator);
        List<DistrictSummary> received = new ArrayList<>();
        aggregator.subscribe(SensorType.PM25, received::add);

        publish(SensorType.PM25, "north", 10, 20, 60, 5, 5);
        publish(SensorType.PM25, "south", 90, 90);

        assertEquals(List.of(new DistrictSummary(SensorType.PM25, "north", 30.0, 3)), received);
    }

    @Test
    void alertInspectsOnlyAffectedDistrictAndPublishesTicket() {
        MaintenanceQueue queue = new MaintenanceQueue();
        tickets.subscribe(TicketPriority.URGENT, queue);
        AlertService alerts = new AlertService(TestCity.CITY, Map.of(SensorType.PM25, 50.0),
                new InspectionVisitor(40), tickets);
        summaries.subscribe(SensorType.PM25, alerts);

        summaries.publish(new DistrictSummary(SensorType.PM25, "north", 75, 10));

        assertEquals(List.of(new MaintenanceTicket(1, TicketPriority.URGENT, "north", SensorType.PM25, 75,
                List.of("a1"))), queue.tickets());
    }

    @Test
    void alertIgnoresSummariesBelowThreshold() {
        MaintenanceQueue queue = new MaintenanceQueue();
        tickets.subscribe(TicketPriority.URGENT, queue);
        tickets.subscribe(TicketPriority.ROUTINE, queue);
        AlertService alerts = new AlertService(TestCity.CITY, Map.of(SensorType.PM25, 50.0),
                new InspectionVisitor(40), tickets);
        summaries.subscribe(SensorType.PM25, alerts);

        summaries.publish(new DistrictSummary(SensorType.PM25, "north", 50, 10));

        assertEquals(List.of(), queue.tickets());
    }

    private void publish(SensorType type, String districtId, double... values) {
        for (double value : values) {
            feed.publish(new SensorReading(type, districtId, value));
        }
    }
}
