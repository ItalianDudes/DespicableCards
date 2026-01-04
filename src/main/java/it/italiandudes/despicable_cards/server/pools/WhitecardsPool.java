package it.italiandudes.despicable_cards.server.pools;

import it.italiandudes.despicable_cards.data.card.WhiteCard;
import it.italiandudes.despicable_cards.utils.Randomizer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Optional;

public final class WhitecardsPool {

    // Attributes
    @NotNull private final ArrayList<@NotNull WhiteCard> availableWhitecards = new ArrayList<>();
    @NotNull private final ArrayList<@NotNull WhiteCard> usedWhitecards = new ArrayList<>();

    // Constructors
    public WhitecardsPool(@NotNull final ArrayList<@NotNull WhiteCard> availableWhitecards) {
        this.availableWhitecards.addAll(availableWhitecards);
    }

    // Methods
    public void resetPool() {
        availableWhitecards.addAll(usedWhitecards);
        usedWhitecards.clear();
    }
    public @Nullable WhiteCard getWhitecardFromUUID(@NotNull final String uuid) {
        Optional<WhiteCard> availableOpt = availableWhitecards.stream().filter(card -> card.getUuid().equals(uuid)).findFirst();
        if (availableOpt.isPresent()) return availableOpt.get();
        Optional<WhiteCard> usedOpt = usedWhitecards.stream().filter(card -> card.getUuid().equals(uuid)).findFirst();
        return usedOpt.orElse(null);
    }
    public @NotNull ArrayList<@NotNull WhiteCard> getRandomWhitecardsAmount(final int amount) {
        if (availableWhitecards.size() < amount) resetPool();
        if (availableWhitecards.size() < amount) throw new RuntimeException("Not enough cards in pool!");
        ArrayList<@NotNull WhiteCard> whiteCards = new ArrayList<>();
        for (int i=0; i<amount; i++) {
            WhiteCard whiteCard = availableWhitecards.get(Randomizer.randomBetween(0, availableWhitecards.size()));
            whiteCards.add(whiteCard);
            availableWhitecards.remove(whiteCard);
            usedWhitecards.add(whiteCard);
        }
        return whiteCards;
    }
}
