package tr.alperendemir.helix.api.scheduling;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public interface HelixScheduler {

    void sync(Runnable runnable);

    <T> T sync(Callable<T> supplier) throws Exception;

    void timer(Runnable runnable, long period, TimeUnit unit);

    <T> Iterable<T> timer(Callable<T> supplier, long period, TimeUnit unit);

    void asyncTimer(Runnable runnable, long period, TimeUnit unit);

    <T> Iterable<T> asyncTimer(Callable<T> supplier, long period, TimeUnit unit);

    void async(Runnable runnable);

    <T> CompletableFuture<T> async(Callable<T> supplier);

    void syncLater(Runnable runnable, long delay, TimeUnit unit);

    <T> CompletableFuture<T> asyncLater(Callable<T> supplier, long delay, TimeUnit unit);


}
