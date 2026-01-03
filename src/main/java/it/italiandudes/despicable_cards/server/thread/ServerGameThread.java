package it.italiandudes.despicable_cards.server.thread;

import it.italiandudes.despicable_cards.data.player.ServerPlayerData;
import it.italiandudes.despicable_cards.data.card.BlackCard;
import it.italiandudes.despicable_cards.data.card.WhiteCardChoice;
import it.italiandudes.despicable_cards.protocol.ServerProtocols;
import it.italiandudes.despicable_cards.server.ServerInstance;
import it.italiandudes.despicable_cards.utils.Defs;
import it.italiandudes.despicable_cards.utils.JSONSerializer;
import it.italiandudes.idl.logger.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.Objects;

public final class ServerGameThread extends Thread {

    // Attributes
    @NotNull private final ServerPlayerData masterPlayerData;
    @NotNull private final BlackCard blackCard;
    private final boolean firstRound;

    // Constructors
    public ServerGameThread(@Nullable final ServerPlayerData masterPlayerData, boolean firstRound) {
        this.firstRound = firstRound;
        this.masterPlayerData = Objects.requireNonNullElseGet(masterPlayerData, () -> ServerInstance.getInstance().getServerPlayerDataManager().randomizeServerPlayerData());
        this.blackCard = ServerInstance.getInstance().getBlackcardsPool().getRandomBlackcard();
        setDaemon(true);
        setName("ServerGameThread");
    }
    public ServerGameThread() {
        this(null, true);
    }

    // Methods
    private boolean everyoneSentChoices() {
        for (ServerPlayerData playerData : ServerInstance.getInstance().getServerPlayerDataManager().getServerPlayersData()) {
            if (playerData.getWhiteCardChoices().isEmpty()) return false;
        }
        return true;
    }

    // Runnable
    @Override
    public void run() {

        /*
        * 1.  Reset pools and player whitecards if it's the first round
        * 2.  Clear up previous player whitecards choices
        * 3.  Announce Round (tell master name and black card)
        * 4.  If is the first round, randomize player whitecards
        * 5.  Send whitecards to players (players receive every time the full hand)
        * 6.  Wait for every player choice
        * 7.  Send master the choices
        * 8.  Wait for master winner election
        * 9.  Announce Winner (if round sucks winner=null and next master will be random)
        * 10. Remove used whitecards from players and replenish whitecards up to card limit
        * 11. Back to lobby or next round with master = previous winner (or random if winner=null)
        *
        * */

        try {

            // 1. Reset pools and player whitecards if it's the first round
            if (firstRound) {
                ServerInstance.getInstance().getServerPlayerDataManager().resetAllPlayersWhitecards();
                ServerInstance.getInstance().getWhitecardsPool().resetPool();
                ServerInstance.getInstance().getBlackcardsPool().resetPool();
            }

            // 2. Clear up previous player whitecards choices
            ServerInstance.getInstance().getServerPlayerDataManager().resetAllPlayersWhitecardsChoices();

            // 3. Announce Round
            ServerInstance.getInstance().broadcastMessage(ServerProtocols.Game.getAnnounceRound(masterPlayerData.getUuid(), blackCard));

            // 4. If first round, fully randomize whitecards
            if (firstRound) {
                for (ServerPlayerData playerData : ServerInstance.getInstance().getServerPlayerDataManager().getServerPlayersData()) {
                    playerData.setWhiteCards(ServerInstance.getInstance().getWhitecardsPool().getRandomWhitecardsAmount(Defs.MAX_WHITECARDS));
                }
            }

            // 5. Send whitecards
            for (ServerPlayerData playerData : ServerInstance.getInstance().getServerPlayerDataManager().getServerPlayersData()) {
                JSONSerializer.writeJSONObject(playerData.getSocket().getOutputStream(), ServerProtocols.Game.getGivePlayerWhitecards(playerData.getWhiteCards()));
            }

            // 6. Wait for everyone sending
            while (!isInterrupted()) {
                //noinspection BusyWait
                Thread.sleep(1000);
                if (everyoneSentChoices()) break;
            }

            // 7. Send choices to master
            JSONSerializer.writeJSONObject(masterPlayerData.getSocket().getOutputStream(), ServerProtocols.Game.getSendChoicesToMaster(ServerInstance.getInstance().getServerPlayerDataManager().getServerPlayersData()));

            // 8. Get winner UUID
            JSONObject winnerMessage = JSONSerializer.readJSONObject(masterPlayerData.getSocket().getInputStream());
            ServerPlayerData winnerPlayerData = ServerInstance.getInstance().getServerPlayerDataManager().getServerPlayerDataWithUUID(winnerMessage.getString("winner"));
            String winnerUuid = winnerPlayerData != null ? winnerPlayerData.getUuid() : null;

            // 9. Announce Winner (or not)
            ServerInstance.getInstance().broadcastMessage(ServerProtocols.Game.getAnnounceWinner(winnerUuid, winnerUuid != null ? winnerPlayerData.getWhiteCardChoices() : null, ServerInstance.getInstance().getServerPlayerDataManager().getServerPlayersData()));

            // 10. Remove choices from player whitecards and replenish
            for (ServerPlayerData playerData : ServerInstance.getInstance().getServerPlayerDataManager().getServerPlayersData()) {
                if (playerData.equals(masterPlayerData)) continue;
                playerData.getWhiteCards().removeAll(playerData.getWhiteCardChoices().stream().map(WhiteCardChoice::whiteCard).toList());
                playerData.getWhiteCards().addAll(ServerInstance.getInstance().getWhitecardsPool().getRandomWhitecardsAmount(Defs.MAX_WHITECARDS - playerData.getWhiteCards().size()));
            }

            // 11. TEMPORARY: BACK TO LOBBY | NO NEW ROUND
            ServerInstance.getInstance().broadcastMessage(ServerProtocols.State.getStateLobby());
        } catch (Exception e) {
            Logger.log(e, Defs.SERVER_LOGGER_CONTEXT);
            ServerInstance.stopInstance();
        }
    }
}
