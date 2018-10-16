package sharqBot.Pickup;

import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.user.UserOnlineStatusUpdateEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.HashMap;
import java.util.Map;

public class PickupListener extends ListenerAdapter {

    private final Map<String, GuildQueue> guildQueues;
    private final Map<User, MessageChannel> lastChannels;


    public PickupListener() {
        guildQueues = new HashMap<>();
        lastChannels = new HashMap<>();
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

                if (command.length < 3) {
                    channel.sendMessage("Please specify a mode!").queue();
                } else {
                    guildQueue.add(event.getMember().getUser(), command[2], channel);
                    lastChannels.put(event.getMember().getUser(), channel);
                }


            } else if (command[1].equalsIgnoreCase("remove")) {
                if (command.length < 3) {

                    if (guildQueue.remove(event.getMember().getUser())) {
                        guildQueue.who(channel);
                        lastChannels.remove(event.getMember().getUser(), channel);

                    } else {
                        channel.sendMessage("You are not in any queues!").queue();
                    }

//                    guildQueue.remove(event.getMember().getUser(),channel);
                } else {
                    guildQueue.remove(event.getMember().getUser(), command[2], channel);
                }


            } else if (command[1].equalsIgnoreCase("whomst")) {
                if (command.length < 3) {
                    guildQueue.who(channel);
                } else {
                    guildQueue.who(command[2], channel);
                }

            } else if (command[1].equalsIgnoreCase("start")) {
                if (command.length < 3) {
                    channel.sendMessage("Please specify a mode!").queue();
                } else {
                    guildQueue.start(command[2],channel);
                }
            } else if (command[1].equalsIgnoreCase("promote")) {

            }
        }
    }


    @Override
    public void onUserOnlineStatusUpdate(UserOnlineStatusUpdateEvent event) {
        Guild guild = event.getGuild();
        GuildQueue guildQueue = getGuildQueue(guild);
        User user = event.getUser();
        if (event.getGuild().getMember(user).getOnlineStatus() == OnlineStatus.OFFLINE) {

            if (guildQueue.remove(user)) {
                lastChannels.get(user).sendMessage(user.getName() + " went offline and was removed from all pickups!").queue();
                lastChannels.remove(user);
//                guildQueue.getLastChannel().sendMessage(user.getName() + " went offline and was removed from all pickups!").queue();

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