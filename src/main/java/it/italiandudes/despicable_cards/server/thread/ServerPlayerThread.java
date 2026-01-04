package it.italiandudes.despicable_cards.server.thread;

import it.italiandudes.despicable_cards.data.card.WhiteCard;
import it.italiandudes.despicable_cards.data.card.WhiteCardChoice;
import it.italiandudes.despicable_cards.data.player.ServerPlayerData;
import it.italiandudes.despicable_cards.protocol.ServerProtocols;
import it.italiandudes.despicable_cards.protocol.SharedProtocols;
import it.italiandudes.despicable_cards.server.ServerInstance;
import it.italiandudes.despicable_cards.utils.Defs;
import it.italiandudes.despicable_cards.utils.JSONSerializer;
import it.italiandudes.idl.logger.Logger;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public final class ServerPlayerThread extends Thread {

    // Attributes
    @NotNull private final ServerPlayerData serverPlayerData;

    // Constructors
    public ServerPlayerThread(@NotNull final ServerPlayerData serverPlayerData) {
        this.serverPlayerData = serverPlayerData;
        setDaemon(true);
        setName("PlayerThread-" + serverPlayerData.getUuid());
    }

    // Methods
    public @NotNull ServerPlayerData getServerPlayerData() {
        return serverPlayerData;
    }
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ServerPlayerThread that)) return false;

        return getServerPlayerData().equals(that.getServerPlayerData());
    }
    @Override
    public int hashCode() {
        return getServerPlayerData().hashCode();
    }
    @Override @NotNull
    public String toString() {
        return getName();
    }

    // Runnable
    @Override
    public void run() {
        try {
            while (!isInterrupted() && !serverPlayerData.getSocket().isClosed()) {
                JSONObject message = JSONSerializer.readJSONObject(serverPlayerData.getSocket().getInputStream());

                if (message.has("alive_request")) { // TODO: possibile hang, forse c'è da creare un socket secondario solo per alive_request
                    JSONSerializer.writeJSONObject(serverPlayerData.getSocket().getOutputStream(), SharedProtocols.getAliveResponse(message.getLong("alive_request")));
                } else if (message.has("close")) {
                    ServerInstance.getInstance().getServerPlayerDataManager().removeAndBroadcast(serverPlayerData);
                    serverPlayerData.getSocket().close();
                    return;
                } else if (message.has("ready")) {
                    serverPlayerData.setReady(message.getBoolean("ready"));
                    ServerInstance.getInstance().broadcastMessage(ServerProtocols.Lobby.getLobbyPlayerReadyChange(serverPlayerData.getUuid(), serverPlayerData.isReady()));
                } else if (message.has("choices")) {
                    ArrayList<WhiteCardChoice> cardChoices = new ArrayList<>();
                    JSONArray cardChoicesArray = message.getJSONArray("choices");
                    for (int i=0; i<cardChoicesArray.length(); i++) {
                        JSONObject choice = cardChoicesArray.getJSONObject(i);
                        String wildcardContent = null;
                        String cardUuid = choice.getString("card_id");
                        WhiteCard whiteCard = ServerInstance.getInstance().getWhitecardsPool().getWhitecardFromUUID(cardUuid);
                        if (whiteCard == null) throw new RuntimeException("No whitecard found with uuid #" + cardUuid);
                        // TODO: aggiungere a questo if un controllo per verificare che la carta sia davvero una wildcard tramite check uuid
                        if (choice.has("wildcard_content")) wildcardContent = choice.getString("wildcard_content");
                        cardChoices.add(new WhiteCardChoice(whiteCard, choice.getInt("order_index"), wildcardContent));
                    }
                    serverPlayerData.setWhiteCardChoices(cardChoices);
                } else if (message.has("winner") && ServerInstance.getInstance().getServerStateThread() instanceof ServerGameThread gameThread && gameThread.getMasterPlayerData().equals(serverPlayerData)) {
                    String winnerUuid = message.getString("winner");
                    ServerPlayerData winnerData = winnerUuid != null ? ServerInstance.getInstance().getServerPlayerDataManager().getServerPlayerDataWithUUID(winnerUuid) : null;
                    if (serverPlayerData.equals(winnerData)) winnerData = null;
                    ServerInstance.getInstance().broadcastMessage(ServerProtocols.Game.getAnnounceWinner(
                            winnerData != null ? winnerData.getUuid() : null,
                            ServerInstance.getInstance().getServerPlayerDataManager().getServerPlayersData())
                    );
                    gameThread.winnerAnnounced();
                }
            }
        } catch (Exception e) {
            Logger.log(e, Defs.SERVER_LOGGER_CONTEXT);
            try {
                ServerInstance.getInstance().getServerPlayerDataManager().removeAndBroadcast(serverPlayerData);
            } catch (Exception ignored) {}
            try {
                JSONSerializer.writeJSONObject(serverPlayerData.getSocket().getOutputStream(), SharedProtocols.getConnectionClose(e.getMessage()));
            } catch (Exception ignored) {}
            try {
                serverPlayerData.getSocket().close();
            } catch (Exception ignored) {}
        }
    }
}
