package tr.alperendemir.helix.api.types;

public record Some<E, T>(E error, T value) {

    public static <E, T> Some<E, T> error(E error) {
        return new Some<>(error, null);
    }

    public static <E, T> Some<E, T> value(T value) {
        return new Some<>(null, value);
    }

    public static <E, T> Some<E, T> empty() {
        return new Some<>(null, null);
    }

    public boolean isEmpty() {
        return !hasError() && !hasValue();
    }

    public boolean hasError() {
        return this.error != null;
    }

    public boolean hasValue() {
        return this.value != null;
    }

}
