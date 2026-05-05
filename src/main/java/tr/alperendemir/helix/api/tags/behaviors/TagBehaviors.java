package tr.alperendemir.helix.api.tags.behaviors;

import tr.alperendemir.helix.api.tags.behaviors.add.AddTagBehavior;
import tr.alperendemir.helix.api.tags.behaviors.join.JoinTagBehavior;
import tr.alperendemir.helix.api.tags.behaviors.join.async.AsyncJoinTagBehavior;
import tr.alperendemir.helix.api.tags.behaviors.quit.QuitTagBehavior;
import tr.alperendemir.helix.api.tags.behaviors.remove.RemoveTagBehavior;

public final class TagBehaviors {

    public static final JoinTagBehavior JOIN = new JoinTagBehavior();
    public static final QuitTagBehavior QUIT = new QuitTagBehavior();
    public static final AsyncJoinTagBehavior ASYNC_JOIN = new AsyncJoinTagBehavior();
    public static final AddTagBehavior ADD = new AddTagBehavior();
    public static final RemoveTagBehavior REMOVE = new RemoveTagBehavior();

}
