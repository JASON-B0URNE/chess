package client;

import com.google.gson.Gson;
import requests.Response;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Locale;
import java.util.Map;

public class ServerFacade {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String baseUrl;

    public ServerFacade(int port) {
        this.baseUrl = String.format(Locale.getDefault(),"http://localhost:" + port + "/");
    }

    public Response clear() throws URISyntaxException, IOException, InterruptedException {
        var request = HttpRequest.newBuilder(new URI(this.baseUrl + "db"))
            .DELETE()
            .build();

        HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return new Response(httpResponse.statusCode(), httpResponse.body());
    }

    public Response createUser(String username, String password, String email) throws URISyntaxException, IOException, InterruptedException {
        var body = Map.of("username", username, "password", password, "email", email);
        var jsonBody = new Gson().toJson(body);

        var request = HttpRequest.newBuilder(new URI(this.baseUrl + "user"))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return new Response(httpResponse.statusCode(), httpResponse.body());
    }

    public Response createSession(String username, String password) throws URISyntaxException, IOException, InterruptedException {
        var body = Map.of("username", username, "password", password);
        var jsonBody = new Gson().toJson(body);

        var request = HttpRequest.newBuilder(new URI(this.baseUrl + "session"))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return new Response(httpResponse.statusCode(), httpResponse.body());
    }

    public Response deleteSession(String authToken) throws URISyntaxException, IOException, InterruptedException {
        var request = HttpRequest.newBuilder(new URI(this.baseUrl + "session"))
                .DELETE()
                .header("Authorization", authToken)
                .build();

        HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return new Response(httpResponse.statusCode(), httpResponse.body());
    }

    public Response getGames(String authToken) throws URISyntaxException, IOException, InterruptedException {
        var request = HttpRequest.newBuilder(new URI(this.baseUrl + "game"))
                .GET()
                .header("Authorization", authToken)
                .build();

        HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return new Response(httpResponse.statusCode(), httpResponse.body());
    }

    public Response createGame(String authToken, String gameName) throws URISyntaxException, IOException, InterruptedException {
        var body = Map.of("gameName", gameName);
        var jsonBody = new Gson().toJson(body);

        var request = HttpRequest.newBuilder(new URI(this.baseUrl + "game"))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Authorization", authToken)
                .build();

        HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return new Response(httpResponse.statusCode(), httpResponse.body());
    }

    public Response joinGame(String authToken, String playerColor, Number gameID) throws URISyntaxException, IOException, InterruptedException {
        var body = Map.of("playerColor", playerColor, "gameID", gameID);
        var jsonBody = new Gson().toJson(body);

        var request = HttpRequest.newBuilder(new URI(this.baseUrl + "game"))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Authorization", authToken)
                .build();

        HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return new Response(httpResponse.statusCode(), httpResponse.body());
    }
}
