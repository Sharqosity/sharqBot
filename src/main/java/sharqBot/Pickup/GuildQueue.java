package sharqBot.Pickup;

import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.Iterator;

public class GuildQueue {
    private static final int CSGO_MAX_PLAYERS = 5;
    private static final int WINGMAN_MAX_PLAYERS = 2;

    private MessageChannel lastChannel;


    private ArrayList<Mode> guildModes = new ArrayList<>();


    GuildQueue() {
        guildModes.add(new Mode("CSGO", 5));
        guildModes.add(new Mode("Wingman", 2));

    }


    public void add(User user, String mode, MessageChannel channel) {

        for (Mode m : guildModes) {
            if (mode.equalsIgnoreCase(m.getName())) {
                if (m.getQueue().contains(user)) {
                    channel.sendMessage("You are already added!").queue();
                } else {
                    m.getQueue().add(user);
                    channel.sendMessage("Added! " + m.getName() + ": (" + m.getQueue().size() + "/" + m.getMaxPlayers() + ")").queue();
                    lastChannel = channel;

                }

                if (m.getQueue().size() == m.getMaxPlayers()) {
                    StringBuilder success = new StringBuilder();
                    success.append(m.getName()).append(" pickup started! ");
                    for (User u : m.getQueue()) {
                        success.append("<@").append(u.getId()).append(">, ");
                    }
                    success.append("go play");
                    channel.sendMessage(success.toString()).queue();
                    m.getQueue().clear();
                }
                return;

            }

        }
        channel.sendMessage("Invalid mode!").queue();


    }

    public MessageChannel getLastChannel() {
        return lastChannel;
    }

    public boolean remove(User user) {
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

//            for (User u : m.getQueue()) {
//                if(u == user) {
//                    m.getQueue().remove(user);
//                    removed = true;
//                }
//            }

        }
        return removed;
    }

//    public void remove(User user ,MessageChannel channel) {
//        if (remove(user)) {
//            who(lastChannel);
//        } else {
//            lastChannel.sendMessage("You are not in any queues!").queue();
//        }
//
//    }

    public void remove(User user, String mode, MessageChannel channel) {

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

//    public void removeOffline(User user) {
//
//        if(remove(user)) {
//            lastChannel.sendMessage(user.getName() + " went offline and was removed from all pickups!").queue();
//        }
//
//    }


    public void who(String mode, MessageChannel channel) {


        for (Mode m : guildModes) {
            if (mode.equalsIgnoreCase(m.getName())) {
                StringBuilder who = new StringBuilder("Current players in " + m.getName() + " queue ");
                who.append("(").append(m.getQueue().size()).append("/").append(m.getMaxPlayers()).append("): ");
                String[] names = getNames(m);
                for (int i = 0; i < names.length; i++) {
                    who.append(names[i]);
                    if (i != names.length - 1) {
                        who.append(", ");
                    }
                }
                channel.sendMessage(who.toString()).queue();
                return;
            }


            channel.sendMessage("Invalid mode!").queue();
        }

    }

    public void who(MessageChannel channel) {
        StringBuilder who = new StringBuilder("Current players in queue ");

        for (Mode m : guildModes) {
            who.append(m.getName());
            who.append(" (").append(m.getQueue().size()).append("/").append(m.getMaxPlayers()).append("): ");
            String[] names = getNames(m);
            for (int i = 0; i < names.length; i++) {
                who.append(names[i]);
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