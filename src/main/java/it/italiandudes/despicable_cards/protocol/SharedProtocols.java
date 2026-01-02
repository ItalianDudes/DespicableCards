package it.italiandudes.despicable_cards.protocol;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public final class SharedProtocols {

    // Methods
    public static @NotNull JSONObject getAliveRequest() {
        JSONObject json = new JSONObject();
        json.put("alive_request", System.currentTimeMillis());
        return json;
    }
    public static @NotNull JSONObject getAliveResponse(final long requestTimestamp) {
        JSONObject json = new JSONObject();
        json.put("alive_answer", requestTimestamp);
        return json;
    }
    public static @NotNull JSONObject getConnectionClose(@Nullable final String reason) {
        JSONObject json = new JSONObject();
        json.put("close", reason);
        return json;
    }
}
