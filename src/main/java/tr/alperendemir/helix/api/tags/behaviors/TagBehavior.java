package tr.alperendemir.helix.api.tags.behaviors;

public abstract class TagBehavior<T extends HelixTagContext> {

    public abstract boolean async();

}
