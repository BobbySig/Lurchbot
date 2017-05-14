package tools.strawpoll;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import org.asynchttpclient.*;

import java.util.concurrent.CompletableFuture;

/**
 * A wrapper around the Strawpoll API's Polls endpoint.
 * @author Bobby Signor
 */
public class PollsResource {
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11";

    private static Gson gson;
    private static AsyncHttpClient httpClient = new DefaultAsyncHttpClient();
    private String baseUrl;
    private HttpHeaders headers = new DefaultHttpHeaders();

    public PollsResource(String baseUrl, int version) {
        this.baseUrl = baseUrl + "v" + Integer.toString(version) + "/polls";
        headers.add("Content-Type", "application/json");
        headers.add("User-Agent", USER_AGENT);
        gson = new GsonBuilder()
                .addSerializationExclusionStrategy(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes fieldAttributes) {
                        return fieldAttributes.getName().equals("id");
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> aClass) {
                        return false;
                    }
                })
                .create();
    }

    /**
     * Performs an HTTP GET request on the Strawpoll API's Polls endpoint.
     * @param id The ID of the poll to request.
     * @return A {@link CompletableFuture< Poll >} that will contain the requested Poll or will thrown an error.
     */
    public CompletableFuture<Poll> get(int id) {
        String url = baseUrl + "/" + Integer.toString(id);
        return httpClient
                .prepareGet(url)
                .setHeaders(headers)
                .execute()
                .toCompletableFuture()
                .thenCompose(response -> {
                    Poll p = gson.fromJson(response.getResponseBody(), Poll.class);
                    return CompletableFuture.completedFuture(p);
                });
    }

    /**
     * Performs an HTTP POST request on the Strawpoll API's Polls endpoint to create a new {@link Poll}.
     * @param p The {@link Poll} to send to the Strawpoll API.
     * @return A {@link CompletableFuture<Poll>} that will contain the newly created Poll or will throw an error.
     */
    public CompletableFuture<Poll> post(Poll p) {
        String msgBody = gson.toJson(p);
        return httpClient
                .preparePost(baseUrl)
                .setHeaders(headers)
                .setBody(msgBody)
                .execute()
                .toCompletableFuture()
                .thenCompose(response -> {
                    Poll np = gson.fromJson(response.getResponseBody(), Poll.class);
                    return CompletableFuture.completedFuture(np);
                });
    }
}
