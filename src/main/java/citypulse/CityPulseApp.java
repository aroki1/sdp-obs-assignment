package citypulse;

import citypulse.city.AirMonitor;
import citypulse.city.Building;
import citypulse.city.City;
import citypulse.city.District;
import citypulse.city.StreetLight;
import citypulse.city.TrafficSignal;
import citypulse.city.WaterPump;
import citypulse.event.DistrictSummary;
import citypulse.event.MaintenanceTicket;
import citypulse.event.SensorReading;
import citypulse.event.SensorType;
import citypulse.event.TicketPriority;
import citypulse.listener.AlertService;
import citypulse.listener.CalibrationProbe;
import citypulse.listener.Dashboard;
import citypulse.listener.DistrictAggregator;
import citypulse.listener.MaintenanceQueue;
import citypulse.listener.StatisticsCollector;
import citypulse.observer.EventChannel;
import citypulse.simulation.CitySimulation;
import citypulse.simulation.SensorProducer;
import citypulse.visitor.EnergyUsageVisitor;
import citypulse.visitor.InspectionVisitor;
import citypulse.visitor.JsonExportVisitor;
import citypulse.visitor.MaintenanceCostVisitor;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;

public final class CityPulseApp {
    private static final int WINDOW_SIZE = 20;
    private static final int CALIBRATION_SAMPLES = 50;
    private static final int MIN_HEALTH = 40;
    private static final Duration SHUTDOWN_TIMEOUT = Duration.ofSeconds(30);
    private static final Map<SensorType, Double> THRESHOLDS = Map.of(
            SensorType.PM25, 60.0,
            SensorType.NOISE, 62.0,
            SensorType.TRAFFIC_DENSITY, 58.0,
            SensorType.WATER_PRESSURE, 60.0);

    private CityPulseApp() {
    }

    public static void main(String[] args) throws InterruptedException {
        int threads = args.length > 0 ? Integer.parseInt(args[0]) : 3;
        int eventsPerProducer = args.length > 1 ? Integer.parseInt(args[1]) : 1_000;

        City city = demoCity();
        EventChannel<SensorType, SensorReading> airFeed = new EventChannel<>();
        EventChannel<SensorType, SensorReading> trafficFeed = new EventChannel<>();
        EventChannel<SensorType, SensorReading> waterFeed = new EventChannel<>();
        EventChannel<SensorType, DistrictSummary> summaries = new EventChannel<>();
        EventChannel<TicketPriority, MaintenanceTicket> tickets = new EventChannel<>();

        Dashboard dashboard = new Dashboard();
        StatisticsCollector statistics = new StatisticsCollector();
        DistrictAggregator aggregator = new DistrictAggregator(WINDOW_SIZE, summaries);
        CalibrationProbe noiseProbe = new CalibrationProbe(airFeed, SensorType.NOISE, CALIBRATION_SAMPLES);
        AlertService alerts = new AlertService(city, THRESHOLDS, new InspectionVisitor(MIN_HEALTH), tickets);
        MaintenanceQueue maintenance = new MaintenanceQueue();

        List<Feed> feeds = List.of(
                new Feed(airFeed, List.of(SensorType.PM25, SensorType.NOISE)),
                new Feed(trafficFeed, List.of(SensorType.TRAFFIC_DENSITY)),
                new Feed(waterFeed, List.of(SensorType.WATER_PRESSURE)));
        for (Feed feed : feeds) {
            for (SensorType type : feed.types()) {
                feed.channel().subscribe(type, dashboard);
                feed.channel().subscribe(type, statistics);
                feed.channel().subscribe(type, aggregator);
            }
        }
        airFeed.subscribe(SensorType.NOISE, noiseProbe);
        THRESHOLDS.keySet().forEach(type -> aggregator.subscribe(type, alerts));
        for (TicketPriority priority : TicketPriority.values()) {
            tickets.subscribe(priority, maintenance);
        }

        List<String> districtIds = city.districts().stream().map(District::id).toList();
        List<SensorProducer> producers = feeds.stream()
                .map(feed -> new SensorProducer(feed.channel(), feed.types(), districtIds, eventsPerProducer, new Random()))
                .toList();
        new CitySimulation(Executors.newFixedThreadPool(threads), SHUTDOWN_TIMEOUT).run(producers);

        MaintenanceCostVisitor maintenanceCost = new MaintenanceCostVisitor();
        city.accept(maintenanceCost);
        System.out.printf("Readings processed: %d%n", statistics.totalCount());
        for (SensorType type : SensorType.values()) {
            statistics.stats(type).ifPresent(stats -> System.out.printf("  %-15s avg %.2f  min %.2f  max %.2f%n",
                    type, stats.average(), stats.min(), stats.max()));
        }
        System.out.printf("Latest PM2.5 in %s: %.2f%n", districtIds.get(0),
                dashboard.latest(districtIds.get(0), SensorType.PM25).orElse(Double.NaN));
        System.out.printf("Noise baseline: %.2f%n", noiseProbe.baseline());
        System.out.printf("Maintenance tickets: %d%n", maintenance.tickets().size());
        maintenance.tickets().stream().limit(5).forEach(ticket -> System.out.println("  " + ticket));
        System.out.printf("City energy use: %.1f W%n", city.accept(new EnergyUsageVisitor()));
        System.out.printf("Maintenance cost by district: %s%n", maintenanceCost.costByDistrict());
        System.out.println(city.accept(new JsonExportVisitor()));
    }

    private static City demoCity() {
        return new City("almaty", List.of(
                new District("medeu", List.of(
                        new Building("medeu-school", List.of(
                                new StreetLight("ml-1", 60, 90, 800),
                                new AirMonitor("ma-1", 15, 35, "PM2.5"))),
                        new TrafficSignal("mt-1", 120, 70, 4),
                        new WaterPump("mw-1", 750, 25, 300))),
                new District("bostandyk", List.of(
                        new Building("bostandyk-mall", List.of(
                                new StreetLight("bl-1", 60, 30, 800),
                                new StreetLight("bl-2", 60, 85, 800),
                                new WaterPump("bw-1", 1100, 80, 450))),
                        new Building("bostandyk-clinic", List.of(
                                new AirMonitor("ba-1", 15, 95, "NO2"))),
                        new TrafficSignal("bt-1", 120, 50, 3))),
                new District("almaly", List.of(
                        new TrafficSignal("at-1", 120, 20, 4),
                        new StreetLight("al-1", 60, 75, 1200)))));
    }

    private record Feed(EventChannel<SensorType, SensorReading> channel, List<SensorType> types) {
    }
}
