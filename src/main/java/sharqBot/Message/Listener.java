package sharqBot.Message;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.json.JSONArray;
import org.json.JSONObject;
import sharqBot.Main;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class Listener extends ListenerAdapter {

//    @Override
//    public void onUserTyping(UserTypingEvent event) {
//        if (event.getUser().getId().equals("251168250023378944")) {
//            MessageChannel channel = event.getChannel();
//            channel.sendMessage("shut up andy").queue();
//        }
//    }

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

        String[] command = content.split(" ", 3);

        if (command[0].equals("<@384172837218287616>")) {

            if (command[1].equalsIgnoreCase("ping")) {
                channel.sendMessage("fuck you").queue();

            } else if (command[1].equalsIgnoreCase("help")) {

                EmbedBuilder messageReply = new EmbedBuilder();
                messageReply.setTitle("Message Commands");
                messageReply.setDescription("|");
//                messageReply.setColor(Color.decode("#244047"));
                messageReply.setColor(Color.decode("#F5CA40"));
                messageReply.addField("@SharqBot ping", "Checks if the bot is online", false);
                messageReply.addField("@SharqBot help", "sends help", false);
                messageReply.addField("@SharqBot servers", "Lists public Reflex servers with players", false);
                messageReply.addField("@SharqBot coinflip <heads,tails>", "Flips a coin (not rigged)", false);
                messageReply.addField("@SharqBot tylerisbored", "For Tyler. wanna hop in?", false);
//                messageReply.addField("","",true);
                channel.sendMessage(messageReply.build()).queue();

                EmbedBuilder musicReply = new EmbedBuilder();
                musicReply.setTitle("Music/Audio Commands");
                musicReply.setDescription("|");
//                musicReply.setColor(Color.decode("#75bdc6"));
                musicReply.setColor(Color.decode("#CA2E47"));
                musicReply.addField("!filename", "Plays .mp3 file with given name from sharq's local folder", false);
                musicReply.addField("@SharqBot play <link>", "Plays a song from youtube", false);
                musicReply.addField("!mts <type your sentence in words>", "Mount text to speech", false);
                musicReply.addField("@SharqBot dictionary", "Lists MountTTS vocabulary", false);
                musicReply.addField("@SharqBot mtts", "Enables/disables MountTTS. sensible people only", false);
                channel.sendMessage(musicReply.build()).queue();


                EmbedBuilder pickupReply = new EmbedBuilder();
                pickupReply.setTitle("Pickup Commands");
                pickupReply.setDescription("|");
//                pickupReply.setColor(Color.decode("#CEEEEB"));
                pickupReply.setColor(Color.decode("#3EB97E"));
                pickupReply.addField("@SharqBot add", "Add yourself to csgo pickup queue (WIP)", false);
                pickupReply.addField("@SharqBot remove", "Remove yourself from csgo pickup queue (WIP)", false);
                pickupReply.addField("@SharqBot who", "Displays csgo pickup queue (WIP)", false);
                channel.sendMessage(pickupReply.build()).queue();

            } else if (command[1].equalsIgnoreCase("tylerisbored")) {
                channel.sendMessage("<@95641408530026496> <@103549948867387392> <@137705421786972162> <@167835741093756928> wannna hop in?").queue();

            } else if (command[1].equalsIgnoreCase("mtts")) {
                //sharq, munt
                if (message.getAuthor().getId().equals("95641408530026496") || message.getAuthor().getId().equals("167835741093756928")) {
                    Main.setMuntTTSIsOn(!Main.isMuntTTSIsOn());
                    message.getAuthor().openPrivateChannel().queue((privateChannel -> {
                        privateChannel.sendMessage("mtts: " + Main.isMuntTTSIsOn()).queue();
                    }));
                }

            } else if (command[1].equalsIgnoreCase("dictionary")) {
                String list = "";
                File folder = new File("./src/muntDict");
                File[] listOfFiles = folder.listFiles();
                assert listOfFiles != null;
                for (File f : listOfFiles) {
                    if (f.isFile()) {
                        list += f.getName().substring(0, f.getName().length() - 4) + "\n";
                    }
                }
                channel.sendMessage(list).queue();

            } else if (command[1].equalsIgnoreCase("cointoss") && ((command[2].equalsIgnoreCase("heads")) || (command[2].equalsIgnoreCase("tails")))) {
                if (message.getAuthor().getId().equals("95641408530026496")) {
                    if (command[2].equalsIgnoreCase("heads")) {
                        channel.sendMessage("Coin toss result is: heads!").queue();
                    } else {
                        channel.sendMessage("Coin toss result is: tails!").queue();
                    }
                } else {
                    if (command[2].equalsIgnoreCase("heads")) {
                        channel.sendMessage("Coin toss result is: tails!").queue();
                    } else {
                        channel.sendMessage("Coin toss result is: heads!").queue();
                    }
                }

            } else if (command[1].equalsIgnoreCase("servers")) {
                ArrayList<Server> serverList = new ArrayList<>();
                URL syncoreSite = null;
                try {
                    syncoreSite = new URL("https://reflex.syncore.org/api/servers");
                } catch (MalformedURLException e) {
                    channel.sendMessage("I couldn't connect to syncore. Does it work for you? https://reflex.syncore.org/api/servers").queue();
                    e.printStackTrace();
                    return;
                }
                BufferedReader in = null;
                String inputLine = null;
                try {
                    in = new BufferedReader(new InputStreamReader(syncoreSite.openStream()));
                    inputLine = in.readLine();
                } catch (IOException e) {
                    channel.sendMessage("I encountered an issue reading from syncore. Does the site work for you? https://reflex.syncore.org/api/servers").queue();
                    e.printStackTrace();
                    return;
                }
                assert inputLine != null;
                JSONObject obj = new JSONObject(inputLine);
                JSONArray servers = obj.getJSONArray("servers");
                for (int i = 0; i < servers.length(); i++) {
                    JSONObject server = servers.getJSONObject(i);
                    JSONObject info = server.getJSONObject("info");
                    if (info.getInt("players") > 0 && info.getInt("private") == 0) {

                        String address = server.getString("address");
                        String serverName = info.getString("serverName");
                        String map = info.getString("map");
                        String gameTypeShort = info.getString("gameTypeShort");
                        int players = info.getInt("players");
                        int maxPlayers = info.getInt("maxPlayers");

                        Server serverObject = new Server(address, serverName, map, gameTypeShort, players, maxPlayers);
                        serverList.add(serverObject);
                    }
                }
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (serverList.isEmpty()) {
                    channel.sendMessage("No public servers with players :frowning:").queue();
                } else {
                    EmbedBuilder messageReply = new EmbedBuilder();
                    messageReply.setTitle("Servers", "https://reflex.syncore.org/");
                    messageReply.setColor(Color.RED);
                    for (Server s : serverList) {
                        messageReply.addField("(" + s.getPlayers() + "/" + s.getMaxPlayers() + ") " + s.getGameTypeShort() + " on " + s.getMap(), s.getServerName() + " steam://connect/" + s.getAddress(), false);
                    }
                    channel.sendMessage(messageReply.build()).queue();
                }
            }
        }
    }
}
