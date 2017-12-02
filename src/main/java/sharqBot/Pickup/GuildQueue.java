package sharqBot.Pickup;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;

import java.lang.reflect.Array;
import java.lang.reflect.Member;
import java.util.ArrayList;

public class GuildQueue {
    private static final int MAX_PLAYERS = 5;


    private ArrayList<User> queue = new ArrayList<>();


    public void add(User user) {
        queue.add(user);

    }

    public void clear() {
        queue.clear();
    }

    public void remove(User user) {
        queue.remove(user);
    }

    public String[] getNames() {
        String[] names = new String[queue.size()];
        for (int i = 0; i < queue.size(); i++) {
            names[i] = queue.get(i).getName();

        }

        return names;
    }

    public ArrayList<User> getQueue() {
        return queue;
    }

    public int playerAmount() {
        return queue.size();
    }

    public final int getMaxPlayers() {
        return MAX_PLAYERS;
    }

}