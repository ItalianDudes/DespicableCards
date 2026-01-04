package it.italiandudes.despicable_cards.data.card;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * @param whiteCard Attributes
 */
public record WhiteCardChoice(@NotNull WhiteCard whiteCard, int orderIndex, @Nullable String wildcardContent) {

    // Methods
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof WhiteCardChoice(WhiteCard card, int index, String content))) return false;
        return orderIndex() == index && whiteCard().equals(card) && Objects.equals(wildcardContent(), content);
    }
    @Override
    public int hashCode() {
        int result = whiteCard().hashCode();
        result = 31 * result + orderIndex();
        result = 31 * result + Objects.hashCode(wildcardContent());
        return result;
    }
    @Override @NotNull
    public String toString() {
        return "#" + orderIndex;
    }
}
