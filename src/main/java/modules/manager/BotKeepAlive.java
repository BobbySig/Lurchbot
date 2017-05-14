package modules.manager;

/**
 * A class containing whether or not the bot should attempt to reconnect if it gets disconnected.
 * @author Bobby Signor
 */
public class BotKeepAlive {
    private static boolean keepAlive = true;

    public static boolean keepAlive() {
        return keepAlive;
    }

    public static void setKeepAlive(boolean keepAlive) {
        BotKeepAlive.keepAlive = keepAlive;
    }
}
