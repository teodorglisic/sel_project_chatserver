package chatroom.server.handlers;

import chatroom.server.Chatroom;
import chatroom.server.Client;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Optional;

public class ChatroomHandler extends Handler {

    @Override
    public void handleGet(HttpExchange httpExchange, HandlerResponse response) {
        String mapping = httpExchange.getRequestURI().toString();
        try {
            switch (mapping) {
                case "/chatrooms":
                    getChatrooms(response);
                    break;
                default:
                    throw new Exception("No such mapping");
            }
        } catch (Exception e) {
            response.jsonOut.put("Error", e.toString());
        }

    }





    private void getChatrooms(HandlerResponse response) {
        JSONArray chatroomArray = new JSONArray();
        if (!Chatroom.listChatrooms().isEmpty()) {
            Chatroom.listChatrooms().forEach(chatroom -> chatroomArray.put(chatroom.getChatroomName()));
        }
        response.jsonOut.put("chatrooms",chatroomArray);
    }




}
