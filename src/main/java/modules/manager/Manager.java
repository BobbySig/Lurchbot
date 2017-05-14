package modules.manager;

import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import tools.TwitchUser;

/**
 * Allows the primary administrator of the bot to perform administrative tasks such as shutting down the bot from chat.
 * Created by Bobby on 4/17/2016.
 */
public class Manager extends ListenerAdapter {
    @Override
    public void onMessage(MessageEvent event) throws Exception {
        //Check if the message was sent in the bot's own channel.
        if (event.getChannel().getName().substring(1).equals(event.getBot().getUserBot().getNick())) {
            //It was, now check if it was an elevated user.
            TwitchUser tuser = new TwitchUser(event);
            if (tuser.isElevated() && event.getMessage().startsWith("!")) {
                //Handle the command
                handleElevatedCommands(event);
            }
        }
    }

    /**
     * Parse out commands for elevated users.
     * @param event The message event to be checked against the commands.
     */
    private void handleElevatedCommands(GenericMessageEvent event) {
        switch (event.getMessage().substring(1).toLowerCase()) {
            case "shutdown":
                shutdown(event.getBot());
                break;
            default:
                //Do nothing.
        }
    }

    /**
     * Properly shuts down the bot.
     */
    private static void shutdown(PircBotX bot) {
        //Notify all channels the bot is going down.
        for (Channel c : bot.getUserBot().getChannels())
            bot.send().message(c.getName(), "The bot is shutting down. Bye!");

        //Tell the Launcher this is a for-real shutdown.
        BotKeepAlive.setKeepAlive(false);
        //Initiate shutdown sequence.
        bot.sendIRC().quitServer();
    }
}
