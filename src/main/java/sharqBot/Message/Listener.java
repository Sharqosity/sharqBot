package sharqBot.Message;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Listener extends ListenerAdapter {

    private final long UPDATE_INTERVAL = 5L;

    public Listener(JDA api) {
        Runnable updateLists = () -> {

            org.json.simple.JSONArray messageList = JSONDude.getServerLists();
            for (int i = 0; i < messageList.size(); i++) {
                org.json.simple.JSONArray channelAndMessageID = (org.json.simple.JSONArray)messageList.get(i);
                String channelID = channelAndMessageID.get(0).toString();
                String messageID = channelAndMessageID.get(1).toString();

                Channel channel = api.getTextChannelById(channelID);

                Message message = serverListCommand();
                ((TextChannel) channel).editMessageById(messageID,message).queue();
            }
        };
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(updateLists, UPDATE_INTERVAL, UPDATE_INTERVAL, TimeUnit.MINUTES);

    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        if (event.getAuthor().isBot()) {
            return;
        }

        Message message = event.getMessage();
        String content = message.getRawContent();
        MessageChannel channel = event.getChannel();

        String[] command = content.split(" ", 4);


        if (event.isFromType(ChannelType.TEXT) || event.isFromType(ChannelType.PRIVATE)) {

            if (command[0].equalsIgnoreCase("!addlist")) {

                //check for administrative role
                boolean roleFound = false;
                for (Role r : message.getGuild().getMember(message.getAuthor()).getRoles()) {
                    if (r.getName().equalsIgnoreCase("Moderators") || r.getName().equalsIgnoreCase("Sushi Administrators") || r.getName().equalsIgnoreCase("Admin")) {
                        roleFound = true;
                        break;
                    }
                }
                if (!roleFound) {
                    return;
                }

                //build the initial server list
                Message initialMessage = serverListCommand();


                //store id of channel and message
                channel.sendMessage(initialMessage).queue(new Consumer<Message>() {
                    @Override
                    public void accept(Message t) {
                        //get list of IDs and add to them
                        org.json.simple.JSONArray messageList = JSONDude.getServerLists();

                        org.json.simple.JSONArray channelAndMessageID = new org.json.simple.JSONArray();
                        channelAndMessageID.add(channel.getId());
                        channelAndMessageID.add(t.getId());
                        messageList.add(channelAndMessageID);

                        FileWriter jsonFile = null;
                        try {
                            jsonFile = new FileWriter("./lists.json");
                            jsonFile.write(messageList.toJSONString());
                            jsonFile.flush();
                            jsonFile.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                });

                channel.sendMessage("Please pin the server list! It will update automatically every " + UPDATE_INTERVAL + " minutes.").queue();

            }
        }
    }

    private Message serverListCommand() {
        boolean sushi = false;
//                if (command[0].equalsIgnoreCase("!sushiservers")) {
//                    sushi = true;
//                }

        ArrayList<Server> serverList = new ArrayList<>();
        URL syncoreSite;
        try {
            syncoreSite = new URL("https://reflex.syncore.org/api/servers");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return new MessageBuilder().append("I couldn't connect to syncore. Does it work for you? https://reflex.syncore.org/").build();
        }
        BufferedReader in;
        String inputLine;
        try {
            in = new BufferedReader(new InputStreamReader(syncoreSite.openStream()));
            inputLine = in.readLine();
        } catch (IOException e) {

            e.printStackTrace();
            return new MessageBuilder().append("I encountered an issue reading from syncore. Does the site work for you? https://reflex.syncore.org/").build();
        }
        assert inputLine != null;
        JSONObject obj = new JSONObject(inputLine);
        JSONArray servers = obj.getJSONArray("servers");
        for (int i = 0; i < servers.length(); i++) {
            JSONObject server = servers.getJSONObject(i);
            JSONObject info = server.getJSONObject("info");
            if (info.getInt("players") > 0 && info.getInt("private") == 0) {

                String version = info.getString("serverVersion");

                String address = server.getString("address");
                String serverName = info.getString("serverName");
                String map = info.getString("map");
                String gameTypeShort = info.getString("gameTypeShort");
                int players = info.getInt("players");
                int maxPlayers = info.getInt("maxPlayers");

                JSONArray playerList = server.getJSONArray("players");
                ArrayList<String> playerArray = new ArrayList<>();

                for (int j = 0; j < playerList.length(); j++) {

                    playerArray.add(playerList.getJSONObject(j).getString("name"));

                }
//                        if (sushi) {
////                            if ((version.equals("1.1.2expplus")) || version.equals("1.1.4expplus")) {
////                                Server serverObject = new Server(address, serverName, map, gameTypeShort, players, maxPlayers, playerArray);
////                                serverList.add(serverObject);
////                            }
////                        } else {
////                            if (!version.equals("1.1.2expplus") && !version.equals("1.1.4expplus")) {
////                                Server serverObject = new Server(address, serverName, map, gameTypeShort, players, maxPlayers, playerArray);
////                                serverList.add(serverObject);
////                            }
////                        }
                Server serverObject = new Server(address, serverName, map, gameTypeShort, players, maxPlayers, playerArray);
                serverList.add(serverObject);

            }
        }
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (serverList.isEmpty()) {
            return new MessageBuilder().append("No public servers with players :frowning:").build();
        } else {
            return new MessageBuilder().setEmbed(buildServerList(serverList, sushi)).build();
        }
    }

    private MessageEmbed buildServerList(ArrayList<Server> serverList, boolean sushi) {
        EmbedBuilder messageReply = new EmbedBuilder();
        messageReply.setTitle("Servers", "https://reflex.syncore.org/");
        messageReply.setDescription("━━━━━━━━━━");
        messageReply.setColor(Color.RED);
        for (Server s : serverList) {

            if (sushi) {
                messageReply.addField("(" + s.getPlayers() + "/" + s.getMaxPlayers() + ") " + s.getGameTypeShort() + " on " + s.getMap(), s.getServerName() + "  `connect " + s.getAddress() + "`", false);

            } else {
                messageReply.addField("__(" + s.getPlayers() + "/" + s.getMaxPlayers() + ")__ " + s.getGameTypeShort() + " on " + s.getMap(), s.getServerName() + ": steam://connect/" + s.getAddress(), false);
                //hyperlink format for when discord supports steam connect hyperlinks
                //                messageReply.addField("__(" + s.getPlayers() + "/" + s.getMaxPlayers() + ")__ " + s.getGameTypeShort() + " on " + s.getMap(), "[" + s.getServerName() + "]" + "(steam://connect/" + s.getAddress() + ")", false);
            }

            StringBuilder playerField = new StringBuilder();
            for (String p : s.getPlayerList()) {
                if(!p.equals("")) {
                    playerField.append("`").append(p).append("`\n");
                }
            }
            playerField.append("━━━━━━━━━━").append("\n");
            messageReply.addField("Players: ", playerField.toString(), false);

        }
        messageReply.setFooter("Bot by Sharqosity. Server data from Syncore. This list updates every "+ UPDATE_INTERVAL + " minutes.","https://reflex.syncore.org/images/reflex.png");

        return messageReply.build();
//        channel.sendMessage(messageReply.build()).queue();
    }
}
