package it.italiandudes.despicable_cards.data.player;

import org.jetbrains.annotations.NotNull;

import java.net.Socket;

public final class ServerPlayerData extends PlayerData {

    // Attributes
    @NotNull private final Socket socket;

    // Constructors
    public ServerPlayerData(@NotNull final Socket socket, @NotNull String uuid, @NotNull String username, boolean ready) {
        super(uuid, username, ready);
        this.socket = socket;
    }

    // Methods
    public @NotNull Socket getSocket() {
        return socket;
    }
}
