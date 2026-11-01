package citypulse.listener;

import citypulse.city.City;
import citypulse.city.CityNode;
import citypulse.city.District;
import citypulse.event.DistrictSummary;
import citypulse.event.MaintenanceTicket;
import citypulse.event.SensorType;
import citypulse.event.TicketPriority;
import citypulse.observer.EventChannel;
import citypulse.observer.Observer;
import citypulse.visitor.InspectionVisitor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class AlertService implements Observer<DistrictSummary> {
    private final Map<String, District> districtsById;
    private final Map<SensorType, Double> thresholds;
    private final InspectionVisitor inspection;
    private final EventChannel<TicketPriority, MaintenanceTicket> tickets;
    private final AtomicLong nextTicketId = new AtomicLong(1);

    public AlertService(City city, Map<SensorType, Double> thresholds, InspectionVisitor inspection,
                        EventChannel<TicketPriority, MaintenanceTicket> tickets) {
        this.districtsById = city.districts().stream().collect(Collectors.toMap(District::id, Function.identity()));
        this.thresholds = Map.copyOf(thresholds);
        this.inspection = inspection;
        this.tickets = tickets;
    }

    @Override
    public void onEvent(DistrictSummary summary) {
        Double threshold = thresholds.get(summary.type());
        District district = districtsById.get(summary.districtId());
        if (threshold == null || district == null || summary.average() <= threshold) {
            return;
        }
        List<String> faultyDevices = district.accept(inspection).stream().map(CityNode::id).toList();
        TicketPriority priority = faultyDevices.isEmpty() ? TicketPriority.ROUTINE : TicketPriority.URGENT;
        tickets.publish(new MaintenanceTicket(nextTicketId.getAndIncrement(), priority, district.id(),
                summary.type(), summary.average(), faultyDevices));
    }
}
