package sharqBot.Message;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.GuildController;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class Listener extends ListenerAdapter {

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

            if (command[0].equalsIgnoreCase("!ping")) {
                channel.sendMessage("fuck you").queue();

            } else if (command[0].equalsIgnoreCase("!help")) {

                EmbedBuilder messageReply = new EmbedBuilder();
                messageReply.setTitle("Message Commands");
                messageReply.setDescription("");
                messageReply.setColor(Color.decode("#3EB97E"));
                messageReply.addField("!ping", "Checks if the bot is online", false);
                messageReply.addField("!help", "sends help", false);
                messageReply.addField("!servers", "Lists public Reflex servers with players", false);
                messageReply.addField("!sushiservers", "Lists public Sushi ruleset servers with players", false);
                messageReply.addField("!cointoss <heads/tails>", "Flips a coin (not rigged)", false);
                messageReply.addField("!notify <NA/EU>", "Gives you pickup role", false);
                messageReply.addField("!removerole <NA/EU>", "Removes pickup role", false);
//                messageReply.addField("","",true);
                channel.sendMessage(messageReply.build()).queue();

                EmbedBuilder sharqCoinReply = new EmbedBuilder();
                sharqCoinReply.setTitle("SharqCoin Commands");
                sharqCoinReply.setDescription("");
                sharqCoinReply.setColor(Color.decode("#F5CA40"));
                sharqCoinReply.addField("!faq", "Frequently asked questions about sharqcoin", false);
                sharqCoinReply.addField("!wallet", "View your current sharqcoin balance", false);
                sharqCoinReply.addField("!send <amount> @user <message>", "Send another user sharqcoin", false);
                sharqCoinReply.addField("!top5", "forbes list of top billionaires", false);
                channel.sendMessage(sharqCoinReply.build()).queue();


                EmbedBuilder betReply = new EmbedBuilder();
                betReply.setTitle("Betting Commands");
                betReply.setColor(Color.decode("#6C0EF7"));
                betReply.addField("PM the bot: !bet <amount> <player>", "Places a wager on a player who is part of an open bet", false);
                betReply.addField("!openbets", "Lists current games open for betting", false);
                betReply.addField("!closedbets", "Lists current in-progress games", false);
                channel.sendMessage(betReply.build()).queue();

//                EmbedBuilder pickupReply = new EmbedBuilder();
//                pickupReply.setTitle("Pickup Commands");
//                pickupReply.setDescription("|");
//                pickupReply.setColor(Color.decode("#3EB97E"));
//                pickupReply.addField("@SharqBot add <mode>", "Add yourself to pickup queue for specified mode", false);
//                pickupReply.addField("@SharqBot remove <mode>", "Remove yourself from specified pickup queue", false);
//                pickupReply.addField("@SharqBot who", "Displays status of current queues.", false);
//                pickupReply.addField("@SharqBot start <mode>", "Starts pickup in case you don't want to wait for it to fill up", false);
//                channel.sendMessage(pickupReply.build()).queue();

            } else if (command[0].equalsIgnoreCase("!cointoss") && ((command[1].equalsIgnoreCase("heads")) || (command[1].equalsIgnoreCase("tails")))) {
                if (event.isFromType(ChannelType.PRIVATE)) {
                    return;
                }
                EmbedBuilder cointoss = new EmbedBuilder();
                if (message.getAuthor().getId().equals("95641408530026496")) {
                    if (command[1].equalsIgnoreCase("heads")) {
                        cointoss.setTitle("Coin toss result is: heads!");

                    } else {
                        cointoss.setTitle("Coin toss result is: tails!");

                    }
                } else {
                    if (command[1].equalsIgnoreCase("heads")) {
                        cointoss.setTitle("Coin toss result is: tails!");
                    } else {
                        cointoss.setTitle("Coin toss result is: heads!");
                    }
                }
                channel.sendMessage(cointoss.build()).queue();


            } else if (command[0].equalsIgnoreCase("!notify")) {
                if (event.isFromType(ChannelType.TEXT)) {
                    return;
                }
                if (message.getGuild().getId().equals("407749422592819200")) {
//                    Role NA = message.getGuild().getRolesByName("NA", true).get(0);
                    Role NA = message.getGuild().getRoleById("411979598541225996");
//                    Role EU = message.getGuild().getRolesByName("EU", true).get(0);
                    Role EU = message.getGuild().getRoleById("408771458622291969");
                    GuildController guildController = new GuildController(message.getGuild());

                    if (command[1].equalsIgnoreCase("NA")) {
                        guildController.addSingleRoleToMember(message.getMember(), NA).queue();
                    } else if (command[1].equalsIgnoreCase("EU")) {
                        guildController.addSingleRoleToMember(message.getMember(), EU).queue();
                    }
                }

            } else if (command[0].equalsIgnoreCase("!removeRole")) {
                if (event.isFromType(ChannelType.TEXT)) {
                    return;
                }
                if (message.getGuild().getId().equals("407749422592819200")) {
                    Role NA = message.getGuild().getRoleById("411979598541225996");
                    Role EU = message.getGuild().getRoleById("408771458622291969");
                    GuildController guildController = new GuildController(message.getGuild());

                    if (command[1].equalsIgnoreCase("NA")) {
                        guildController.removeSingleRoleFromMember(message.getMember(), NA).queue();
                    } else if (command[1].equalsIgnoreCase("EU")) {
                        guildController.removeSingleRoleFromMember(message.getMember(), EU).queue();
                    }
                }

            } else if (command[0].equalsIgnoreCase("!servers") || command[0].equalsIgnoreCase("!sushiservers")) {
                boolean sushi = false;
                if (command[0].equalsIgnoreCase("!sushiservers")) {
                    sushi = true;
                }

                ArrayList<Server> serverList = new ArrayList<>();
                URL syncoreSite;
                try {
                    syncoreSite = new URL("https://reflex.syncore.org/api/servers");
                } catch (MalformedURLException e) {
                    channel.sendMessage("I couldn't connect to syncore. Does it work for you? https://reflex.syncore.org/").queue();
                    e.printStackTrace();
                    return;
                }
                BufferedReader in;
                String inputLine;
                try {
                    in = new BufferedReader(new InputStreamReader(syncoreSite.openStream()));
                    inputLine = in.readLine();
                } catch (IOException e) {
                    channel.sendMessage("I encountered an issue reading from syncore. Does the site work for you? https://reflex.syncore.org/").queue();
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

                        if (sushi) {
                            if ((version.equals("1.1.2expplus")) || version.equals("1.1.4expplus")) {
                                Server serverObject = new Server(address, serverName, map, gameTypeShort, players, maxPlayers, playerArray);
                                serverList.add(serverObject);
                            }
                        } else {
                            if (!version.equals("1.1.2expplus") && !version.equals("1.1.4expplus")) {
                                Server serverObject = new Server(address, serverName, map, gameTypeShort, players, maxPlayers, playerArray);
                                serverList.add(serverObject);
                            }
                        }



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
                    sendServerList(serverList, channel, sushi);
                }
            }
        }
    }

    private void sendServerList(ArrayList<Server> serverList, MessageChannel channel, boolean sushi) {
        EmbedBuilder messageReply = new EmbedBuilder();
        messageReply.setTitle("Servers", "https://reflex.syncore.org/");
        messageReply.setColor(Color.RED);
        for (Server s : serverList) {

            if (sushi) {
                messageReply.addField("(" + s.getPlayers() + "/" + s.getMaxPlayers() + ") " + s.getGameTypeShort() + " on " + s.getMap(), s.getServerName() + "  `connect " + s.getAddress() + "`", false);

            } else {
                messageReply.addField("(" + s.getPlayers() + "/" + s.getMaxPlayers() + ") " + s.getGameTypeShort() + " on " + s.getMap(), s.getServerName() + " steam://connect/" + s.getAddress(), false);

            }

            StringBuilder playerField = new StringBuilder();
            for (String p : s.getPlayerList()) {
                if(!p.equals("")) {
                    playerField.append(p).append("\n");
                }
            }
            messageReply.addField("Players: ", playerField.toString(), false);

        }
        channel.sendMessage(messageReply.build()).queue();
    }
}
