package modules;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.*;

/**
 * A listener that echoes all messages sent and received to the command line.
 * Created by Bobby on 3/27/2016.
 */
public class EchoChamber extends ListenerAdapter {
    private final String outputFormat = "%s\t|\t%s:\t%s";

    @Override
    public void onMessage(MessageEvent event) throws Exception {
        System.out.println(
                String.format(outputFormat,
                        event.getChannel().getName(),
                        event.getUser().getNick(),
                        event.getMessage()
                )
        );
    }

    @Override
    public void onOutput(OutputEvent event) throws Exception {
        if (event.getLineParsed().get(0).equals("PRIVMSG")) {
            System.out.println(
                    String.format(outputFormat,
                            event.getLineParsed().get(1),
                            event.getBot().getNick(),
                            event.getLineParsed().get(2)
                    )
            );
        }
    }

    @Override
    public void onConnect(ConnectEvent event) throws Exception {
        System.out.println("Connected to " + event.getBot().getServerHostname() + " as " + event.getBot().getNick() + ".");
    }

    @Override
    public void onJoin(JoinEvent event) throws Exception {
        if (event.getUser().getNick().equals(event.getBot().getNick())) {
            System.out.println("Joined " + event.getChannel().getName() + " as " + event.getBot().getNick() + ".");
            event.getBot().send().message(event.getChannel().getName(), "The bot is up!");
        }
    }

    @Override
    public void onConnectAttemptFailed(ConnectAttemptFailedEvent event) {
        System.err.println("Connection to " + event.getBot().getServerHostname() + " as " + event.getBot().getNick() + " failed.");
        System.err.println("Reason: " + event.getConnectExceptions().values().asList().get(0).getMessage());
    }
}
