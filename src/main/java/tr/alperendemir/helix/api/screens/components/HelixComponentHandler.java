package tr.alperendemir.helix.api.screens.components;

public interface HelixComponentHandler {

    void render(HelixComponentContext context, HelixPageComponent component);

    void handleEvent(HelixComponentEvent event, HelixComponentContext context, HelixPageComponent component);

    void close(HelixComponentContext context, HelixPageComponent component);

}
