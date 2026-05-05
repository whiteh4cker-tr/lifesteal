package tr.alperendemir.helix.scheduling;

import tr.alperendemir.helix.BukkitHelixProvider;
import tr.alperendemir.helix.api.logging.HelixLogger;
import tr.alperendemir.helix.api.scheduling.HelixScheduler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class BukkitHelixScheduler implements HelixScheduler {

    // TODO post-release improve this class... significantly...

    @Override
    public void sync(Runnable runnable) {
        Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(BukkitHelixProvider.class), runnable);
    }

    @Override
    public <T> T sync(Callable<T> supplier) throws Exception {
        if (Bukkit.isPrimaryThread()) {
            return supplier.call();
        }

        try {
            var future = Bukkit.getScheduler().callSyncMethod(JavaPlugin.getPlugin(BukkitHelixProvider.class), supplier);
            return future.get();
        } catch (ExecutionException exception) {
            if (exception.getCause() instanceof Exception ex) {
                throw ex;
            }
        }

        return null;
    }

    @Override
    public void timer(Runnable runnable, long period, TimeUnit unit) {
        var ticks = unit.toSeconds(period) * 20L;

        Bukkit.getScheduler().runTaskTimer(JavaPlugin.getPlugin(BukkitHelixProvider.class), task -> {
            try {
                runnable.run();
            } catch (IllegalStateException e) {
                if (e.getMessage().equals("zip file closed")) {
                    task.cancel(); // TEMPORARY FIX UNTIL THE SCHEDULER IS IMPROVED!
                }
            }
        }, ticks, ticks);
    }

    @Override
    public <T> Iterable<T> timer(Callable<T> supplier, long period, TimeUnit unit) {
        var closed = new AtomicBoolean(false);
        var ticks = unit.toSeconds(period) * 20L;

        var futureRef = new AtomicReference<>(new CompletableFuture<T>());

        Supplier<T> sup = () -> {
            try {
                return futureRef.get().get();
            } catch (InterruptedException | ExecutionException e) {
                HelixLogger.reportError(e);
                closed.set(true);
            }

            return null;
        };

        Bukkit.getScheduler().runTaskTimer(JavaPlugin.getPlugin(BukkitHelixProvider.class), task -> {
            var future = futureRef.get();

            try {
                var res = supplier.call();
                if (res == null) {
                    future.complete(null);
                } else {
                    future.complete(res);
                    futureRef.set(new CompletableFuture<>());
                    return;
                }
            } catch (IllegalStateException e) {
                if (e.getMessage().equals("zip file closed")) {
                    task.cancel(); // TEMPORARY FIX UNTIL THE SCHEDULER IS IMPROVED!
                }
            } catch (Exception e) {
                HelixLogger.reportError(e);
            }

            task.cancel();
            closed.set(true);
        }, ticks, ticks);

        return new HelixSchedulingIterable<>(closed, sup);
    }

    @Override
    public void asyncTimer(Runnable runnable, long period, TimeUnit unit) {
        var ticks = unit.toSeconds(period) * 20L;

        Bukkit.getScheduler().runTaskTimerAsynchronously(JavaPlugin.getPlugin(BukkitHelixProvider.class), task -> {
            try {
                runnable.run();
            } catch (IllegalStateException e) {
                if (e.getMessage().equals("zip file closed")) {
                    task.cancel(); // TEMPORARY FIX UNTIL THE SCHEDULER IS IMPROVED!
                }
            }
        }, ticks, ticks);
    }

    @Override
    public <T> Iterable<T> asyncTimer(Callable<T> supplier, long period, TimeUnit unit) {
        var closed = new AtomicBoolean(false);
        var ticks = unit.toSeconds(period) * 20L;

        var futureRef = new AtomicReference<>(new CompletableFuture<T>());

        Supplier<T> sup = () -> {
            try {
                return futureRef.get().get();
            } catch (InterruptedException | ExecutionException e) {
                HelixLogger.reportError(e);
                closed.set(true);
                throw new RuntimeException("Error while running async task", e);
            }
        };

        Bukkit.getScheduler().runTaskTimerAsynchronously(JavaPlugin.getPlugin(BukkitHelixProvider.class), task -> {
            var future = futureRef.get();

            try {
                var res = supplier.call();
                if (res != null) {
                    future.complete(res);
                    futureRef.set(new CompletableFuture<>());
                    return;
                }
            } catch (IllegalStateException e) {
                if (e.getMessage().equals("zip file closed")) {
                    task.cancel(); // TEMPORARY FIX UNTIL THE SCHEDULER IS IMPROVED!
                }
            } catch (Exception e) {
                HelixLogger.reportError(e);
            }

            future.complete(null);
            task.cancel();
            closed.set(true);
        }, ticks, ticks);

        return new HelixSchedulingIterable<>(closed, sup);
    }

    @Override
    public void async(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(JavaPlugin.getPlugin(BukkitHelixProvider.class), runnable);
    }

    @Override
    public <T> CompletableFuture<T> async(Callable<T> supplier) {
        var future = new CompletableFuture<T>();

        Bukkit.getScheduler().runTaskAsynchronously(JavaPlugin.getPlugin(BukkitHelixProvider.class), () -> {
            try {
                future.complete(supplier.call());
            } catch (Exception e) {
                HelixLogger.reportError(e);
            }
        });

        return future;
    }

    @Override
    public void syncLater(Runnable runnable, long delay, TimeUnit unit) {
        var ticks = unit.toSeconds(delay) * 20L;

        Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(BukkitHelixProvider.class), task -> {
            try {
                runnable.run();
            } catch (IllegalStateException e) {
                if (e.getMessage().equals("zip file closed")) {
                    task.cancel(); // TEMPORARY FIX UNTIL THE SCHEDULER IS IMPROVED!
                }
            }
        }, ticks);
    }

    @Override
    public <T> CompletableFuture<T> asyncLater(Callable<T> supplier, long delay, TimeUnit unit) {
        var future = new CompletableFuture<T>();
        var ticks = unit.toSeconds(delay) * 20L;

        Bukkit.getScheduler().runTaskLaterAsynchronously(JavaPlugin.getPlugin(BukkitHelixProvider.class), task -> {
            try {
                future.complete(supplier.call());
            } catch (IllegalStateException e) {
                if (e.getMessage().equals("zip file closed")) {
                    task.cancel(); // TEMPORARY FIX UNTIL THE SCHEDULER IS IMPROVED!
                }
            } catch (Exception e) {
                HelixLogger.reportError(e);
            }
        }, ticks);

        return future;
    }
}
