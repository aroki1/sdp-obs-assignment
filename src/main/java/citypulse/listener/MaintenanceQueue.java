package citypulse.listener;

import citypulse.event.MaintenanceTicket;
import citypulse.observer.Observer;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class MaintenanceQueue implements Observer<MaintenanceTicket> {
    private final Queue<MaintenanceTicket> tickets = new ConcurrentLinkedQueue<>();

    @Override
    public void onEvent(MaintenanceTicket ticket) {
        tickets.add(ticket);
    }

    public List<MaintenanceTicket> tickets() {
        return List.copyOf(tickets);
    }
}
