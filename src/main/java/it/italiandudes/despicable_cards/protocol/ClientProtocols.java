package it.italiandudes.despicable_cards.protocol;

import it.italiandudes.despicable_cards.data.card.WhiteCardChoice;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

public final class ClientProtocols {

    // Handshake JSONObjects
    public static final class Handshake {
        public static @NotNull JSONObject getRequest(@NotNull final String username, @Nullable final String sha512password) {
            JSONObject json = new JSONObject();
            json.put("username", username);
            if (sha512password != null) json.put("server_password", sha512password);
            return json;
        }
    }

    // Lobby JSONObjects
    public static final class Lobby {
        public static @NotNull JSONObject getLobbyReady(boolean ready) {
            JSONObject json = new JSONObject();
            json.put("ready", ready);
            return json;
        }
    }

    // Game JSONObjects
    public static final class Game {
        public static @NotNull JSONObject getSendWhitecards(WhiteCardChoice... choices) {
            JSONObject json = new JSONObject();
            JSONArray choicesArray = new JSONArray();
            for (WhiteCardChoice choice : choices) {
                JSONObject choiceJson = new JSONObject();
                choiceJson.put("card_id", choice.whiteCard().getUuid());
                choiceJson.put("order_index", choice.orderIndex());
                if (choice.whiteCard().isWildcard()) {
                    choiceJson.put("wildcard_content", choice.wildcardContent());
                }
                choicesArray.put(choiceJson);
            }
            json.put("choices", choicesArray);
            return json;
        }
    }
}
