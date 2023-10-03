package chatroom.server.handlers;

import chatroom.server.Client;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONObject;

public class ChatHandler extends Handler {
    @Override
    protected void handlePost(HttpExchange httpExchange, JSONObject JSONin, HandlerResponse response) {
        String mapping = httpExchange.getRequestURI().toString(); // For this handler, will begin with "/user"

        // Read various strings that may be present (depending on the mapping)
        String username = readString(JSONin, "username");
        String message = readString(JSONin, "message");
        String token = readString(JSONin, "token");

        // If anything at all goes wrong, we throw an exception and return an error
        try {
            switch (mapping) {
                case "/chat/send" -> {
                    if (token == null || username == null || message == null) throw new Exception("Invalid parameters");
                    sendMessage(token, username, message, response);
                }
                case "/chat/poll" -> {
                    if (token == null) throw new Exception("Invalid parameters");
                    receiveMessages(token, response);
                }
                default -> {
                    throw new Exception("No such mapping");
                }
            }
        } catch (Exception e) {
            response.jsonOut.put("Error", e.getMessage());
        }
    }

    private void sendMessage(String token, String username, String message, HandlerResponse response) throws Exception {
        boolean success = false;
        Client sender = Client.findByToken(token);
        if (sender == null) throw new Exception("Invalid token");
        Client recipient = Client.findByUsername(username);
        if (recipient != null) {
            recipient.send(sender.getName(), message);
            success = true;
        }
        response.jsonOut.put("send", success);
    }

    private void receiveMessages(String token, HandlerResponse response) throws Exception {
        Client client = Client.findByToken(token);
        if (client == null) throw new Exception("Invalid token");
        response.jsonOut.put("messages", client.getMessages());
    }
}

