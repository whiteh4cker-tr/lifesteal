package tr.alperendemir.helix.scheduling;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class HelixSchedulingIterator<T> implements Iterator<T> {

    private final AtomicBoolean closed;
    private final Supplier<T> supplier;

    public HelixSchedulingIterator(AtomicBoolean closed, Supplier<T> supplier) {
        this.closed = closed;
        this.supplier = supplier;
    }

    @Override
    public boolean hasNext() {
        return !this.closed.get();
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more elements");
        }

        return this.supplier.get();
    }
}
