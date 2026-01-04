package it.italiandudes.despicable_cards.data.player;

import it.italiandudes.despicable_cards.data.card.WhiteCard;
import it.italiandudes.despicable_cards.data.card.WhiteCardChoice;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class PlayerData {

    // Attributes
    @NotNull private final String uuid;
    @NotNull private final String username;
    private boolean ready;
    @NotNull private final ArrayList<@NotNull WhiteCard> whiteCards = new ArrayList<>();
    @NotNull private final ArrayList<@NotNull WhiteCardChoice> whiteCardChoices = new ArrayList<>();

    // Constructors
    public PlayerData(@NotNull final String uuid, @NotNull final String username, boolean ready) {
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
    public @NotNull ArrayList<@NotNull WhiteCardChoice> getWhiteCardChoices() {
        return whiteCardChoices;
    }
    public void setWhiteCardChoices(@NotNull final ArrayList<@NotNull WhiteCardChoice> whiteCardChoices) {
        this.whiteCardChoices.clear();
        this.whiteCardChoices.addAll(whiteCardChoices);
    }
    public @NotNull ArrayList<@NotNull WhiteCard> getWhiteCards() {
        return whiteCards;
    }
    public void setWhiteCards(@NotNull final ArrayList<@NotNull WhiteCard> whiteCards) {
        this.whiteCards.clear();
        this.whiteCards.addAll(whiteCards);
    }
    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof PlayerData that)) return false;
        return getUuid().equals(that.getUuid());
    }
    @Override
    public int hashCode() {
        return getUuid().hashCode();
    }
    @Override @NotNull
    public String toString() {
        return username;
    }
}
