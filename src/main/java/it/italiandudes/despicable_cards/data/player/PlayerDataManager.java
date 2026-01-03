package it.italiandudes.despicable_cards.data.player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public final class PlayerDataManager {

    // Attributes
    @NotNull private final ArrayList<@NotNull PlayerData> playersData = new ArrayList<>();

    // Methods
    public @NotNull ArrayList<@NotNull PlayerData> getPlayersData() {
        return playersData;
    }
    public @Nullable PlayerData getPlayerDataWithUUID(@NotNull final String uuid) {
        for (PlayerData playerData : playersData) {
            if (playerData.getUuid().equals(uuid)) return playerData;
        }
        return null;
    }
    public void clear() {
        playersData.clear();
    }
    public void add(@NotNull final PlayerData playerData) {
        if (!playersData.contains(playerData)) playersData.add(playerData);
    }
    public @Nullable PlayerData getPlayerDataWithUsername(@NotNull final String username) {
        for (PlayerData playerData : playersData) {
            if (playerData.getUsername().equals(username)) return playerData;
        }
        return null;
    }
    public void remove(@NotNull final PlayerData playerData) {
        playersData.remove(playerData);
    }
    public void removePlayerDataWithUUID(@NotNull final String uuid) {
        for (PlayerData playerData : playersData) {
            if (playerData.getUuid().equals(uuid)) {
                playersData.remove(playerData);
                return;
            }
        }
    }
}
