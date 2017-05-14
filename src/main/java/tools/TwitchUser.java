package tools;

import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.events.MessageEvent;

/**
 * A wrapper for a user with Extra features added from Twitch.
 * Created by Bobby on 4/1/2016.
 */
public class TwitchUser {
    private MessageEvent event;

    private boolean isTurbo;
    private boolean isSubscriber;
    private boolean isModerator;
    private boolean isBroadcaster;

    public TwitchUser(MessageEvent event) {
        this.event = event;

        isTurbo = event.getTags().get("turbo").equals("1");
        isSubscriber = event.getTags().get("subscriber").equals("1");
        isModerator = event.getTags().get("mod").equals("1");
        isBroadcaster = event.getTags().get("user-id").equals(event.getTags().get("room-id"));
    }

    public boolean isTurbo() {
        return isTurbo;
    }

    public boolean isSubscriber() {
        return isSubscriber;
    }

    public boolean isModerator() {
        return isModerator;
    }

    public boolean isBroadcaster() {
        return isBroadcaster;
    }

    public boolean isElevated() {
        return isModerator || isBroadcaster;
    }

    public Event getEvent() {
        return event;
    }
}
