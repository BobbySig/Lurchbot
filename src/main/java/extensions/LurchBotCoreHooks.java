package extensions;

import org.pircbotx.hooks.CoreHooks;
import org.pircbotx.hooks.events.ServerPingEvent;

/**
 * A Twitch-specific CoreHooks implementation.
 * @author Bobby Signor
 *
 * Changes:
 * - PONG response now includes ':' for Twitch.
 */
public class LurchBotCoreHooks extends CoreHooks {
    @Override
    public void onServerPing(ServerPingEvent event) {
        event.getBot().sendRaw().rawLine("PONG :" + event.getResponse());
    }
}
