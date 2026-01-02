package it.italiandudes.despicable_cards.server.server;

import it.italiandudes.despicable_cards.protocol.ServerProtocols;
import it.italiandudes.despicable_cards.server.ServerInstance;
import it.italiandudes.despicable_cards.utils.JSONSerializer;
import it.italiandudes.idl.logger.Logger;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

public final class ServerPlayerHandshakeThread extends Thread {

    // Attributes
    @NotNull private final Socket socket;

    // Constructors
    public ServerPlayerHandshakeThread(@NotNull final Socket socket) {
        this.socket = socket;
        setDaemon(true);
        setName("PlayerHandshakeThread-" + socket);
    }

    // Methods
    public @NotNull Socket getSocket() {
        return socket;
    }

    // Runnable
    @Override
    public void run() {
        try {
            JSONObject request = JSONSerializer.readJSONObject(socket.getInputStream());
            String username = request.getString("username");
            if (ServerInstance.getInstance().getSha512password() != null) { // Password Check
                JSONSerializer.writeJSONObject(socket.getOutputStream(), ServerProtocols.Handshake.getPasswordAlgorithm());
                JSONObject pwdJson = JSONSerializer.readJSONObject(socket.getInputStream());
                String serverPassword = pwdJson.getString("server_password");
                if (!serverPassword.equals(ServerInstance.getInstance().getSha512password())) {
                    JSONSerializer.writeJSONObject(socket.getOutputStream(), ServerProtocols.Handshake.getResponseInvalidPassword());
                    socket.close();
                    return;
                }
            }

            /* TODO: Rejects not implemented yet
            * 1  Lobby Full
            * 2  Username Taken
            * 3  Not In Lobby
            * */

            // Accepting new player
            String playerGuid = UUID.randomUUID().toString();
            JSONObject okResponse = new JSONObject();
            okResponse.put("uuid", playerGuid);
            JSONSerializer.writeJSONObject(socket.getOutputStream(), okResponse);
            new ServerPlayerThread(socket, username, playerGuid).start();
        } catch (IOException e) {
            Logger.log(e);
            try {
                JSONSerializer.writeJSONObject(socket.getOutputStream(), ServerProtocols.Handshake.getResponseUnknownError());
            } catch (Exception ignored) {}
            try {
                socket.close();
            } catch (Exception ignored) {}
        }
    }
}
