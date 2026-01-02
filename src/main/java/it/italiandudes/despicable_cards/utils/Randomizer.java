package it.italiandudes.despicable_cards.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

@SuppressWarnings("unused")
public final class Randomizer {

    // Attributes
    @NotNull private static final Random RANDOMIZER = new Random();

    // Methods
    @NotNull
    public static Random getRandomizer() {
        return RANDOMIZER;
    }
    /**
     * Return a random integer in range bounds from min (included) to max (excluded).
     * @param min The minimum value included.
     * @param max The maximum value excluded.
     * */
    public static int randomBetween(final int min, final int max) {
        return RANDOMIZER.nextInt(max - min) + min;
    }
}
