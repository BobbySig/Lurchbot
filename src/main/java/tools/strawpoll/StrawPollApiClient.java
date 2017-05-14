package tools.strawpoll;

/**
 * @author Bobby Signor
 */
public class StrawPollApiClient {
    public static final String DEFAULT_BASE_URL = "https://www.strawpoll.me/api/";
    public static final int DEFAULT_API_VERSION = 2;

    private PollsResource pollsResource;

    public StrawPollApiClient() {
        this(DEFAULT_BASE_URL, DEFAULT_API_VERSION);
    }

    public StrawPollApiClient(String baseUrl, int version) {
        pollsResource = new PollsResource(baseUrl, version);
    }

    public PollsResource polls() {
        return pollsResource;
    }
}
