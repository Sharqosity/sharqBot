package sharqBot.Message;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.GuildController;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;

public class Listener extends ListenerAdapter {

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

        String[] command = content.split(" ", 4);

        if (command[0].equalsIgnoreCase("!ping")) {
            channel.sendMessage("fuck you").queue();

        } else if (command[0].equalsIgnoreCase("!help")) {

            EmbedBuilder messageReply = new EmbedBuilder();
            messageReply.setTitle("Message Commands");
            messageReply.setDescription("");
            messageReply.setColor(Color.decode("#F5CA40"));
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
            sharqCoinReply.setColor(Color.decode("#3EB97E"));
            sharqCoinReply.addField("!wallet", "View your current sharqcoin balance", false);
            sharqCoinReply.addField("!send <amount> @user <message>", "Send another user sharqcoin", false);
            sharqCoinReply.addField("!top5", "forbes list of top billionaires", false);
            channel.sendMessage(sharqCoinReply.build()).queue();

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
            if (message.getAuthor().getId().equals("95641408530026496")) {
                if (command[1].equalsIgnoreCase("heads")) {
                    channel.sendMessage("Coin toss result is: heads!").queue();
                } else {
                    channel.sendMessage("Coin toss result is: tails!").queue();
                }
            } else {
                if (command[1].equalsIgnoreCase("heads")) {
                    channel.sendMessage("Coin toss result is: tails!").queue();
                } else {
                    channel.sendMessage("Coin toss result is: heads!").queue();
                }
            }

        } else if (command[0].equalsIgnoreCase("!wallet")) {

            JSONParser parser = new JSONParser();

            try {
                Object obj = parser.parse(new FileReader("./sharqcoin.json"));
                org.json.simple.JSONArray users;

                createWallet(message.getAuthor());
                obj = parser.parse(new FileReader("./sharqcoin.json"));
                users = (org.json.simple.JSONArray) obj;

                org.json.simple.JSONObject userFound = findUser(message.getAuthor().getId(), users);

                assert userFound != null;
                channel.sendMessage("You have " + userFound.get("amount") + "<:sharqcoin:413785618573819905> in your wallet.").queue();

//                for (Emote e : message.getGuild().getEmotes()) {
//                    System.out.println(e.getName() + ", " + e.getId());
//                }
//                org.json.simple.JSONObject foundUser = findUser(message.getAuthor().getId(), users);
//                if (foundUser != null) {
//                    channel.sendMessage("You have " + foundUser.get("amount") + "<sharqcoin:413865986211708950> in your wallet.").queue();
//                } else {
////                    channel.sendMessage("No wallet found! Create a new one with !createwallet").queue();
//                    createWallet(message.getAuthor());
//                }

            } catch (ParseException | IOException e) {
                e.printStackTrace();
            }

        }
//        else if (command[0].equalsIgnoreCase("!createwallet")) {
//
//        }
        else if (command[0].equalsIgnoreCase("!send")) {

            double sendAmount = Double.parseDouble(command[1]);
//            int sendAmount = Integer.parseInt(command[1]);

            JSONParser parser = new JSONParser();
//                org.json.simple.JSONObject sender = new org.json.simple.JSONObject();


            try {
                Object obj = parser.parse(new FileReader("./sharqcoin.json"));
                org.json.simple.JSONArray users;


                createWallet(message.getAuthor());
//                System.out.println(command[2].substring(2, command[2].length() - 1));
                User recipient = message.getGuild().getMemberById(Long.parseLong(command[2].substring(2, command[2].length() - 1))).getUser();
                createWallet(recipient);

//                System.out.println(recipient.getName());

                obj = parser.parse(new FileReader("./sharqcoin.json"));
                users = (org.json.simple.JSONArray) obj;

                org.json.simple.JSONObject userFound = findUser(message.getAuthor().getId(), users);

                org.json.simple.JSONObject targetUser = findUser(command[2].substring(2, command[2].length() - 1), users);

                assert userFound != null;
                if (sendAmount > 0 && sendAmount > Double.parseDouble(userFound.get("amount").toString())) {
                    channel.sendMessage("Insufficient funds!").queue();
                } else {
                    userFound.replace("amount", Double.parseDouble(userFound.get("amount").toString()) - sendAmount);
                    targetUser.replace("amount", Double.parseDouble(targetUser.get("amount").toString()) + sendAmount);

                    if (command.length > 3) {
                        channel.sendMessage(sendAmount + "<:sharqcoin:413785618573819905> sent to " + targetUser.get("Name").toString() + ". Message: " + command[3]).queue();

                    } else {
                        channel.sendMessage(sendAmount + "<:sharqcoin:413785618573819905> sent to " + targetUser.get("Name").toString() + ".").queue();

                    }

                    FileWriter jsonFile = new FileWriter("./sharqcoin.json");
                    jsonFile.write(users.toString());
                    jsonFile.flush();
                    jsonFile.close();
                }


//                if (userFound != null) {
//
//
//                    org.json.simple.JSONObject targetUser = findUser(command[2].substring(2, command[2].length() - 1), users);
//                    if (targetUser != null) {
//                        if (sendAmount > Integer.parseInt(userFound.get("amount").toString())) {
//                            channel.sendMessage("Insufficient funds!").queue();
//                        } else {
//                            userFound.put("amount", Integer.parseInt(userFound.get("amount").toString()) - sendAmount);
//                            targetUser.put("amount", Integer.parseInt(targetUser.get("amount").toString()) + sendAmount);
//
//                            if (command[3] != null) {
//                                channel.sendMessage(sendAmount + "<sharqcoin:413865986211708950> sent to " + targetUser.get("Name").toString() + ". Message: " + command[3]).queue();
//
//                            } else {
//                                channel.sendMessage(sendAmount + "<sharqcoin:413865986211708950> sent to " + targetUser.get("Name").toString() + ".").queue();
//
//                            }
//
//                            FileWriter jsonFile = new FileWriter("./sharqcoin.json");
//                            jsonFile.write(users.toString());
//                            jsonFile.flush();
//                            jsonFile.close();
//                        }
//                    } else {
//                        channel.sendMessage("Recipient must have a sharqcoin wallet to recieve funds!").queue();
//                    }
//
//
//                } else {
//                    channel.sendMessage("You must create a sharqcoin wallet to send funds!").queue();
//
//                }


            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }


        } else if (message.getAuthor().getId().equalsIgnoreCase("")) {

        } else if (command[0].equalsIgnoreCase("!top5")) {


            JSONParser parser = new JSONParser();
            try {
                Object obj = parser.parse(new FileReader("./sharqcoin.json"));
                org.json.simple.JSONArray users = (org.json.simple.JSONArray) obj;


                ArrayList<org.json.simple.JSONObject> usersArray = new ArrayList<>();
                for (Object u : users) {
                    org.json.simple.JSONObject user = (org.json.simple.JSONObject) u;
                    usersArray.add(user);
                }

                usersArray.sort(Comparator.comparingDouble(a -> Double.parseDouble(a.get("amount").toString())));

                EmbedBuilder sharqCoinReply = new EmbedBuilder();
                sharqCoinReply.setTitle("Top 5 SharqCoin Net Worth");
                sharqCoinReply.setDescription("");
                sharqCoinReply.setColor(Color.decode("#ffd700"));


                for (int i = usersArray.size() - 1; i > usersArray.size() - 6; i--) {
                    sharqCoinReply.addField(usersArray.get(i).get("Name").toString(), usersArray.get(i).get("amount").toString(), false);
                }
                channel.sendMessage(sharqCoinReply.build()).queue();

            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }


        } else if (command[0].equalsIgnoreCase("!notify")) {
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

    private org.json.simple.JSONObject findUser(String id, org.json.simple.JSONArray users) {
        for (Object u : users) {
            org.json.simple.JSONObject user = (org.json.simple.JSONObject) u;
            if (id.equalsIgnoreCase(user.get("id").toString())) {
                return (org.json.simple.JSONObject) u;
            }
        }
        return null;
    }

    private void createWallet(User author) {
        JSONParser parser = new JSONParser();

        try {

            Object obj = parser.parse(new FileReader("./sharqcoin.json"));
            org.json.simple.JSONArray users = (org.json.simple.JSONArray) obj;

            org.json.simple.JSONObject existingUser = findUser(author.getId(), users);
            if (existingUser == null) {
                org.json.simple.JSONObject newUser = new org.json.simple.JSONObject();
                newUser.replace("Name", author.getName());
                newUser.replace("id", author.getId());
                newUser.replace("amount", 0.0);
                users.add(newUser);

                FileWriter jsonFile = new FileWriter("./sharqcoin.json");
                jsonFile.write(users.toString());
                jsonFile.flush();
                jsonFile.close();
            } else {
                existingUser.replace("Name", author.getName());
                FileWriter jsonFile = new FileWriter("./sharqcoin.json");
                jsonFile.write(users.toString());
                jsonFile.flush();
                jsonFile.close();
            }


        } catch (IOException | ParseException e) {
            e.printStackTrace();
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
                playerField.append(p).append("\n");
            }
            messageReply.addField("Players: ", playerField.toString(), false);

        }
        channel.sendMessage(messageReply.build()).queue();
    }
}
