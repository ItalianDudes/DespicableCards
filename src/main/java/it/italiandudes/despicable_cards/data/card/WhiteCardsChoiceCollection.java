package it.italiandudes.despicable_cards.data.card;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * @param playerUuid Attributes
 */
public record WhiteCardsChoiceCollection(int collectionNumber, @NotNull String playerUuid,
                                         @NotNull ArrayList<@NotNull WhiteCardChoice> whiteCardChoices) {

    // Constructors
    public WhiteCardsChoiceCollection(final int collectionNumber, @NotNull final String playerUuid, @NotNull final ArrayList<@NotNull WhiteCardChoice> whiteCardChoices) {
        this.playerUuid = playerUuid;
        this.collectionNumber = collectionNumber;
        this.whiteCardChoices = new ArrayList<>(whiteCardChoices.stream().sorted(Comparator.comparingInt(WhiteCardChoice::orderIndex)).toList());
    }

    // Methods
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof WhiteCardsChoiceCollection(int number, String uuid, ArrayList<WhiteCardChoice> cardChoices))) return false;
        return collectionNumber() == number && playerUuid().equals(uuid) && whiteCardChoices().equals(cardChoices);
    }
    @Override
    public int hashCode() {
        int result = playerUuid().hashCode();
        result = 31 * result + whiteCardChoices().hashCode();
        result = 31 * result + collectionNumber();
        return result;
    }
    @Override
    @NotNull
    public String toString() {
        return "#" + collectionNumber();
    }
}
