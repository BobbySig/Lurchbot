package modules.responder;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * A class that represents a single response from Responder.
 * Created by Bobby on 8/25/2016.
 */
@DatabaseTable(tableName = "responder_responses")
public class ResponderResponse {
    public static final String ID_FIELD_NAME = "id";
    public static final String COMMAND_FIELD_NAME = "command";
    public static final String RESPONSE_FIELD_NAME = "response";

    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true, columnName = ID_FIELD_NAME)
    private long id;
    @DatabaseField(columnName = COMMAND_FIELD_NAME)
    private String command;
    @DatabaseField(columnName = RESPONSE_FIELD_NAME)
    private String response;

    public ResponderResponse() {
        //No-arg constructor for ORMLite.
    }

    public ResponderResponse(String command, String response) {
        this.command = command;
        this.response = response;
    }

    public long getId() {
        return id;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
