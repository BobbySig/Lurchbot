package tools.strawpoll;

import java.util.ArrayList;
import java.util.List;

/**
 * A representation of a single poll. Based on the Strawpoll API.
 * Created by Bobby on 4/2/2016.
 */
public class Poll {
    /**
     * Pretty-printing base format for a Poll.
     * Parameters: Title, Options, Link
     */
    private static final String PRETTY_PRINT_BASE_FMT = "%s || %s|| %s";

    /**
     * Pretty-printing format for a Poll option.
     * Parameters: Rank, Description, Votes
     */
    private static final String PRETTY_PRINT_OPTION_FMT = "#%d: %s (%d votes) ";

    private static final String PRETTY_PRINT_URL_FMT = "Vote at https://strawpoll.me/%d";

    public int id = 0;
    public String title;
    public List<String> options;
    public List<Integer> votes;
    public final boolean multi = false;
    public final String dupcheck = "normal";
    public final boolean captcha = false;

    public Poll(String title, List<String> options) {
        this.title = title;
        this.options = options;
    }

    public String leader() {
        int maxIndex = votes.get(0);

        for (int i = 1; i < votes.size(); i++)
            if (votes.get(i) > votes.get(maxIndex))
                maxIndex = i;

        return options.get(maxIndex);
    }

    public String prettyPrint() {
        String options = prettyPrintOptions();
        String url = String.format(PRETTY_PRINT_URL_FMT, id);
        return String.format(PRETTY_PRINT_BASE_FMT, title, options, url);
    }

    private String prettyPrintOptions() {
        ArrayList<Integer> ranks = new ArrayList<>(options.size());
        ranks.add(0);

        for (int i = 1; i < votes.size(); i++) {
            int j = 0;
            while (!ranks.isEmpty() && votes.get(i) < votes.get(ranks.get(j)))
                j++;
            ranks.add(j, i);
        }

        String s = "";
        for (int i = 0; i < votes.size(); i++)
            s += String.format(PRETTY_PRINT_OPTION_FMT, i, options.get(i), votes.get(i));

        return s;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Poll poll = (Poll) o;

        return id == poll.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "Poll{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", options=" + options +
                ", votes=" + votes +
                '}';
    }
}
