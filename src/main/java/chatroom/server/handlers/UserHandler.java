package chatroom.server.handlers;

import chatroom.server.Account;
import chatroom.server.Client;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONArray;
import org.json.JSONObject;

public class UserHandler  extends Handler {
    /**
     * The only valid GET-mapping for this handler is /users
     */
    @Override
    protected void handleGet(HttpExchange httpExchange, HandlerResponse response) {
        String mapping = httpExchange.getRequestURI().toString(); // For this handler, will begin with "/users"
        if (mapping.equals("/users")) {
            response.jsonOut.put("users", listUsers());
        } else if (mapping.equals("/users/online")) {
            response.jsonOut.put("online", listUsersOnline());
        } else { // Unsupported request type
            response.jsonOut.put("Error", "Invalid request");
        }
    }

    @Override
    protected void handlePost(HttpExchange httpExchange, JSONObject JSONin, HandlerResponse response) {
        String mapping = httpExchange.getRequestURI().toString(); // For this handler, will begin with "/user"

        // Read various strings that may be present (depending on the mapping)
        String username = readString(JSONin, "username");
        String password = readString(JSONin, "password");
        String token = readString(JSONin, "token");

        // If anything at all goes wrong, we throw an exception and return an error
        try {
            switch (mapping) {
                case "/user/register" -> {
                    if (username == null || password == null) throw new Exception("Invalid parameters");
                    createUser(username, password, response);
                }
                case "/user/login" -> {
                    if (username == null || password == null) throw new Exception("Invalid parameters");
                    loginUser(username, password, response);
                }
                case "/user/logout" -> {
                    if (token == null ) throw new Exception("Missing token");
                    logoutUser(token, response);
                }
                case "/user/online" -> {
                    if (token == null || username == null) throw new Exception("Invalid parameters");
                    userOnline(token, username, response);
                }
                default -> {
                    throw new Exception("No such mapping");
                }
            }
        } catch (Exception e) {
            response.jsonOut.put("Error", e.getMessage());
        }
    }

    private JSONArray listUsers() {
        JSONArray userArray = new JSONArray();
        for (String username : Account.listAccounts()) userArray.put(username);
        return userArray;
    }

    private JSONArray listUsersOnline() {
        JSONArray userArray = new JSONArray();
        for (String username : Client.listClients()) userArray.put(username);
        return userArray;
    }

    private void createUser(String username, String password, HandlerResponse response) throws Exception {
        if (username.length() < 3 && password.length() < 3) {
            throw new Exception("Invalid username or password");
        } else if (Account.exists(username) != null) {
            throw new Exception("Username already in use");
        } else {
            Account newAccount = new Account(username, password);
            Account.add(newAccount);
            response.jsonOut.put("username", username);
        }
    }

    private void loginUser(String username, String password, HandlerResponse response) throws Exception {
        if (username.length() < 3 && password.length() < 3) {
            throw new Exception("Invalid username or password");
        } else {
            Account account = Account.exists(username);
            if (account == null || !account.checkPassword(password)) {
                throw new Exception("Invalid username or password");
            } else {
                String token = Account.getToken();
                Client.add(username, token);
                response.jsonOut.put("token", token);
            }
        }
    }

    private void logoutUser(String token, HandlerResponse response) {
        Client.remove(token);
        response.jsonOut.put("logout", true);
    }

    private void userOnline(String token, String username, HandlerResponse response) throws Exception {
        Client client = Client.findByToken(token);
        if (client == null) throw new Exception("Invalid token");
        response.jsonOut.put("online", Client.findByUsername(username) != null);
    }
}

