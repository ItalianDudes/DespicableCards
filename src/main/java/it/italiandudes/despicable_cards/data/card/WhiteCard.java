package it.italiandudes.despicable_cards.data.card;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.jetbrains.annotations.NotNull;

public class WhiteCard extends Group {

    // Attributes
    @NotNull private final Rectangle card;
    @NotNull private final String uuid;
    @NotNull private final String content;
    private final boolean isWildcard;

    // Constructors
    public WhiteCard(@NotNull final String uuid, @NotNull final String content, final boolean isWildcard) {
        super();
        this.uuid = uuid;
        this.content = content;
        this.isWildcard = isWildcard;
        card = new Rectangle();
        card.setStyle("-fx-border-radius: 2px;");
        card.setFill(Color.WHITE);
        card.setStroke(Color.BLACK);
    }
    public WhiteCard(@NotNull final String uuid, @NotNull final String content) {
        this(uuid, content, false);
    }

    // Methods
    public @NotNull String getUuid() {
        return uuid;
    }
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
    public final boolean equals(Object o) {
        if (!(o instanceof WhiteCard whiteCard)) return false;

        return isWildcard() == whiteCard.isWildcard() && getCard().equals(whiteCard.getCard()) && getUuid().equals(whiteCard.getUuid()) && getContent().equals(whiteCard.getContent());
    }
    @Override
    public int hashCode() {
        int result = getCard().hashCode();
        result = 31 * result + getUuid().hashCode();
        result = 31 * result + getContent().hashCode();
        result = 31 * result + Boolean.hashCode(isWildcard());
        return result;
    }
    @Override @NotNull
    public String toString() {
        return content;
    }
}
