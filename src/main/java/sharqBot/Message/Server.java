package sharqBot.Message;

import java.util.ArrayList;

class Server {

    private String address, serverName, map, gameTypeShort;
    private int players, maxPlayers;

    private ArrayList<String> playerList;

    Server(String address, String serverName, String map, String gameTypeShort, int players, int maxPlayers, ArrayList<String> playerList) {
        this.address = address;
        this.serverName = serverName;
        this.map = map;
        this.gameTypeShort = gameTypeShort;
        this.players = players;
        this.maxPlayers = maxPlayers;
        this.playerList = playerList;
    }

    String getAddress() {
        return address;
    }

    String getServerName() {
        return serverName;
    }

    String getMap() {
        return map;
    }

    String getGameTypeShort() {
        return gameTypeShort;
    }

    int getPlayers() {
        return players;
    }

    int getMaxPlayers() {
        return maxPlayers;
    }

    ArrayList<String> getPlayerList() {
        return playerList;
    }

}
