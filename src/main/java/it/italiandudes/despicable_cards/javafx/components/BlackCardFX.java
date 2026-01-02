package it.italiandudes.despicable_cards.javafx.components;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.jetbrains.annotations.NotNull;

public class BlackCardFX extends Group {

    // Attributes
    @NotNull
    private final Rectangle card;
    @NotNull private final String content;
    private final int emptyFields;

    // Constructors
    public BlackCardFX(@NotNull final String content, final int emptyFields) {
        this.content = content;
        this.emptyFields = emptyFields;
        card = new Rectangle();
        card.setStyle("-fx-border-radius: 2px;");
        card.setFill(Color.BLACK);
        card.setStroke(Color.WHITE);
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
    public int getEmptyFields() {
        return emptyFields;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlackCardFX that)) return false;
        if (getEmptyFields() != that.getEmptyFields()) return false;
        if (!getCard().equals(that.getCard())) return false;
        return getContent().equals(that.getContent());
    }
    @Override
    public int hashCode() {
        int result = getCard().hashCode();
        result = 31 * result + getContent().hashCode();
        result = 31 * result + getEmptyFields();
        return result;
    }
    @Override @NotNull
    public String toString() {
        return content;
    }
}
