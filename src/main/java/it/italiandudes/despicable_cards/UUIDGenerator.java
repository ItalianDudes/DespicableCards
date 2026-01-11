package it.italiandudes.despicable_cards;

import java.util.UUID;

public final class UUIDGenerator {
    public static void main(String[] args) {
        for (int i=0; i<100; i++) System.out.println(UUID.randomUUID());
        System.exit(0);
    }
}
