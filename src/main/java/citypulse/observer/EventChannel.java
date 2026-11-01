package citypulse.observer;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public final class EventChannel<T, E extends Event<T>> implements Subject<T, E> {
    private final Map<T, Set<Observer<? super E>>> observersByType = new ConcurrentHashMap<>();

    @Override
    public void subscribe(T type, Observer<? super E> observer) {
        observersByType.computeIfAbsent(type, t -> new CopyOnWriteArraySet<>()).add(observer);
    }

    @Override
    public void unsubscribe(T type, Observer<? super E> observer) {
        Set<Observer<? super E>> observers = observersByType.get(type);
        if (observers != null) {
            observers.remove(observer);
        }
    }

    // Iterates a copy-on-write snapshot, so handlers may (un)subscribe while events are in flight.
    public void publish(E event) {
        observersByType.getOrDefault(event.type(), Set.of()).forEach(observer -> observer.onEvent(event));
    }
}
