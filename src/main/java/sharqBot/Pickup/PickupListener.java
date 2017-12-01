package sharqBot.Pickup;

import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class PickupListener extends ListenerAdapter {

    public PickupListener() {

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
        MessageChannel channel = event.getChannel();
        Guild guild = event.getGuild();


        String[] command = content.split(" ", 3);

        if (command[0].equals("<@384172837218287616>")) {
            if (command[1].equalsIgnoreCase("add")) {


            } else if (command[1].equalsIgnoreCase("remove")) {


            } else if (command[1].equalsIgnoreCase("who")) {


            }
        }
    }
}