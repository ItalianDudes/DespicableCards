package it.italiandudes.despicable_cards.data;

import org.jetbrains.annotations.NotNull;

public final class PlayerLobbyInfo {

    // Attributes
    @NotNull private final String uuid;
    @NotNull private final String username;
    private boolean ready;

    // Constructors
    public PlayerLobbyInfo(@NotNull final String uuid, @NotNull final String username, final boolean ready) {
        this.uuid = uuid;
        this.username = username;
        this.ready = ready;
    }

    // Methods
    public @NotNull String getUuid() {
        return uuid;
    }
    public @NotNull String getUsername() {
        return username;
    }
    public boolean isReady() {
        return ready;
    }
    public void setReady(boolean ready) {
        this.ready = ready;
    }
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PlayerLobbyInfo that)) return false;

        return isReady() == that.isReady() && getUuid().equals(that.getUuid()) && getUsername().equals(that.getUsername());
    }
    @Override
    public int hashCode() {
        int result = getUuid().hashCode();
        result = 31 * result + getUsername().hashCode();
        result = 31 * result + Boolean.hashCode(isReady());
        return result;
    }
    @Override @NotNull
    public String toString() {
        return username;
    }
}
