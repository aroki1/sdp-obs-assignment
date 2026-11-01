package citypulse.observer;

public interface Subject<T, E extends Event<T>> {
    void subscribe(T type, Observer<? super E> observer);

    void unsubscribe(T type, Observer<? super E> observer);
}
