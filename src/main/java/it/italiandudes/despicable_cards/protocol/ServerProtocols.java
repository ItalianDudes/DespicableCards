package it.italiandudes.despicable_cards.protocol;

import it.italiandudes.despicable_cards.data.player.ServerPlayerData;
import it.italiandudes.despicable_cards.data.card.BlackCard;
import it.italiandudes.despicable_cards.data.card.WhiteCard;
import it.italiandudes.despicable_cards.data.card.WhiteCardChoice;
import it.italiandudes.despicable_cards.data.enums.ServerRejectReason;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class ServerProtocols {

    // Handshake JSONObjects
    public static final class Handshake {
        public static @NotNull JSONObject getResponseServerFull() {
            JSONObject json = new JSONObject();
            json.put("reject", ServerRejectReason.LOBBY_FULL.name());
            return json;
        }
        public static @NotNull JSONObject getResponseInvalidPassword() {
            JSONObject json = new JSONObject();
            json.put("reject", ServerRejectReason.INVALID_PASSWORD.name());
            return json;
        }
        public static @NotNull JSONObject getResponseNotInLobby() {
            JSONObject json = new JSONObject();
            json.put("reject", ServerRejectReason.NOT_IN_LOBBY.name());
            return json;
        }
        public static @NotNull JSONObject getResponseUnknownError() {
            JSONObject json = new JSONObject();
            json.put("reject", ServerRejectReason.UNKNOWN_ERROR.name());
            return json;
        }
        public static @NotNull JSONObject getResponseUsernameTaken() {
            JSONObject json = new JSONObject();
            json.put("reject", ServerRejectReason.USERNAME_TAKEN.name());
            return json;
        }
        public static @NotNull JSONObject getResponseInvalidUsername() {
            JSONObject json = new JSONObject();
            json.put("reject", ServerRejectReason.INVALID_USERNAME.name());
            return json;
        }
        public static @NotNull JSONObject getResponseOk(@NotNull final String uuid) {
            JSONObject json = new JSONObject();
            json.put("uuid", uuid);
            return json;
        }
    }

    // Lobby JSONObjects
    public static final class Lobby {
        public static @NotNull JSONObject getLobbyPlayerJoin(@NotNull final String uuid, @NotNull final String username) {
            JSONObject json = new JSONObject();
            json.put("join", uuid);
            json.put("username", username);
            return json;
        }
        public static @NotNull JSONObject getLobbyPlayerLeft(@NotNull final String uuid) {
            JSONObject json = new JSONObject();
            json.put("left", uuid);
            return json;
        }
        public static @NotNull JSONObject getLobbyPlayersList(@NotNull final List<@NotNull ServerPlayerData> playersData) {
            JSONObject json = new JSONObject();
            JSONArray playersArray = new JSONArray();
            for (ServerPlayerData playerData : playersData) {
                JSONObject player = new JSONObject();
                player.put("player", playerData.getUuid());
                player.put("username", playerData.getUsername());
                player.put("ready", playerData.isReady());
                playersArray.put(player);
            }
            json.put("players", playersArray);
            return json;
        }
        public static @NotNull JSONObject getLobbyPlayerReadyChange(@NotNull final String uuid, boolean ready) {
            JSONObject json = new JSONObject();
            json.put("player", uuid);
            json.put("ready", ready);
            return json;
        }
    }

    // Game JSONObjects
    public static final class Game {
        public static @NotNull JSONObject getAnnounceRound(@NotNull final String masterUuid, @NotNull final BlackCard blackCard) {
            JSONObject json = new JSONObject();
            json.put("master", masterUuid);
            JSONObject jsonCard = new JSONObject();
            jsonCard.put("card_id", blackCard.getUuid());
            jsonCard.put("content", blackCard.getContent());
            jsonCard.put("blanks", blackCard.getBlanks());
            json.put("blackcard", jsonCard);
            return json;
        }
        public static @NotNull JSONObject getSendChoicesToMaster(@NotNull final ArrayList<@NotNull ServerPlayerData> serverPlayersData) {
            JSONObject json = new JSONObject();
            JSONArray playersChoicesArray = new JSONArray();
            for (ServerPlayerData playerData : serverPlayersData) {
                JSONObject combination = new JSONObject();
                combination.put("player", playerData.getUuid());
                JSONArray combinationArray = new JSONArray();
                for (WhiteCardChoice choice : playerData.getWhiteCardChoices()) {
                    JSONObject choiceJSON = new JSONObject();
                    choiceJSON.put("order_index", choice.orderIndex());
                    choiceJSON.put("card_id", choice.whiteCard().getUuid());
                    choiceJSON.put("wildcard", choice.whiteCard().isWildcard());
                    choiceJSON.put("content", choice.whiteCard().getContent());
                    if (choice.whiteCard().isWildcard()) choiceJSON.put("wildcard_content", choice.wildcardContent());
                    combinationArray.put(choiceJSON);
                }
                combination.put("combination", combinationArray);
            }
            json.put("players_choices", playersChoicesArray);
            return json;
        }
        public static @NotNull JSONObject getPlayerLeft(@NotNull final String playerUuid, boolean cardMaster) {
            JSONObject json = new JSONObject();
            json.put("left", playerUuid);
            json.put("card_master", cardMaster);
            return json;
        }
        public static @NotNull JSONObject getGivePlayerWhitecards(@NotNull final ArrayList<@NotNull WhiteCard> whiteCards) {
            JSONObject json = new JSONObject();
            JSONArray cards = new JSONArray();
            for (WhiteCard whiteCard : whiteCards) {
                JSONObject jsonCard = new JSONObject();
                jsonCard.put("card_id", whiteCard.getUuid());
                jsonCard.put("content", whiteCard.getContent());
                jsonCard.put("wildcard", whiteCard.isWildcard());
                cards.put(jsonCard);
            }
            json.put("whitecards", cards);
            return json;
        }
        public static @NotNull JSONObject getAnnounceWinner(@Nullable final String winnerUuid, @NotNull final ArrayList<ServerPlayerData> serverPlayersData) {
            JSONObject json = new JSONObject();
            json.put("winner", winnerUuid);
            JSONArray playersChoicesArray = new JSONArray();
            for (ServerPlayerData playerData : serverPlayersData) {
                JSONObject combination = new JSONObject();
                combination.put("player", playerData.getUuid());
                JSONArray combinationArray = new JSONArray();
                for (WhiteCardChoice choice : playerData.getWhiteCardChoices()) {
                    JSONObject choiceJSON = new JSONObject();
                    choiceJSON.put("order_index", choice.orderIndex());
                    choiceJSON.put("card_id", choice.whiteCard().getUuid());
                    choiceJSON.put("wildcard", choice.whiteCard().isWildcard());
                    choiceJSON.put("content", choice.whiteCard().getContent());
                    if (choice.whiteCard().isWildcard()) choiceJSON.put("wildcard_content", choice.wildcardContent());
                    combinationArray.put(choiceJSON);
                }
                combination.put("combination", combinationArray);
            }
            json.put("players_choices", playersChoicesArray);
            return json;
        }
    }

    // State JSONObjects
    public static final class State {
        public static @NotNull JSONObject getStateClose() {
            JSONObject json = new JSONObject();
            json.put("state", "close");
            return json;
        }
        public static @NotNull JSONObject getStateGame() {
            JSONObject json = new JSONObject();
            json.put("state", "game");
            return json;
        }
        public static @NotNull JSONObject getStateLobby() {
            JSONObject json = new JSONObject();
            json.put("state", "lobby");
            return json;
        }
    }
}
