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

    @Override
    protected void handlePost(HttpExchange httpExchange, JSONObject JSONin, HandlerResponse response) {
        String mapping = httpExchange.getRequestURI().toString();

        String chatroomName = readString(JSONin, "chatroomName");
        String token = readString(JSONin, "token");
        String message = readString(JSONin, "message");
        try {
            switch (mapping) {
                case "/chatroom/create":
                    createChatroom(chatroomName, token, response);
                    break;
                case "/chatroom/delete":
                    deleteChatroom(token, chatroomName, response);
                    break;
                case "/chatroom/join":
                    joinChatroom(token, chatroomName, response);
                    break;
                case "/chatroom/leave":
                    leaveChatroom(token, chatroomName, response);
                    break;
                case "/chatroom/users":
                    getChatroomUsers(chatroomName, response);
                    break;
                case "/chatroom/send":
                    sendChatroomMessage(token, chatroomName, message, response);
                    break;

                default:
                    throw new Exception("No such mapping");
            }
        } catch (Exception e) {
            response.jsonOut.put("Error", e.toString());
        }
    }

    private void sendChatroomMessage(String token, String chatroomName, String message, HandlerResponse response) {
    }

    private void getChatroomUsers(String chatroomName, HandlerResponse response) {
    }

    private void leaveChatroom(String token, String chatroomName, HandlerResponse response) {
    }

    private void joinChatroom(String token, String chatroomName, HandlerResponse response) {
    }

    private void deleteChatroom(String token, String chatroomName, HandlerResponse response) throws Exception {
        Client user = Client.findByToken(token);
        if (user != null) {
            Optional<Chatroom> chatroomToOptional = Chatroom.listChatrooms().stream().filter(chatroom -> chatroom.getUser().getName().equals(user.getName())).filter(chatroom -> chatroom.getChatroomName().equals(chatroomName)).findFirst();

            if (chatroomToOptional.isPresent()) {
                Chatroom chatroomToDelete = chatroomToOptional.get();
                Chatroom.deleteChatroom(chatroomToDelete);
                response.jsonOut.put("deleted", true);
            } else {
                response.jsonOut.put("deleted", false);
            }
        } else {
            throw new Exception("User with token does not exist.");
        }
    }

    private void createChatroom(String chatroomName, String token, HandlerResponse response) throws Exception {
        Client user = Client.findByToken(token);
        if (user != null) {
            if (Chatroom.listChatrooms().stream().anyMatch(cr -> cr.equals(chatroomName))) response.jsonOut.put("warning", "Chatroom with name " + chatroomName + " already exists, please join with /chatroom/join.");
            Chatroom chatroom = new Chatroom(user, chatroomName);
            response.jsonOut.put("chatroomName", chatroom.getChatroomName());
        } else {
            throw new Exception("User with token does not exist.");
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
