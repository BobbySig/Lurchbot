package tools;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import extensions.LurchBotCoreHooks;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import modules.EchoChamber;
import modules.manager.BotKeepAlive;
import modules.manager.Manager;
import modules.pollster.Pollster;
import modules.WakeupListener;
import modules.responder.Responder;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.cap.EnableCapHandler;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.managers.BackgroundListenerManager;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.util.ArrayList;

public class Launcher {
    public static void main(String[] args) {
        System.out.println("Lurchbot  Copyright (C) 2017 \n" +
                "This program comes with ABSOLUTELY NO WARRANTY;\nThis is free software, and you are welcome to " +
                "redistribute it under certain conditions; see LICENSE.txt for details.");

        /* Configure the CLI Options */

        OptionParser parser = new OptionParser();

        /* Required Options */

        //The user option
        OptionSpec<String> userOption =
                parser.accepts("user", "The user to connect to Twitch as.")
                .withRequiredArg()
                .ofType(String.class)
                .required();

        //The oauth token option
        OptionSpec<String> oauthTokenOption = parser
                .accepts("oauth-token", "The oauth token for the user to connect to Twitch as.")
                .withRequiredArg()
                .ofType(String.class)
                .required();

        //The channels option
        OptionSpec<String> channelsOption = parser
                .accepts("channels", "A comma separated list of the channels you want to join.")
                .withRequiredArg()
                .ofType(String.class)
                .withValuesSeparatedBy(',')
                .required();

        //The database option
        OptionSpec<String> databaseOption = parser
                .accepts("database", "The URI of the database the bot will use.")
                .withRequiredArg()
                .ofType(String.class)
                .required();

        //The database username option
        OptionSpec<String> databaseUsername = parser
                .accepts("dbuser", "The username of the user to connect to the database as.")
                .withRequiredArg()
                .ofType(String.class)
                .required();

        //The database password option
        OptionSpec<String> databasePassword = parser
                .accepts("dbpassword", "The password of the user to connect to the database as.")
                .withRequiredArg()
                .ofType(String.class)
                .required();

        /* Modules Options */

        //Enable the quote engine
        parser.accepts("r", "Enables the call-response engine.");

        //Enable the poll management system
        parser.accepts("p", "Enables the poll management system.");

        //Enable the WakeupListener demo.
        parser.accepts("d", "Enables the WakeupListener demo.");

        /* The Actual Work */

        //Parse the options from the command line
        OptionSet options;
        try {
            options = parser.parse(args);
        } catch (OptionException e) {
            System.err.println(e.getMessage());
            try {
                parser.printHelpOn(System.out);
            } catch (IOException e2) {
                //Do Nothing
            }
            return;
        }

        //Grab the username for easier use.
        String username = options.valueOf(userOption);

        //Prepend "oauth:" to the oauth token if needed.
        String oauthToken = options.valueOf(oauthTokenOption);
        if (!oauthToken.startsWith("oauth:")) {
            oauthToken = "oauth:" + oauthToken;
        }

        //Get the list of channels from the command line.
        ArrayList<String> channels = new ArrayList<>(options.valuesOf(channelsOption));

        //Add the bot's name to the list of channels if needed.
        if (!channels.contains(username))
            channels.add(username);

        //Prepend '#' to the name of each channel if needed.
        for (int i = 0; i < channels.size(); i++) {
            if (channels.get(i).charAt(0) != '#') {
                channels.set(i, "#" + channels.get(i));
            }
        }

        //Configure & start HikariCP
        String databaseUrl = options.valueOf(databaseOption);
        HikariConfig hConfig = new HikariConfig();
        hConfig.setJdbcUrl(databaseUrl);
        hConfig.setDriverClassName("org.postgresql.Driver");
        hConfig.setUsername(options.valueOf(databaseUsername));
        hConfig.setPassword(options.valueOf(databasePassword));
        HikariDataSource hikariDataSource = new HikariDataSource(hConfig);

        //Compile the list of listeners.
        ArrayList<Listener> listeners = new ArrayList<>();

        //EchoChamber is always on, for reasons.
        BackgroundListenerManager bglistener = new BackgroundListenerManager();
        bglistener.addListener(new EchoChamber(), true);

        //Add the call-response engine to the list of listeners.
        if (options.has("r"))
            listeners.add(new Responder(hikariDataSource, databaseUrl));

        //Add Pollster to the list of Listeners.
        if (options.has("p"))
            listeners.add(new Pollster());

        //Add the WakeupListener demo to the list of listeners.
        if (options.has("d"))
            listeners.add(new WakeupListener());

        //Add Manager to the list of listeners.
        listeners.add(new Manager());

        /* Construction and Startup */

        //Build the bot configuration from the arguments.
        Configuration.Builder configBuilder = new Configuration.Builder()
                //The base settings required for Twitch.
                .setAutoNickChange(false)
                .setOnJoinWhoEnabled(false)
                .setCapEnabled(true)
                .addCapHandler(new EnableCapHandler("twitch.tv/membership"))
                .addCapHandler(new EnableCapHandler("twitch.tv/tags"))
                .addServer("irc.chat.twitch.tv", 443)
                .setSocketFactory(SSLSocketFactory.getDefault())
                //The custom config settings.
                .setName(options.valueOf(userOption))   //The bot's login name.
                .setServerPassword(oauthToken)          //The oauth token for the bot's login name.
                .addAutoJoinChannels(channels)          //The channels the bot will connect to.
                .setListenerManager(bglistener)         //Make the listener manager bglistener.
                .addListeners(listeners);               //The listeners to add to the bot.
        //Replace default CoreHooks with Twitch-specific implementation.
        configBuilder.replaceCoreHooksListener(new LurchBotCoreHooks());

        //Finalize the bot configuration.
        Configuration config = configBuilder.buildConfiguration();
        //Construct & configure the bot
        PircBotX bot = new PircBotX(config);
        //Start the bot
        try {
            while (BotKeepAlive.keepAlive())
                bot.startBot();
        } catch (IOException e) {
            System.err.println("I/O Exception: " + e.getMessage());
        } catch (IrcException e) {
            System.err.println("IRC Exception: " + e.getMessage());
        }

        hikariDataSource.close();
    }
}
