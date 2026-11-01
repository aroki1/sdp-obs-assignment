package citypulse.simulation;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public final class CitySimulation {
    private final ExecutorService executor;
    private final Duration timeout;

    public CitySimulation(ExecutorService executor, Duration timeout) {
        this.executor = executor;
        this.timeout = timeout;
    }

    public void run(List<? extends Runnable> producers) throws InterruptedException {
        List<Callable<Object>> tasks = producers.stream().map(producer -> Executors.callable(producer)).toList();
        try {
            for (Future<Object> result : executor.invokeAll(tasks, timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                result.get();
            }
        } catch (ExecutionException e) {
            throw new IllegalStateException("Producer failed", e.getCause());
        } finally {
            executor.shutdown();
            if (!executor.awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
        }
    }
}
