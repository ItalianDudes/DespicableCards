package it.italiandudes.despicable_cards.server;

import it.italiandudes.despicable_cards.utils.Defs;
import it.italiandudes.idl.logger.InfoFlags;
import it.italiandudes.idl.logger.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public final class ServerListener extends Thread {

    // Attributes
    private final int port;

    // Constructors
    public ServerListener(int port) {
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
        if (!(o instanceof ServerListener that)) return false;

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

                } catch (Exception e) {
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (Exception ignored) {}
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            Logger.log("Invalid port value", Defs.LOGGER_CONTEXT);
        } catch (IOException e) {
            Logger.log("An error has occured while open ServerSocket on port " + port, new InfoFlags(true, false), Defs.LOGGER_CONTEXT);
            Logger.log(e);
        }
    }
}
