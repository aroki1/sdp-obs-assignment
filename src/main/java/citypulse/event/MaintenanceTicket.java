package citypulse.event;

import citypulse.observer.Event;

import java.util.List;

public record MaintenanceTicket(long id, TicketPriority priority, String districtId,
                                SensorType trigger, double average, List<String> deviceIds)
        implements Event<TicketPriority> {

    public MaintenanceTicket {
        deviceIds = List.copyOf(deviceIds);
    }

    @Override
    public TicketPriority type() {
        return priority;
    }
}
