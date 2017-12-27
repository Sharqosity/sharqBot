package sharqBot.Pickup;

import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;

class Mode {

    private final int MAX_PLAYERS;
    private final String NAME;
    private ArrayList<User> queue = new ArrayList<>();

    Mode(String name, int maxPlayers) {
        MAX_PLAYERS = maxPlayers;
        NAME = name;
    }

    int getMaxPlayers() {
        return MAX_PLAYERS;
    }

    String getName() {
        return NAME;
    }

    ArrayList<User> getQueue() {
        return queue;
    }

}
