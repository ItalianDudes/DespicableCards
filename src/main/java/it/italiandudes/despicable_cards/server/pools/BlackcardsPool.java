package it.italiandudes.despicable_cards.server.pools;

import it.italiandudes.despicable_cards.data.card.BlackCard;
import it.italiandudes.despicable_cards.utils.Randomizer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public final class BlackcardsPool {

    // Attributes
    @NotNull private final ArrayList<@NotNull BlackCard> availableBlackcards = new ArrayList<>();
    @NotNull private final ArrayList<@NotNull BlackCard> usedBlackcards = new ArrayList<>();

    // Constructors
    public BlackcardsPool(@NotNull final ArrayList<@NotNull BlackCard> availableBlackcards) {
        this.availableBlackcards.addAll(availableBlackcards);
    }

    // Methods
    public void resetPool() {
        availableBlackcards.addAll(usedBlackcards);
        usedBlackcards.clear();
    }
    public @NotNull BlackCard getRandomBlackcard() {
        if (availableBlackcards.isEmpty()) resetPool();
        if (availableBlackcards.isEmpty()) throw new RuntimeException("Blackcards pool is empty!");
        BlackCard blackCard = availableBlackcards.get(Randomizer.randomBetween(0, availableBlackcards.size()));
        availableBlackcards.remove(blackCard);
        usedBlackcards.add(blackCard);
        return blackCard;
    }
}
