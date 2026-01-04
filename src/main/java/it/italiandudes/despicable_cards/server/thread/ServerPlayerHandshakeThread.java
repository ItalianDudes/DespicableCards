package it.italiandudes.despicable_cards.server.thread;

import it.italiandudes.despicable_cards.data.player.ServerPlayerData;
import it.italiandudes.despicable_cards.protocol.ServerProtocols;
import it.italiandudes.despicable_cards.server.ServerInstance;
import it.italiandudes.despicable_cards.utils.Defs;
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
            if (username.trim().isBlank()) { // Invalid Username Check
                JSONSerializer.writeJSONObject(socket.getOutputStream(), ServerProtocols.Handshake.getResponseInvalidUsername());
                socket.close();
                return;
            }
            if (ServerInstance.getInstance().getAvailableSpace() <= 0) { // Max Players Check
                JSONSerializer.writeJSONObject(socket.getOutputStream(), ServerProtocols.Handshake.getResponseServerFull());
                socket.close();
                return;
            }
            if (ServerInstance.getInstance().getServerStateThread() instanceof ServerGameThread) { // Not in Lobby Check
                JSONSerializer.writeJSONObject(socket.getOutputStream(), ServerProtocols.Handshake.getResponseNotInLobby());
                socket.close();
                return;
            }
            if (ServerInstance.getInstance().getServerPlayerDataManager().getPlayerDataWithUsername(username) != null) { // Username Taken Check
                JSONSerializer.writeJSONObject(socket.getOutputStream(), ServerProtocols.Handshake.getResponseUsernameTaken());
                socket.close();
                return;
            }
            if (ServerInstance.getInstance().getSha512password() != null) { // Password Check
                if (request.has("server_password")) {
                    String serverPassword = request.getString("server_password");
                    if (!serverPassword.equals(ServerInstance.getInstance().getSha512password())) {
                        JSONSerializer.writeJSONObject(socket.getOutputStream(), ServerProtocols.Handshake.getResponseInvalidPassword());
                        socket.close();
                        return;
                    }
                } else {
                    JSONSerializer.writeJSONObject(socket.getOutputStream(), ServerProtocols.Handshake.getResponseInvalidPassword());
                    socket.close();
                    return;
                }
            }

            // Accepting new player
            String playerUuid = UUID.randomUUID().toString();
            JSONObject okResponse = new JSONObject();
            okResponse.put("uuid", playerUuid);
            JSONSerializer.writeJSONObject(socket.getOutputStream(), okResponse);
            ServerPlayerData serverPlayerData = new ServerPlayerData(socket, playerUuid, username, false);
            ServerInstance.getInstance().getServerPlayerDataManager().addAndBroadcast(serverPlayerData);
            JSONSerializer.writeJSONObject(socket.getOutputStream(), ServerProtocols.Lobby.getLobbyPlayersList(ServerInstance.getInstance().getServerPlayerDataManager().getServerPlayersData()));
            new ServerPlayerThread(serverPlayerData).start();
        } catch (IOException e) {
            Logger.log(e, Defs.SERVER_LOGGER_CONTEXT);
            try {
                JSONSerializer.writeJSONObject(socket.getOutputStream(), ServerProtocols.Handshake.getResponseUnknownError());
            } catch (Exception ignored) {}
            try {
                socket.close();
            } catch (Exception ignored) {}
        }
    }
}
