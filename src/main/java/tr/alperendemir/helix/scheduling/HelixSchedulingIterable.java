package tr.alperendemir.helix.scheduling;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class HelixSchedulingIterable<T> implements Iterable<T> {

    private final HelixSchedulingIterator<T> iterator;

    public HelixSchedulingIterable(AtomicBoolean closed, Supplier<T> supplier) {
        this.iterator = new HelixSchedulingIterator<>(closed, supplier);
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return this.iterator;
    }
}
