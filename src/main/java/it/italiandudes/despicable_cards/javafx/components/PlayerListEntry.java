package it.italiandudes.despicable_cards.javafx.components;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.jetbrains.annotations.NotNull;

public final class PlayerListEntry {

    // Attributes
    @NotNull private final String name;
    private final BooleanProperty ready = new SimpleBooleanProperty(false);

    // Constructors
    public PlayerListEntry(@NotNull final String name) {
        this.name = name;
    }

    // Methods
    @NotNull
    public String getName() {
        return name;
    }
    public boolean isReady() {
        return ready.get();
    }
    public BooleanProperty readyProperty() { return ready; }
    public void setReady(boolean ready) {
        this.ready.setValue(ready);
    }
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PlayerListEntry that)) return false;
        return isReady() == that.isReady() && getName().equals(that.getName());
    }
    @Override
    public int hashCode() {
        int result = getName().hashCode();
        result = 31 * result + Boolean.hashCode(isReady());
        return result;
    }
    @Override @NotNull
    public String toString() {
        return getName();
    }
}
