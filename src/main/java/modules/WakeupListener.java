package modules;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.types.GenericMessageEvent;

/**
 * A simplistic demo listener.
 * Created by Bobby on 3/25/2016.
 */
public class WakeupListener extends ListenerAdapter {
    private boolean awoken = false;

    @Override
    public void onGenericMessage(GenericMessageEvent event) {
        if (event.getMessage().toLowerCase().startsWith("!wakeup")) {
            if (awoken)
                event.respondWith("Stop poking me, I'm awake!");
            else
                event.respondWith("I'm up! I'm up!");

            awoken = true;
        } else if (event.getMessage().toLowerCase().startsWith("!bedtime")) {
            if (awoken)
                event.respondWith("Alright, I'll go to bed.");
            else
                event.respondWith("Zzzzz...");

            awoken = false;
        } else if (event.getMessage().toLowerCase().startsWith("!ding")) {
            event.respondWith("You rang?");
        }
    }
}
