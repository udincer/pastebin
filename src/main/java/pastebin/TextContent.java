package pastebin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;

/**
 * Super simple model class for the text content.
 */

public class TextContent {

    @Id
    @JsonIgnore
    private String id;

    String token;
    String content;


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
        id = token;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
