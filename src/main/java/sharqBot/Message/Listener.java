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

public class Listener extends ListenerAdapter {

    //How often pinned server lists should update
    private final long UPDATE_INTERVAL = 5L;

    public Listener(JDA api) {
        //Task to update lists
        Runnable updateLists = () -> {
            //Get list of stored IDs from json file and loop through them
            org.json.simple.JSONArray messageList = JSONDude.getServerLists();
            if (messageList.size() > 0) {
                //Get updated server lists message
                Message message = serverListCommand();
                for (Object channelAndMessageIDJSON : messageList) {
                    //Each json array has IDs for messages and the channel they were sent in
                    //converts object to JSONArray
                    org.json.simple.JSONArray channelAndMessageID = (org.json.simple.JSONArray) channelAndMessageIDJSON;
                    String channelID = channelAndMessageID.get(0).toString();
                    String messageID = channelAndMessageID.get(1).toString();
                    //Get actual channel object from ID
                    TextChannel channel = api.getTextChannelById(channelID);
                    //edits the original message
                    channel.editMessageById(messageID, message).queue();
                }
            }
        };
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        //schedules the task at every time interval
        executorService.scheduleAtFixedRate(updateLists, UPDATE_INTERVAL, UPDATE_INTERVAL, TimeUnit.MINUTES);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        //ignore other bot messages
        if (event.getAuthor().isBot()) {
            return;
        }
        //get context from event
        Message message = event.getMessage();
        String content = message.getRawContent();
        MessageChannel channel = event.getChannel();
        //split user commands into separate strings
        String[] command = content.split(" ", 4);
        if (event.isFromType(ChannelType.TEXT)) {
            if (command[0].equalsIgnoreCase("!addlist")) {
                //check for administrative role
                boolean roleFound = false;
                for (Role r : message.getGuild().getMember(message.getAuthor()).getRoles()) {
                    if (r.getName().equalsIgnoreCase("Moderators") || r.getName().equalsIgnoreCase("Sushi Administrators") || r.getName().equalsIgnoreCase("Admin")) {
                        roleFound = true;
                        break;
                    }
                }
                if (!roleFound) { //stops if administrative role not found
                    return;
                }

                //Build the initial server list
                Message initialMessage = serverListCommand();

                //Store id of channel and message
                channel.sendMessage(initialMessage).queue(t -> { //message callback
                    //get list of IDs from json file and add to them
                    org.json.simple.JSONArray messageList = JSONDude.getServerLists();
                    //new ID pair json list
                    org.json.simple.JSONArray channelAndMessageID = new org.json.simple.JSONArray();
                    channelAndMessageID.add(channel.getId());
                    channelAndMessageID.add(t.getId());
                    messageList.add(channelAndMessageID);
                    //commit to file
                    FileWriter jsonFile = null;
                    try {
                        jsonFile = new FileWriter("./lists.json");
                        jsonFile.write(messageList.toJSONString());
                        jsonFile.flush();
                        jsonFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                //reminder message. todo: automatically pin the message
                channel.sendMessage("Please pin the server list! It will update automatically every " + UPDATE_INTERVAL + " minutes.").queue();

            }
        }
    }

    private Message serverListCommand() {

        ArrayList<Server> serverList = new ArrayList<>();
        //reads from site
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
        //gets JSON array of servers
        JSONArray servers = obj.getJSONArray("servers");
        //loops through each server and makes a Server object with the needed info
        for (int i = 0; i < servers.length(); i++) {
            JSONObject server = servers.getJSONObject(i);
            JSONObject info = server.getJSONObject("info");
            //public servers with players in them
            if (info.getInt("players") > 0 && info.getInt("private") == 0) {
//                String version = info.getString("serverVersion");
                String address = server.getString("address");
                String serverName = info.getString("serverName");
                String map = info.getString("map");
                String gameTypeShort = info.getString("gameTypeShort");
                int players = info.getInt("players");
                int maxPlayers = info.getInt("maxPlayers");
                //array of players in server
                JSONArray playerList = server.getJSONArray("players");
                ArrayList<String> playerArray = new ArrayList<>();
                for (int j = 0; j < playerList.length(); j++) {
                    playerArray.add(playerList.getJSONObject(j).getString("name"));
                }
                //create Server object and add it to master array
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
//            return new MessageBuilder().append("No public servers with players :frowning:").build();
            return new MessageBuilder().setEmbed(new EmbedBuilder().setDescription("No public servers with players :frowning:").build()).build();
        } else {//If any filtered server list isn't empty
            return new MessageBuilder().setEmbed(buildServerList(serverList)).build();
        }
    }

    private MessageEmbed buildServerList(ArrayList<Server> serverList) {
        EmbedBuilder messageReply = new EmbedBuilder();
        messageReply.setTitle("Servers", "https://reflex.syncore.org/");
        messageReply.setDescription("━━━━━━━━━━");
        messageReply.setColor(Color.RED);
        for (Server s : serverList) {

            messageReply.addField("__(" + s.getPlayers() + "/" + s.getMaxPlayers() + ")__ " + s.getGameTypeShort() + " on " + s.getMap(), s.getServerName() + ": steam://connect/" + s.getAddress(), false);
            /* hyperlink format for when discord supports steam connect hyperlinks
             */
            //                messageReply.addField("__(" + s.getPlayers() + "/" + s.getMaxPlayers() + ")__ " + s.getGameTypeShort() + " on " + s.getMap(), "[" + s.getServerName() + "]" + "(steam://connect/" + s.getAddress() + ")", false);

            /* legacy console command paste instead of steam connect link
             */
            //messageReply.addField("(" + s.getPlayers() + "/" + s.getMaxPlayers() + ") " + s.getGameTypeShort() + " on " + s.getMap(), s.getServerName() + "  `connect " + s.getAddress() + "`", false);

            StringBuilder playerField = new StringBuilder();
            for (String p : s.getPlayerList()) {
                if (!p.equals("")) { //filter out bugged players
                    playerField.append("`").append(p).append("`\n");
                }
            }
            playerField.append("━━━━━━━━━━").append("\n");
            messageReply.addField("Players: ", playerField.toString(), false);

        }
        messageReply.setFooter("Bot by Sharqosity. Server data from Syncore. This list updates every " + UPDATE_INTERVAL + " minutes.", "https://reflex.syncore.org/images/reflex.png");

        return messageReply.build();
    }
}
