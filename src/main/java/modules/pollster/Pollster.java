package modules.pollster;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import tools.TwitchUser;
import tools.strawpoll.Poll;
import tools.strawpoll.StrawPollApiClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates and runs polls hosted on Straw Poll in chat.
 * @author Bobby Signor
 */
public class Pollster extends ListenerAdapter {
    private static final String STRAWPOLL_URL_BASE = "https://strawpoll.me/";

    private StrawPollApiClient strawPollApiClient = new StrawPollApiClient();
    private Poll currentPoll = null;
    private boolean pollIsRunning = false;

    /**
     * The prefix for any command.
     */
    private static final char CMD_PREFIX = '!';

    /**
     * The base poll command. All commands handled by this module should match this pattern.
     */
    private static final String BASE_POLL_COMMAND = CMD_PREFIX + "poll";

    /**
     * Handles a <code>!poll start</code> command from a chat.
     * @param event The event containing the <code>!poll start</code> command.
     */
    private void startPoll(MessageEvent event) {
        if (pollIsRunning) {
            event.respond("I'm currently running a poll, end it with \"!poll end\" before asking me to start a new one.");
            return;
        }

        String[] cmdTokens = event.getMessage().substring(12).split("\\|");
        if (cmdTokens.length < 3) {
            event.respond("Your poll needs a title and at least 2 options, separated with \"|\". Add more options and try again.");
        } else if (cmdTokens.length > 31) {
            event.respond("Your poll has more than 30 options, please reduce the number of options and try again.");
        } else {
            List<String> options = new ArrayList<>(cmdTokens.length - 1);
            for (int i = 1; i < cmdTokens.length; i++)
                options.add(cmdTokens[i]);

            strawPollApiClient.polls()
                    .post(new Poll(cmdTokens[0], options))
                    .exceptionally(t -> {
                        t.printStackTrace();
                        event.respondWith("Sorry, I can't create that poll.");
                        return null;
                    })
                    .thenApply(poll -> {
                        currentPoll = poll;
                        event.respondWith(poll.prettyPrint());
                        pollIsRunning = true;
                        return poll;
                    });
        }
    }

    /**
     * Handles a <code>!poll end</code> command from a chat.
     * @param event The event containing the <code>!poll end</code> command.
     */
    private void endPoll(MessageEvent event) {
        if (pollIsRunning) {
            pollIsRunning = false;
            event.respondWith("Poll ended! " + currentPoll.title + " Winner: " + currentPoll.leader());
        } else {
            event.respond("There is no currently running poll.");
        }
    }

    /**
     * Puts the status of the current poll into chat.
     * @param event The message that requested this.
     */
    private void currentPollStatus(MessageEvent event) {
        if (pollIsRunning && currentPoll != null) {
            strawPollApiClient.polls()
                    .get(currentPoll.id)
                    .exceptionally(t -> {
                        t.printStackTrace();
                        event.respondWith("Sorry, I can't update the current poll.");
                        return null;
                    })
                    .thenApply(poll -> {
                        currentPoll = poll;
                        event.respondWith(poll.prettyPrint());
                        return poll;
                    });
        } else {
            String responseStr = "There is currently no running poll.";
            if (currentPoll != null)
                responseStr += " Last Poll: " + currentPoll.prettyPrint();
            event.respondWith(responseStr);
        }
    }

    /**
     * Handles forwarding the poll command to wherever it needs to go.
     * @param event The event with the received poll command.
     */
    private void onPollCommand(MessageEvent event) {
        String[] tokens = event.getMessage().split(" ");
        TwitchUser usr = new TwitchUser(event);
        if (tokens.length > 1) {
            //If the user is elevated,
            if (usr.isElevated()) {
                switch (tokens[1]) {
                    case "start":
                        startPoll(event);
                        break;
                    case "end":
                        endPoll(event);
                        break;
                }
            }
        } else {
            currentPollStatus(event);
        }
    }

    @Override
    public void onMessage(MessageEvent event) throws Exception {
        //If the message matches the !poll command, continue onward.
        if (event.getMessage().startsWith(BASE_POLL_COMMAND))
            onPollCommand(event);
    }
}
