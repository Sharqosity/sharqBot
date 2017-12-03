package sharqBot.Pickup;

import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;

public class Mode {


    private final int MAX_PLAYERS;

    private final String NAME;

    private ArrayList<User> queue = new ArrayList<>();


    Mode(String name, int maxPlayers) {
        MAX_PLAYERS = maxPlayers;
        NAME = name;
    }

    public int getMaxPlayers() {
        return MAX_PLAYERS;
    }

    public String getName() {
        return NAME;
    }

    public ArrayList<User> getQueue() {
        return queue;
    }

    public void setQueue(ArrayList<User> queue) {
        this.queue = queue;
    }


}
