package it.italiandudes.despicable_cards.server.thread;

import it.italiandudes.despicable_cards.data.player.ServerPlayerData;
import it.italiandudes.despicable_cards.protocol.ServerProtocols;
import it.italiandudes.despicable_cards.server.ServerInstance;
import it.italiandudes.despicable_cards.utils.Defs;
import it.italiandudes.idl.logger.Logger;

public final class ServerLobbyThread extends Thread {

    // Attributes

    // Constructors
    public ServerLobbyThread() {
        setDaemon(true);
        setName("ServerLobbyThread");
    }

    // Methods
    private boolean everyoneReady() {
        if (ServerInstance.getInstance().getServerPlayerDataManager().size() < 3) return false;
        for (ServerPlayerData playerData : ServerInstance.getInstance().getServerPlayerDataManager().getServerPlayersData()) {
            if (!playerData.isReady()) return false;
        }
        return true;
    }

    // Runnable
    @Override
    public void run() {
        try {
            ServerInstance.getInstance().broadcastMessage(ServerProtocols.Lobby.getLobbyPlayersList(ServerInstance.getInstance().getServerPlayerDataManager().getServerPlayersData()));
            while (!isInterrupted()) {
                //noinspection BusyWait
                Thread.sleep(1000);
                if (everyoneReady()) {
                    ServerInstance.getInstance().broadcastMessage(ServerProtocols.State.getStateGame());
                    ServerGameThread gameThread = new ServerGameThread();
                    ServerInstance.getInstance().changeServerStateThread(gameThread);
                    return;
                }
            }
        } catch (InterruptedException ignored) {
        } catch (Exception e) {
            Logger.log(e, Defs.SERVER_LOGGER_CONTEXT);
            ServerInstance.stopInstance();
        }
    }
}
