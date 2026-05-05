package tr.alperendemir.helix.api.types;

public record Tuple<K, V>(K key, V value) {

    public static <K, V> Tuple<K, V> of(final K key, final V value) {
        return new Tuple<>(key, value);
    }

}
