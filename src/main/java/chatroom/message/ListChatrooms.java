package chatroom.message;

import chatroom.server.Chatroom;
import chatroom.server.Client;

import java.util.ArrayList;

public class ListChatrooms extends Message {
	private String token;

	@Override
	public void process(Client client) {
		if (client.getToken().equals(token)) {
			ArrayList<String> names = Chatroom.listPublicNames();
			client.send(new ResultWithList(this.getClass(), true, names));
		} else {
			client.send(new Result(this.getClass(), false));
		}
	}
}
