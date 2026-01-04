package it.italiandudes.despicable_cards;

import it.italiandudes.despicable_cards.data.card.BlackCard;
import it.italiandudes.despicable_cards.data.card.WhiteCard;
import it.italiandudes.despicable_cards.db.DBDataHandler;
import it.italiandudes.despicable_cards.db.DBManager;
import it.italiandudes.despicable_cards.db.KeyParameters;
import it.italiandudes.despicable_cards.utils.Defs;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.UUID;

public final class PackCreator {

    // Constants
    private static final String FILE_TERMINATOR = "~END_FILE";

    // Main Method
    public static void main(String[] args) throws FileNotFoundException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Benvenuto nel sistema guidato di creazione pacchetti di DespicableCards.");
        System.out.println("AVVISO: QUESTO PROGRAMMA NON E' ANCORA CAPACE DI RICONOSCERE LE WILDCARDS!");
        System.out.println("REQUISITI: 2 file in formato TXT nella stessa cartella del jar chiamati \"whitecards.txt\" e \"blackcards.txt\"");
        System.out.println("REQUISITI: Ogni file deve avere come ultima riga " + FILE_TERMINATOR);
        System.out.println("Inserisci il carattere o la sequenza di caratteri che identificano i campi vuoti nelle carte nere:");
        System.out.print("Sequenza di Escape: ");
        String escapeSequence = scanner.nextLine();
        System.out.println("Sequenza Letta: \"" + escapeSequence + "\". Premere invio per continuare, altrimenti chiudere il programma.");
        scanner.nextLine();
        System.out.println("Controllo esistenza file...");
        File whiteFile = new File(new File(Defs.JAR_POSITION).getParent() + File.separator + "whitecards.txt");
        File blackFile = new File(new File(Defs.JAR_POSITION).getParent() + File.separator + "blackcards.txt");
        if (!whiteFile.exists() || !whiteFile.isFile()) {
            System.out.println("File \"whitecards.txt\" non trovato.");
            return;
        }
        if (!blackFile.exists() || !blackFile.isFile()) {
            System.out.println("File \"blackcards.txt\" non trovato.");
            return;
        }
        ArrayList<WhiteCard> whiteCards = new ArrayList<>();
        System.out.println("Lettura carte bianche...");
        try (Scanner fileReader = new Scanner(whiteFile)) {
            String line;
            do {
                line = fileReader.nextLine();
                whiteCards.add(new WhiteCard(UUID.randomUUID().toString(), line, false));
            } while (!line.equals(FILE_TERMINATOR));
        }
        System.out.println("Sono state indicizzate " + whiteCards.size() + " carte bianche.");

        ArrayList<BlackCard> blackCards = new ArrayList<>();
        System.out.println("Lettura carte bianche...");
        try (Scanner fileReader = new Scanner(whiteFile)) {
            String line;
            do {
                line = fileReader.nextLine();
                int blanks = getBlanksInLine(line, escapeSequence);
                blackCards.add(new BlackCard(UUID.randomUUID().toString(), line, blanks));
            } while (!line.equals(FILE_TERMINATOR));
        }
        System.out.println("Sono state indicizzate " + blackCards.size() + " carte nere.\n");

        System.out.println("Che titolo vuoi dare al pacchetto di carte?");
        System.out.print("Titolo: ");
        String title = scanner.nextLine();
        String dbFilename = title.trim() + Defs.DB_EXTENSION;
        System.out.println("Verra' dato internamente il titolo: \"" + title + "\". Il file creato si chiamera' \"" + dbFilename + "\". Per continuare premere invio, altrimenti chiudere il programma.");
        scanner.nextLine();

        try {
            System.out.println("Creazione database...");
            DBManager.createDB(dbFilename);
            System.out.println("Database creato!");
            System.out.println("Inserendo titolo...");
            DBDataHandler.writeKeyParameter(KeyParameters.DB_TITLE, title);
            System.out.println("Titolo inserito!");

            System.out.println("Inserendo carte bianche...");
            String query = "INSERT INTO whitecards (uuid, content, is_wildcard) VALUES (?, ?, ?);";
            try (PreparedStatement ps = DBManager.preparedStatement(query)) {
                if (ps == null) throw new SQLException("DB CONNECTION IS NULL");
                for (WhiteCard whiteCard : whiteCards) {
                    ps.setString(1, whiteCard.getUuid());
                    ps.setString(2, whiteCard.getContent());
                    ps.setInt(3, whiteCard.isWildcard() ? 1 : 0);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            System.out.println("Carte bianche inserite!");

            System.out.println("Inserendo carte nere...");
            query = "INSERT INTO blackcards (uuid, content, blanks) VALUES (?, ?, ?);";
            try (PreparedStatement ps = DBManager.preparedStatement(query)) {
                if (ps == null) throw new SQLException("DB CONNECTION IS NULL");
                for (BlackCard blackCard : blackCards) {
                    ps.setString(1, blackCard.getUuid());
                    ps.setString(2, blackCard.getContent());
                    ps.setInt(3, blackCard.getBlanks());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            System.out.println("Carte nere inserite!");

            System.out.println("Chiudendo connessione col database...");
            DBManager.closeConnection();
            System.out.println("Connessione chiusa correttamente!");
        } catch (Exception e) {
            System.err.println("ERRORE DATABASE");
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            DBManager.closeConnection();
        }
        System.out.println("---PROGRAMMA TERMINATO---");
        System.exit(0);
    }

    // Methods
    private static int getBlanksInLine(@NotNull final String line, @NotNull final String sequence) {
        if (line.isBlank() || sequence.isBlank()) return 0;
        int count = 0;
        int index = 0;
        while ((index = line.indexOf(sequence, index)) != -1) {
            count++;
            index++;
        }
        return count;
    }
}
