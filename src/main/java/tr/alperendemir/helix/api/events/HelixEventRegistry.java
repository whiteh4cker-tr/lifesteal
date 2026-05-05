package tr.alperendemir.helix.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.Listener;

public interface HelixEventRegistry {

    void broadcast(Event event);

    void register(Listener listener);

    void unregister(Listener listener);



}
