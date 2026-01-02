package it.italiandudes.despicable_cards.javafx.components;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.jetbrains.annotations.NotNull;

public class WhiteCardFX extends Group {

    // Attributes
    @NotNull private final Rectangle card;
    @NotNull private final String content;
    private final boolean isWildcard;

    // Constructors
    public WhiteCardFX(@NotNull final String content, final boolean isWildcard) {
        super();
        this.content = content;
        this.isWildcard = isWildcard;
        card = new Rectangle();
        card.setStyle("-fx-border-radius: 2px;");
        card.setFill(Color.WHITE);
        card.setStroke(Color.BLACK);
    }
    public WhiteCardFX(@NotNull final String content) {
        this(content, false);
    }

    // Methods
    @NotNull
    public Rectangle getCard() {
        return card;
    }
    @NotNull
    public String getContent() {
        return content;
    }
    public boolean isWildcard() {
        return isWildcard;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WhiteCardFX whiteCard)) return false;
        if (isWildcard() != whiteCard.isWildcard()) return false;
        if (!getCard().equals(whiteCard.getCard())) return false;
        return getContent().equals(whiteCard.getContent());
    }
    @Override
    public int hashCode() {
        int result = getCard().hashCode();
        result = 31 * result + getContent().hashCode();
        result = 31 * result + (isWildcard() ? 1 : 0);
        return result;
    }
    @Override @NotNull
    public String toString() {
        return content;
    }
}
