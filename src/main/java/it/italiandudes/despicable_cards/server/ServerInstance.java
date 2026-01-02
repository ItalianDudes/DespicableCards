package it.italiandudes.despicable_cards.server;

import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class ServerInstance {

    // Attributes
    @Nullable private final String sha512password;
    private final int port;
    @NotNull private final ServerListener listener;

    // Constructors
    private ServerInstance(final int port, @Nullable final String password) {
        this.port = port;
        if (password == null) this.sha512password = null;
        else this.sha512password = DigestUtils.sha512Hex(password);
        this.listener = new ServerListener(port);
        this.listener.start();
    }

    // Methods
    public @Nullable String getSha512password() {
        return sha512password;
    }
    public int getPort() {
        return port;
    }
    public @NotNull ServerListener getListener() {
        return listener;
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
    public static ServerInstance INSTANCE = null;
    public static @NotNull ServerInstance newInstance(int port, @Nullable final String password) {
        if (INSTANCE != null) {
            INSTANCE.listener.interrupt();
            INSTANCE = null;
        }
        INSTANCE = new ServerInstance(port, password);
        return INSTANCE;
    }
    public static ServerInstance getInstance() {
        return INSTANCE;
    }
}
