package it.italiandudes.despicable_cards.data.card;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.jetbrains.annotations.NotNull;

public class BlackCard /*extends Group*/ {

    // Attributes
    @NotNull private final Rectangle card;
    @NotNull private final String uuid;
    @NotNull private final String content;
    private final int blanks;

    // Constructors
    public BlackCard(@NotNull final String uuid, @NotNull final String content, final int blanks) {
        super();
        this.uuid = uuid;
        this.content = content;
        this.blanks = blanks;
        card = new Rectangle();
        card.setStyle("-fx-border-radius: 2px;");
        card.setFill(Color.BLACK);
        card.setStroke(Color.WHITE);
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
    public int getBlanks() {
        return blanks;
    }
    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof BlackCard blackCard)) return false;

        return getUuid().equals(blackCard.getUuid());
    }
    @Override
    public int hashCode() {
        return getUuid().hashCode();
    }
    @Override @NotNull
    public String toString() {
        return content;
    }
}
