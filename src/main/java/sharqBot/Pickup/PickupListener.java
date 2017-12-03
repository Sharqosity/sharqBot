package sharqBot.Pickup;

import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.HashMap;
import java.util.Map;

public class PickupListener extends ListenerAdapter {

    private final Map<String, GuildQueue> guildQueues;

    public PickupListener() {
        guildQueues = new HashMap<String, GuildQueue>();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        if (!event.isFromType(ChannelType.TEXT)) {
            return;
        }

        if (event.getAuthor().isBot()) {
            return;
        }

        Message message = event.getMessage();
        String content = message.getRawContent();


        String[] command = content.split(" ", 3);

        if (command[0].equals("<@384172837218287616>")) {
            Guild guild = event.getGuild();
            GuildQueue guildQueue = getGuildQueue(guild);

            MessageChannel channel = event.getChannel();

            if (command[1].equalsIgnoreCase("add")) {

                if (guildQueue.getQueue().contains(event.getMember().getUser())) {
                    channel.sendMessage("You are already added!").queue();
                } else {
                    guildQueue.add(event.getMember().getUser());
                    channel.sendMessage("Added! (" + guildQueue.playerAmount() + "/" + guildQueue.getMaxPlayers() + ")").queue();

                }

                if (guildQueue.getQueue().size() == guildQueue.getMaxPlayers()) {
                    StringBuilder success = new StringBuilder();
                    success.append("Pickup started! ");
                    for (User u : guildQueue.getQueue()) {
                        success.append("<@").append(u.getId()).append(">, ");
                    }
                    success.append("go play");
                    channel.sendMessage(success.toString()).queue();
                    guildQueue.clear();
                }

            } else if (command[1].equalsIgnoreCase("remove")) {
                guildQueue.remove(event.getMember().getUser());
                channel.sendMessage("Queue: (" + guildQueue.playerAmount() + "/" + guildQueue.getMaxPlayers() + ")").queue();


            } else if (command[1].equalsIgnoreCase("who")) {
                StringBuilder who = new StringBuilder("Current players in queue ");
                who.append("(").append(guildQueue.playerAmount()).append("/").append(guildQueue.getMaxPlayers()).append("): ");
                String[] names = guildQueue.getNames();
                for (int i = 0; i < names.length; i++) {
                    who.append(names[i]);
                    if (i != names.length - 1) {
                        who.append(", ");
                    }
                }
                channel.sendMessage(who.toString()).queue();

            }
        }
    }

    private GuildQueue getGuildQueue(Guild guild) {
        String guildId = guild.getId();
        GuildQueue gq = guildQueues.get(guildId);
        if (gq == null) {
            synchronized (guildQueues) {
                gq = guildQueues.get(guildId);
                if (gq == null) {
                    gq = new GuildQueue();
                    guildQueues.put(guildId, gq);
                }
            }
        }
        return gq;
    }
}