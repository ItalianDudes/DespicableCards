package it.italiandudes.despicable_cards.server.thread;

import it.italiandudes.despicable_cards.utils.Defs;
import it.italiandudes.idl.logger.InfoFlags;
import it.italiandudes.idl.logger.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public final class ServerListenerThread extends Thread {

    // Attributes
    private final int port;

    // Constructors
    public ServerListenerThread(int port) {
        this.port = port;
        setDaemon(true);
        setName("DespicableCards-ServerListener-" + port + "-Daemon");
    }

    // Methods
    public int getPort() {
        return port;
    }
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ServerListenerThread that)) return false;

        return getPort() == that.getPort();
    }
    @Override
    public int hashCode() {
        return getPort();
    }
    @Override @NotNull
    public String toString() {
        return getName();
    }

    // Runnable
    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            Socket socket = null;
            while (!isInterrupted()) {
                try {
                    socket = serverSocket.accept();
                    new ServerPlayerHandshakeThread(socket).start();
                } catch (Exception e) {
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (Exception ignored) {}
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            Logger.log("Invalid port value", Defs.SERVER_LOGGER_CONTEXT);
        } catch (IOException e) {
            Logger.log("An error has occurred while open ServerSocket on port " + port, new InfoFlags(true, false), Defs.SERVER_LOGGER_CONTEXT);
            Logger.log(e, Defs.SERVER_LOGGER_CONTEXT);
        }
    }
}
