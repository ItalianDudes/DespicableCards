package it.italiandudes.despicable_cards.server;

import it.italiandudes.despicable_cards.data.card.BlackCard;
import it.italiandudes.despicable_cards.data.card.WhiteCard;
import it.italiandudes.despicable_cards.data.player.ServerPlayerData;
import it.italiandudes.despicable_cards.data.player.ServerPlayerDataManager;
import it.italiandudes.despicable_cards.db.DBDataHandler;
import it.italiandudes.despicable_cards.db.DBManager;
import it.italiandudes.despicable_cards.db.KeyParameters;
import it.italiandudes.despicable_cards.exceptions.NotEnoughBlackcardsException;
import it.italiandudes.despicable_cards.exceptions.NotEnoughWhitecardsException;
import it.italiandudes.despicable_cards.protocol.SharedProtocols;
import it.italiandudes.despicable_cards.server.pools.BlackcardsPool;
import it.italiandudes.despicable_cards.server.pools.WhitecardsPool;
import it.italiandudes.despicable_cards.server.thread.ServerListenerThread;
import it.italiandudes.despicable_cards.server.thread.ServerLobbyThread;
import it.italiandudes.despicable_cards.utils.Defs;
import it.italiandudes.despicable_cards.utils.JSONSerializer;
import it.italiandudes.idl.logger.Logger;
import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;

public final class ServerInstance {

    // Attributes
    @Nullable private final String sha512password;
    private final int port;
    private final int maxPlayers;
    private final int maxRounds;
    @NotNull private final ServerListenerThread listener;
    @NotNull private final ServerPlayerDataManager serverPlayerDataManager = new ServerPlayerDataManager();
    @NotNull private Thread serverStateThread;
    @NotNull private final WhitecardsPool whitecardsPool;
    @NotNull private final BlackcardsPool blackcardsPool;

    // Constructors
    private ServerInstance(final int port, final int maxPlayers, final int maxRounds, @Nullable final String password) throws IOException, NotEnoughWhitecardsException, NotEnoughBlackcardsException {
        this.port = port;
        if (maxPlayers >= Defs.MIN_PLAYER_LIMIT && maxPlayers <= Defs.MAX_PLAYERS_LIMIT) this.maxPlayers = maxPlayers;
        else this.maxPlayers = Defs.MAX_PLAYERS_LIMIT;
        this.maxRounds = Math.min(Math.max(maxRounds, 1), Defs.MAX_ROUNDS_LIMIT);
        if (password == null) this.sha512password = null;
        else this.sha512password = DigestUtils.sha512Hex(password);
        ArrayList<WhiteCard> whiteCards = new ArrayList<>();
        ArrayList<BlackCard> blackCards = new ArrayList<>();
        loadCardsFromDB(whiteCards, blackCards);
        this.whitecardsPool = new WhitecardsPool(whiteCards);
        this.blackcardsPool = new BlackcardsPool(blackCards);
        this.listener = new ServerListenerThread(port);
        this.listener.start();
        this.serverStateThread = new ServerLobbyThread();
        this.serverStateThread.start();
    }

    // Util Methods
    public void broadcastMessage(JSONObject message) throws IOException {
        for (ServerPlayerData playerData : serverPlayerDataManager.getServerPlayersData()) {
            JSONSerializer.writeJSONObject(playerData.getSocket().getOutputStream(), message);
        }
    }
    private void loadCardsFromDB(@NotNull final ArrayList<@NotNull WhiteCard> whiteCards, @NotNull final ArrayList<@NotNull BlackCard> blackCards) throws IOException, NotEnoughWhitecardsException, NotEnoughBlackcardsException {
        File targetDir = new File(new File(Defs.JAR_POSITION).getParent() + File.separator + Defs.DBS_DIRECTORY_NAME);
        if (!targetDir.exists() || !targetDir.isDirectory()) targetDir.mkdir();
        File[] dbs = targetDir.listFiles((dir, name) -> name.endsWith(Defs.DB_EXTENSION));
        if (dbs == null) throw new IOException("DB dir path \"" + targetDir.getAbsolutePath() + "\" is not a directory IO Exception has occurred while accessing directory");
        for (File db : dbs) {
            try {
                int whitecardsCount = 0;
                int blackcardsCount = 0;
                Logger.log("Checking db: " + db.getAbsolutePath(), Defs.SERVER_LOGGER_CONTEXT);
                if (!db.isFile()) continue;
                DBManager.connectToDB(db);
                String dbVersion = DBDataHandler.readKeyParameter(KeyParameters.DB_VERSION);
                if (dbVersion == null || !dbVersion.equals(Defs.DB_VERSION)) {
                    DBManager.closeConnection();
                    continue;
                }
                String query = "SELECT * FROM whitecards;";
                try (PreparedStatement ps = DBManager.preparedStatement(query)) {
                    if (ps == null) throw new SQLException("DB Connection is null");
                    ResultSet result = ps.executeQuery();
                    while (result.next()) {
                        String uuid = result.getString("uuid");
                        String content = result.getString("content");
                        boolean isWildcard = result.getInt("is_wildcard") == 1;
                        whiteCards.add(new WhiteCard(uuid, content, isWildcard));
                        whitecardsCount++;
                    }
                }
                query = "SELECT * FROM blackcards;";
                try (PreparedStatement ps = DBManager.preparedStatement(query)) {
                    if (ps == null) throw new SQLException("DB Connection is null");
                    ResultSet result = ps.executeQuery();
                    while (result.next()) {
                        String uuid = result.getString("uuid");
                        if (uuid == null || uuid.trim().isBlank()) continue;
                        String content = result.getString("content");
                        if (content == null || content.trim().isBlank()) continue;
                        int blanks = result.getInt("blanks");
                        if (blanks <= 0) continue;
                        blackCards.add(new BlackCard(uuid, content, blanks));
                        blackcardsCount++;
                    }
                }
                Logger.log("Loaded from \"" + db.getAbsolutePath() + "\": " + whitecardsCount + " W | " + blackcardsCount + "B", Defs.SERVER_LOGGER_CONTEXT);
                DBManager.closeConnection();
            } catch (SQLException e) {
                Logger.log("An SQLException has occurred while loading db: \"" + db.getAbsolutePath() + "\", this db will be skipped.");
                Logger.log(e, Defs.SERVER_LOGGER_CONTEXT);
                DBManager.closeConnection();
            }
        }
        if (whiteCards.size() < Defs.MIN_WHITECARDS_LOADED) throw new NotEnoughWhitecardsException("Not enough whitecards from DBs");
        if (blackCards.size() < Defs.MIN_BLACKCARDS_LOADED) throw new NotEnoughBlackcardsException("Not enough blackcards from DBs");
        Logger.log("Loaded from DBs: " + whiteCards.size() + "W | " + blackCards.size() + "B", Defs.SERVER_LOGGER_CONTEXT);
    }

    // Methods
    public @NotNull WhitecardsPool getWhitecardsPool() {
        return whitecardsPool;
    }
    public @NotNull BlackcardsPool getBlackcardsPool() {
        return blackcardsPool;
    }
    public @Nullable String getSha512password() {
        return sha512password;
    }
    public int getPort() {
        return port;
    }
    public int getCurrentPlayers() {
        return serverPlayerDataManager.size();
    }
    public int getAvailableSpace() {
        return Math.max(0, getMaxPlayers() - getCurrentPlayers());
    }
    public int getMaxRounds() {
        return maxRounds;
    }
    public int getMaxPlayers() {
        return maxPlayers;
    }
    public @NotNull ServerListenerThread getListener() {
        return listener;
    }
    public @NotNull ServerPlayerDataManager getServerPlayerDataManager() {
        return serverPlayerDataManager;
    }
    public @NotNull Thread getServerStateThread() {
        return serverStateThread;
    }
    public void changeServerStateThread(@NotNull final Thread serverStateThread) {
        this.serverStateThread.interrupt();
        this.serverStateThread = serverStateThread;
        this.serverStateThread.start();
    }
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ServerInstance that)) return false;

        return getPort() == that.getPort() && Objects.equals(getSha512password(), that.getSha512password()) && getListener().equals(that.getListener());
    }
    @Override
    public int hashCode() {
        int result = Objects.hashCode(getSha512password());
        result = 31 * result + getPort();
        result = 31 * result + getListener().hashCode();
        return result;
    }
    @Override @NotNull
    public String toString() {
        return listener.toString();
    }

    // Instance
    private static ServerInstance INSTANCE = null;
    public static void stopInstance() {
        if (INSTANCE == null) return;
        INSTANCE.listener.interrupt();
        for (ServerPlayerData playerData : INSTANCE.serverPlayerDataManager.getServerPlayersData()) {
            try {
                JSONSerializer.writeJSONObject(playerData.getSocket().getOutputStream(), SharedProtocols.getConnectionClose("Server closed."));
            } catch (Exception ignored) {}
            try {
                playerData.getSocket().close();
            } catch (Exception ignored) {}
            INSTANCE.serverPlayerDataManager.getServerPlayersData().remove(playerData);
        }
        INSTANCE.serverStateThread.interrupt();
        INSTANCE = null;
    }
    public static void newInstance(int port, int maxPlayers, int maxRounds, @Nullable final String password) throws IOException, NotEnoughWhitecardsException, NotEnoughBlackcardsException {
        if (INSTANCE != null) {
            throw new RuntimeException("Can't create a new ServerInstance while there is already one active.");
        }
        INSTANCE = new ServerInstance(port, maxPlayers, maxRounds, password);
    }
    public static ServerInstance getInstance() {
        return INSTANCE;
    }
}
