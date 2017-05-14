package modules.responder;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import tools.TwitchUser;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * A generic database driven command-response engine.
 * Created by Bobby on 4/30/2016.
 */
public class Responder extends ListenerAdapter {
    /**
     * DAO for response objects.
     */
    private Dao<ResponderResponse, Long> responsesDao;

    /**
     * The character that indicates the message is supposed to be a command.
     */
    private static final char COMMAND_PREFIX = '!';

    /**
     * Regex pattern of a properly formed add command.
     */
    private static Pattern addCommand = Pattern.compile(COMMAND_PREFIX + "add [a-z0-9]* [\\s\\S]*");

    /**
     * Regex pattern of a properly formed edit command.
     */
    private static Pattern editCommand = Pattern.compile(COMMAND_PREFIX + "edit [0-9]* [\\s\\S]*");

    /**
     * Regex pattern of a properly formed remove command.
     */
    private static Pattern removeCommand = Pattern.compile(COMMAND_PREFIX + "remove [0-9]*");

    /**
     * Regex pattern of a properly formed response command.
     */
    private static Pattern responseCommand = Pattern.compile(COMMAND_PREFIX + "response [0-9]*");

    /**
     * The random number generator used to pick from possible responses.
     */
    private Random rnjesus = new Random();

    public Responder(DataSource dataSource, String databaseUrl) {
        try {
            DataSourceConnectionSource dscs = new DataSourceConnectionSource(dataSource, databaseUrl);
            responsesDao = DaoManager.createDao(dscs, ResponderResponse.class);
            TableUtils.createTableIfNotExists(dscs, ResponderResponse.class);
        } catch (SQLException e) {
            if (!e.getMessage().equals("SQL statement failed: CREATE SEQUENCE \"responder_responses_id_seq\"")) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    /* Helper Methods */

    /**
     * Helper method that attempts to get a random response from the database given a command.
     * @param command The command given with COMMAND_PREFIX stripped.
     * @return A response to be sent or null.
     */
    private ResponderResponse respond(String command) {
        ResponderResponse response = null;
        try {
            List<ResponderResponse> responses =
                    responsesDao.query(
                    responsesDao.queryBuilder()
                            .where()
                            .eq(ResponderResponse.COMMAND_FIELD_NAME, command)
                            .prepare());
            if (responses != null) {
                response = responses.get(rnjesus.nextInt(responses.size()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * Helper method that attempts to get the response with the given ID from the database.
     * @param id The ID of the response being asked for.
     * @return The response with the given ID, or null if there isn't a response with that ID.
     */
    private ResponderResponse respond(int id) {
        ResponderResponse response = null;
        try {
            List<ResponderResponse> responses =
                    responsesDao.query(
                            responsesDao.queryBuilder()
                            .where()
                            .eq(ResponderResponse.ID_FIELD_NAME, Integer.toString(id))
                            .prepare()
                    );
            if (responses != null) {
                response = responses.get(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * Adds a command & response to the database.
     * @param command The command to add.
     * @param response The response to add.
     * @return The new response if success, or null if failed.
     */
    private ResponderResponse add(String command, String response) {
        try {
            ResponderResponse newResponse = new ResponderResponse(command, response);
            responsesDao.create(newResponse);
            return newResponse;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Modifies a response already in the database..
     * @param id The ID of the command-response set to change.
     * @param response The response to replace the old one.
     * @return Whether or not the operation succeeded.
     */
    private boolean edit(long id, String response) {
        try {
            ResponderResponse oldResponse = responsesDao.queryForId(id);
            oldResponse.setResponse(response);
            responsesDao.update(oldResponse);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Removes a command & response set from the database.
     * @param id The ID of the command-response set to change.
     * @return Whether or not the operation succeeded.
     */
    private boolean remove(long id) {
        try {
            responsesDao.deleteById(id);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /* Message Handling */

    @Override
    public void onMessage(MessageEvent event) throws Exception {
        if (event.getMessage().charAt(0) == COMMAND_PREFIX) {
            boolean commandHandled = false;
            if (new TwitchUser(event).isElevated())
                commandHandled = handleElevatedCommand(event);
            if (!commandHandled)
                handleViewerCommand(event);
        }
    }

    private boolean handleElevatedCommand(MessageEvent event) {
        boolean returnValue = false;
        String[] splits;
        int responseIndex;
        if (addCommand.matcher(event.getMessage()).matches()) {
            returnValue = true;
            splits = event.getMessage().split(" ");
            responseIndex = splits[0].length() + splits[1].length() + 2;
            ResponderResponse r = add(splits[1], event.getMessage().substring(responseIndex));
            if (r != null) {
                event.respondWith(r.getResponse() + " (#" + r.getId() + ") has been added to !" + r.getCommand() + ".");
            } else {
                event.respondWith("Sorry, but I wasn't able to add that to the database. " +
                        "Please contact the developer for help if you think you've received this message in error."
                );
            }
        } else if (editCommand.matcher(event.getMessage()).matches()) {
            returnValue = true;
            splits = event.getMessage().split(" ");
            responseIndex = splits[0].length() + splits[1].length() + 2;
            if (edit(Integer.parseInt(splits[1]), event.getMessage().substring(responseIndex))) {
                event.respondWith("I've edited entry #" + splits[1] + ", just like you asked.");
            } else {
                event.respondWith("Sorry, but I wasn't able to edit that response. " +
                        "Please contact the developer for help if you think you've received this message in error."
                );
            }
        } else if (removeCommand.matcher(event.getMessage()).matches()) {
            returnValue = true;
            splits = event.getMessage().split(" ");
            if (remove(Integer.parseInt(splits[1]))) {
                event.respondWith("I've removed entry #" + splits[1] + " from the database.");
            } else {
                event.respondWith("Sorry, but I wasn't able to remove that from the database. " +
                        "Please contact the developer for help if you think you've received this message in error."
                );
            }
        }
        return returnValue;
    }

    private void handleViewerCommand(MessageEvent event) {
        ResponderResponse response;
        if (responseCommand.matcher(event.getMessage()).matches()) {
            String[] splits = event.getMessage().split(" ");
            response = respond(Integer.parseInt(splits[1]));
        } else {
            response = respond(event.getMessage().substring(1));
        }

        if (response != null) {
            String responseStr = response.getResponse() + " (#" + response.getId() + ")";
            event.respondWith(responseStr);
        }
    }
}
