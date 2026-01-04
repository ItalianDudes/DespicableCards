package it.italiandudes.despicable_cards.data.player;

import it.italiandudes.despicable_cards.protocol.ServerProtocols;
import it.italiandudes.despicable_cards.server.ServerInstance;
import it.italiandudes.despicable_cards.utils.Randomizer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public final class ServerPlayerDataManager {

    // Attributes
    @NotNull private final ArrayList<@NotNull ServerPlayerData> serverPlayersData = new ArrayList<>();

    // Methods
    public @NotNull ArrayList<@NotNull ServerPlayerData> getServerPlayersData() {
        return serverPlayersData;
    }
    public @Nullable ServerPlayerData getServerPlayerDataWithUUID(@NotNull final String uuid) {
        for (ServerPlayerData playerData : serverPlayersData) {
            if (playerData.getUuid().equals(uuid)) return playerData;
        }
        return null;
    }
    public synchronized void shuffleServerPlayersData() {
        Collections.shuffle(serverPlayersData);
    }
    public void resetReadyStateForPlayers() {
        for (ServerPlayerData serverPlayerData : serverPlayersData) {
            serverPlayerData.setReady(false);
        }
    }
    public void resetAllPlayersWhitecards() {
        for (ServerPlayerData playerData : serverPlayersData) {
            playerData.getWhiteCards().clear();
        }
    }
    public void resetAllPlayersWhitecardsChoices() {
        for (ServerPlayerData playerData : serverPlayersData) {
            playerData.getWhiteCardChoices().clear();
        }
    }
    public @NotNull ServerPlayerData randomizeServerPlayerData() {
        return serverPlayersData.get(Randomizer.randomBetween(0, serverPlayersData.size()));
    }
    public int size() {
        return serverPlayersData.size();
    }
    public void clear() {
        serverPlayersData.clear();
    }
    public void addAndBroadcast(@NotNull final ServerPlayerData playerData) throws IOException {
        if (!serverPlayersData.contains(playerData)) {
            serverPlayersData.add(playerData);
            ServerInstance.getInstance().broadcastMessage(ServerProtocols.Lobby.getLobbyPlayerJoin(playerData.getUuid(), playerData.getUsername()));
        }
    }
    public @Nullable ServerPlayerData getPlayerDataWithUsername(@NotNull final String username) {
        for (ServerPlayerData playerData : serverPlayersData) {
            if (playerData.getUsername().equals(username)) return playerData;
        }
        return null;
    }
    public void removeAndBroadcast(@NotNull final ServerPlayerData playerData) throws IOException {
        serverPlayersData.remove(playerData);
        ServerInstance.getInstance().broadcastMessage(ServerProtocols.Lobby.getLobbyPlayerLeft(playerData.getUuid()));
    }
    public void removePlayerDataWithUUID(@NotNull final String uuid) {
        for (ServerPlayerData playerData : serverPlayersData) {
            if (playerData.getUuid().equals(uuid)) {
                serverPlayersData.remove(playerData);
                return;
            }
        }
    }
}
