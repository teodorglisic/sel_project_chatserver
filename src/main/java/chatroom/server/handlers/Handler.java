package chatroom.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public abstract class Handler implements HttpHandler  {
    public void handle(HttpExchange httpExchange) throws IOException {
        try (// Get the input and output streams
             BufferedReader in = new BufferedReader(new InputStreamReader(httpExchange.getRequestBody()));
             OutputStreamWriter out = new OutputStreamWriter(httpExchange.getResponseBody())
        ) {
            // Empty response with an optimistic status-code
            HandlerResponse response = new HandlerResponse();

            // Web clients are sending cross-origin, because the client is not running on this server.
            // In that case, the browser sends a pre-flight requests, to ensure that a cross-origin
            // request will be accepted. This is an OPTIONS command, and must be answers with headers
            // that show what cross-origin commands are acceptable.
            if (httpExchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                httpExchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                httpExchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
                httpExchange.sendResponseHeaders(204, -1); // No content for OPTIONS requests
                return;
            }

            // For all other requests, our usual processing
            String requestMethod = httpExchange.getRequestMethod();
            if (requestMethod.equals("GET")) {
                handleGet(httpExchange, response);
            } else if (requestMethod.equals("POST")) {
                JSONObject JSONin = readJSON(in);
                handlePost(httpExchange, JSONin, response);
            } else { // Unsupported request type
                response.statusCode = 418;
                response.jsonOut.put("Error", "Invalid HTTP request method");
            }

            // We include the CORS headers for all normal requests as well,
            // to ensure that web clients are happy.
            httpExchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            httpExchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            httpExchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            // Send the response
            String textOut = response.jsonOut.toString();
            httpExchange.sendResponseHeaders(response.statusCode, textOut.length());
            out.write(textOut);
        }
    }

    /**
     * The handler should override this method, if it supports GET-requests
     */
    protected void handleGet(HttpExchange httpExchange, HandlerResponse response) {
        response.statusCode = 418;
        response.jsonOut.put("Error", "Invalid HTTP request method");
    }

    /**
     * The handler should override this method, if it supports POST-requests
     */
    protected void handlePost(HttpExchange httpExchange, JSONObject JSONin, HandlerResponse response) {
        response.statusCode = 418;
        response.jsonOut.put("Error", "Invalid HTTP request method");
    }

    /**
     * Read the JSON from the input, and place into a JSONObject
     */
    protected JSONObject readJSON(BufferedReader in) {
        JSONObject jsonIn = null;
        try {
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                stringBuilder.append(line);
            }
            String jsonText = stringBuilder.toString();
            jsonIn = new JSONObject(jsonText);
        } catch (Exception e) {
            // If anything goes wrong, return null
        }
        return jsonIn;
    }

    /**
     * Helper method to read a JSON string. Getting exceptions is a nuisance - if a string does
     * not exist, just return null.
     */
    protected String readString(JSONObject obj, String key) {
        try {
            return obj.getString(key);
        } catch (JSONException e) {
            return null;
        }
    }
}
