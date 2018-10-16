package sharqBot.Pickup;

import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.Iterator;

class GuildQueue {

    private ArrayList<Mode> guildModes = new ArrayList<>();

    GuildQueue() {
        //TODO read from a file
        guildModes.add(new Mode("CSGO", 5));
        guildModes.add(new Mode("Wingman", 2));
        guildModes.add(new Mode("Dota", 3));

    }

    //start with string
    void start(String string, MessageChannel channel) {
        for (Mode m : guildModes) {
            if (string.equalsIgnoreCase(m.getName())) {
                start(m,channel);
                return;
            }
        }
        channel.sendMessage("Invalid mode!").queue();

    }

    //start without string
    private void start(Mode mode, MessageChannel channel) {

        StringBuilder success = new StringBuilder();
        success.append(mode.getName()).append(" pickup started! ");
        for (User u : mode.getQueue()) {
            success.append("<@").append(u.getId()).append(">, ");
        }
        success.append("go play");
        channel.sendMessage(success.toString()).queue();
        mode.getQueue().clear();
    }

    boolean add(User user, String mode, MessageChannel channel) {

        for (Mode m : guildModes) {
            if (mode.equalsIgnoreCase(m.getName())) {
                if (m.getQueue().contains(user)) {
                    channel.sendMessage("You are already added!").queue();
                    return false;
                } else {
                    m.getQueue().add(user);
                    channel.sendMessage("Added! " + m.getName() + ": (" + m.getQueue().size() + "/" + m.getMaxPlayers() + ")").queue();
                    if (m.getQueue().size() == m.getMaxPlayers()) {
                        start(m,channel);
                    }
                    return true;
                }



            }

        }
        channel.sendMessage("Invalid mode!").queue();
        return false;
    }

    boolean remove(User user) {
        boolean removed = false;
        for (Mode m : guildModes) {
            Iterator<User> iterator = m.getQueue().iterator();
            while (iterator.hasNext()) {
                User u = iterator.next();
                if (u == user) {
                    iterator.remove();
                    removed = true;
                }
            }

        }
        return removed;
    }

    void remove(User user, String mode, MessageChannel channel) {

        boolean gotACorrectModeName = false;
        for (Mode m : guildModes) {
            if (mode.equalsIgnoreCase(m.getName())) {
                for (User u : m.getQueue()) {
                    if (u == user) {
                        m.getQueue().remove(u);
                        channel.sendMessage("Removed! Queue: (" + m.getQueue().size() + "/" + m.getMaxPlayers() + ")").queue();
                        return;
                    }
                }
                gotACorrectModeName = true;
            }
        }
        if (gotACorrectModeName) {
            channel.sendMessage("You are not in any queues!").queue();
        } else {
            channel.sendMessage("Invalid mode!").queue();
        }

    }

    //who command with mode given
    void who(String mode, MessageChannel channel) {
        for (Mode m : guildModes) {
            if (mode.equalsIgnoreCase(m.getName())) {
                StringBuilder who = new StringBuilder("Current players in " + m.getName() + " queue: ");
                who.append("(").append(m.getQueue().size()).append("/").append(m.getMaxPlayers()).append(") ");
                String[] names = getNames(m);
                for (int i = 0; i < names.length; i++) {
                    who.append("`"+names[i]+"`");
                    if (i != names.length - 1) {
                        who.append(", ");
                    }
                }
                channel.sendMessage(who.toString()).queue();
                return;
            }
        }
        channel.sendMessage("Invalid mode!").queue();

    }

    //who command without mode given
    void who(MessageChannel channel) {
        StringBuilder who = new StringBuilder("Current players in queue ");

        for (Mode m : guildModes) {
            who.append(m.getName());
            who.append(": (").append(m.getQueue().size()).append("/").append(m.getMaxPlayers()).append(") ");
            String[] names = getNames(m);
            for (int i = 0; i < names.length; i++) {
                who.append("`" + names[i] + "`");
                if (i != names.length - 1) {
                    who.append(", ");
                }
            }
            who.append(" ");


        }
        channel.sendMessage(who.toString()).queue();


    }

    private String[] getNames(Mode m) {
        String[] names = new String[m.getQueue().size()];
        for (int i = 0; i < m.getQueue().size(); i++) {
            names[i] = m.getQueue().get(i).getName();

        }

        return names;
    }


}