package it.italiandudes.despicable_cards.server;

import it.italiandudes.despicable_cards.data.player.ServerPlayerData;
import it.italiandudes.despicable_cards.data.player.ServerPlayerDataManager;
import it.italiandudes.despicable_cards.protocol.SharedProtocols;
import it.italiandudes.despicable_cards.server.pools.BlackcardsPool;
import it.italiandudes.despicable_cards.server.pools.WhitecardsPool;
import it.italiandudes.despicable_cards.server.thread.ServerListenerThread;
import it.italiandudes.despicable_cards.server.thread.ServerLobbyThread;
import it.italiandudes.despicable_cards.utils.JSONSerializer;
import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public final class ServerInstance {

    // Attributes
    @Nullable private final String sha512password;
    private final int port;
    @NotNull private final ServerListenerThread listener;
    @NotNull private final ServerPlayerDataManager serverPlayerDataManager = new ServerPlayerDataManager();
    @NotNull private Thread serverStateThread;

    // Constructors
    private ServerInstance(final int port, @Nullable final String password) {
        this.port = port;
        if (password == null) this.sha512password = null;
        else this.sha512password = DigestUtils.sha512Hex(password);
        this.listener = new ServerListenerThread(port);
        this.listener.start();
        this.serverStateThread = new ServerLobbyThread();
        this.serverStateThread.start();
    }

    // Util Methods
    public void broadcastMessage(JSONObject message) throws IOException {
        for (ServerPlayerData playerData : serverPlayerDataManager.getServerPlayersData()) {
            JSONSerializer.writeJSONObject(playerData.getSocket().getOutputStream(), message);
        }
    }

    // Methods
    public @NotNull WhitecardsPool getWhitecardsPool() { // TODO
        return new WhitecardsPool(new ArrayList<>());
    }
    public @NotNull BlackcardsPool getBlackcardsPool() { // TODO
        return new BlackcardsPool(new ArrayList<>());
    }
    public @Nullable String getSha512password() {
        return sha512password;
    }
    public int getPort() {
        return port;
    }
    public @NotNull ServerListenerThread getListener() {
        return listener;
    }
    public @NotNull ServerPlayerDataManager getServerPlayerDataManager() {
        return serverPlayerDataManager;
    }
    public @NotNull Thread getServerStateThread() {
        return serverStateThread;
    }
    public void changeServerStateThread(@NotNull final Thread serverStateThread) {
        this.serverStateThread.interrupt();
        this.serverStateThread = serverStateThread;
        this.serverStateThread.start();
    }
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ServerInstance that)) return false;

        return getPort() == that.getPort() && Objects.equals(getSha512password(), that.getSha512password()) && getListener().equals(that.getListener());
    }
    @Override
    public int hashCode() {
        int result = Objects.hashCode(getSha512password());
        result = 31 * result + getPort();
        result = 31 * result + getListener().hashCode();
        return result;
    }
    @Override @NotNull
    public String toString() {
        return listener.toString();
    }

    // Instance
    private static ServerInstance INSTANCE = null;
    public static void stopInstance() {
        if (INSTANCE == null) return;
        INSTANCE.listener.interrupt();
        for (ServerPlayerData playerData : INSTANCE.serverPlayerDataManager.getServerPlayersData()) {
            try {
                JSONSerializer.writeJSONObject(playerData.getSocket().getOutputStream(), SharedProtocols.getConnectionClose("Server closed."));
            } catch (Exception ignored) {}
            try {
                playerData.getSocket().close();
            } catch (Exception ignored) {}
            INSTANCE.serverPlayerDataManager.getServerPlayersData().remove(playerData);
        }
        INSTANCE.serverStateThread.interrupt();
        INSTANCE = null;
    }
    public static void newInstance(int port, @Nullable final String password) {
        if (INSTANCE != null) {
            throw new RuntimeException("Can't create a new ServerInstance while there is already one active.");
        }
        INSTANCE = new ServerInstance(port, password);
    }
    public static ServerInstance getInstance() {
        return INSTANCE;
    }
}
