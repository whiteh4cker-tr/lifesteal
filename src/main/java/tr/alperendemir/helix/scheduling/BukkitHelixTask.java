package tr.alperendemir.helix.scheduling;

import java.util.concurrent.Callable;

// TODO: This is to be used later when the scheduler is rewritten
public class BukkitHelixTask {

    private final Callable<?> callable;
    private final long period;
    private final boolean repeating;

    private long remainingTicks;

    public BukkitHelixTask(Callable<?> callable, long period, boolean repeating) {
        this.callable = callable;
        this.period = period;
        this.repeating = repeating;
    }

    public Callable<?> getCallable() {
        return this.callable;
    }

    public long getPeriod() {
        return this.period;
    }

    public void resetTicks() {
        this.remainingTicks = this.period;
    }

    public boolean decreaseAndCheckTicks() {
        return --this.remainingTicks <= 0;
    }

    public boolean isRepeating() {
        return this.repeating;
    }
}
