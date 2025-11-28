package chatroom.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Chatroom {

    private static final ArrayList<Chatroom> chatrooms = new ArrayList<>();
    private final ArrayList<Client> members = new ArrayList<>();
    private final Client user;
    private final String chatroomName;


    public Chatroom(Client user, String chatroomName) {
        this.chatroomName = chatroomName;
        chatrooms.add(this);
        this.user = user;
        members.add(user);
    }

    public static void deleteChatroom(Chatroom chatroomToDelete) {
        chatrooms.removeIf(chatroom -> chatroom.equals(chatroomToDelete));
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) return false;
        Chatroom chatroom = (Chatroom) o;
        return this.chatroomName.equals(chatroom.getChatroomName());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(chatroomName);
    }

    public String getChatroomName() {
        return chatroomName;
    }


    public static List<Chatroom> listChatrooms() {
        return chatrooms;
    }

    public Client getUser() {
        return user;
    }
}
