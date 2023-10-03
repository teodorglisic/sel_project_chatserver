package chatroom.server.handlers;

import chatroom.server.Client;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONObject;

public class PingHandler extends Handler {
    @Override
    protected void handleGet(HttpExchange httpExchange, HandlerResponse response) {
        response.statusCode = 200;
        response.jsonOut.put("ping", true);
    }

    @Override
    protected void handlePost(HttpExchange httpExchange, JSONObject JSONin, HandlerResponse response) {
        boolean goodToken = false;
        String token = readString(JSONin, "token");
        if (token != null) {
            goodToken = Client.findByToken(token) != null;
        }
        if (goodToken) {
            response.statusCode = 200;
            response.jsonOut.put("ping", true);
        } else {
            response.statusCode = 200;
            response.jsonOut.put("Error", "Missing or invalid token");
        }
    }
}

