package tr.alperendemir.helix.api.config.builder;

import tr.alperendemir.helix.api.config.Commentable;

public interface ElementBuilder extends Commentable<ElementBuilder> {

    ConfigurationBuilder next();

}
