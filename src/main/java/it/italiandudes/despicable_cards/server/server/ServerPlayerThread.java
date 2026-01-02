package it.italiandudes.despicable_cards.server.server;

import org.jetbrains.annotations.NotNull;

import java.net.Socket;

public final class ServerPlayerThread extends Thread {

    // Attributes
    @NotNull private final Socket socket;
    @NotNull private final String username;
    @NotNull private final String uuid;

    // Constructors
    public ServerPlayerThread(@NotNull final Socket socket, @NotNull final String username, @NotNull final String uuid) {
        this.socket = socket;
        this.username = username;
        this.uuid = uuid;
        setDaemon(true);
        setName("PlayerHandshakeThread-" + uuid);
    }

    // Methods
    public @NotNull Socket getSocket() {
        return socket;
    }
    public @NotNull String getUsername() {
        return username;
    }
    public @NotNull String getUuid() {
        return uuid;
    }
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ServerPlayerThread that)) return false;
        return getSocket().equals(that.getSocket()) && getUsername().equals(that.getUsername()) && getUuid().equals(that.getUuid());
    }
    @Override
    public int hashCode() {
        int result = getSocket().hashCode();
        result = 31 * result + getUsername().hashCode();
        result = 31 * result + getUuid().hashCode();
        return result;
    }
    @Override @NotNull
    public String toString() {
        return getName();
    }

    // Runnable
    @Override
    public void run() {
    }
}
