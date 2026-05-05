package tr.alperendemir.helix.api.screens.components;

import java.util.function.UnaryOperator;

public interface HelixPropertyCallback<T> {

    void fireUpdate(T value);

    void onUpdate(UnaryOperator<T> value);

}
