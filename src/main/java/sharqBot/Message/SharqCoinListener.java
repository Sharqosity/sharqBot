package sharqBot.Message;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;

public class SharqCoinListener extends ListenerAdapter {

    private final double DUEL_REWARD = 1;
    private final double DOUBLES_REWARD = 4;
    private final double THREE_DAY_STREAK_REWARD = 5;

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        Message message = event.getMessage();

        if (!event.isFromType(ChannelType.TEXT)) {
            return;
        }
//        else if (event.getAuthor().isBot() && !message.getAuthor().getId().equalsIgnoreCase("177022387903004673")) {
//            return;
//        }

        String content = message.getRawContent();
        MessageChannel channel = event.getChannel();

        String[] command = content.split(" ", 4);


        if (command[0].equalsIgnoreCase("!wallet")) {

            JSONObject userFound = getUser(message.getAuthor());

            assert userFound != null;
            channel.sendMessage("You have " + userFound.get("amount") + "<:sharqcoin:413785618573819905> in your wallet.").queue();

        } else if (command[0].equalsIgnoreCase("!send")) {

            double sendAmount = Double.parseDouble(command[1]);

            try {
                User recipient = (message.getMentionedUsers().get(0));
                JSONObject targetUser = getUser(recipient);
                JSONObject userFound = getUser(message.getAuthor());

                assert userFound != null;
                if (sendAmount > 0 && sendAmount > Double.parseDouble(userFound.get("amount").toString())) {
                    channel.sendMessage("Insufficient funds!").queue();
                } else {
                    JSONParser parser = new JSONParser();
                    Object obj = parser.parse(new FileReader("./sharqcoin.json"));
                    JSONArray users = (JSONArray) obj;

                    users.remove(userFound);
                    users.remove(targetUser);

                    userFound.put("amount", Double.parseDouble(userFound.get("amount").toString()) - sendAmount);
                    assert targetUser != null;
                    targetUser.put("amount", Double.parseDouble(targetUser.get("amount").toString()) + sendAmount);

                    if (command.length > 3) {
                        channel.sendMessage(sendAmount + "<:sharqcoin:413785618573819905> sent to " + targetUser.get("Name").toString() + ". Message: " + command[3]).queue();
                    } else {
                        channel.sendMessage(sendAmount + "<:sharqcoin:413785618573819905> sent to " + targetUser.get("Name").toString() + ".").queue();
                    }

                    users.add(userFound);
                    users.add(targetUser);


                    FileWriter jsonFile = new FileWriter("./sharqcoin.json");
                    jsonFile.write(users.toJSONString());
                    jsonFile.flush();
                    jsonFile.close();
                }


            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }

        } else if (command[0].equalsIgnoreCase("!top5")) {


            JSONParser parser = new JSONParser();
            try {
                Object obj = parser.parse(new FileReader("./sharqcoin.json"));
                JSONArray users = (JSONArray) obj;


                ArrayList<JSONObject> usersArray = new ArrayList<>();
                for (Object u : users) {
                    JSONObject user = (JSONObject) u;
                    usersArray.add(user);
                }

                usersArray.sort(Comparator.comparingDouble(a -> Double.parseDouble(a.get("amount").toString())));

                EmbedBuilder sharqCoinReply = new EmbedBuilder();
                sharqCoinReply.setTitle("Top 5 SharqCoin Net Worth");
//                sharqCoinReply.setDescription("<:sharqcoin:413785618573819905> <:sharqcoin:413785618573819905> <:sharqcoin:413785618573819905>");
                sharqCoinReply.setColor(Color.decode("#ffd700"));


                for (int i = usersArray.size() - 1; i > usersArray.size() - 6; i--) {
                    sharqCoinReply.addField(usersArray.get(i).get("Name").toString(), usersArray.get(i).get("amount").toString() + "<:sharqcoin:413785618573819905>", false);
                }
                channel.sendMessage(sharqCoinReply.build()).queue();

            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }

        } else if (message.getAuthor().getId().equalsIgnoreCase("177022387903004673") && command[1].equalsIgnoreCase("pickup")) { //message sent by pubobot

            //get player's users
            User player1 = message.getMentionedUsers().get(0);
            User player2 = message.getMentionedUsers().get(1);


            double player1Reward, player2Reward;
            //check mode
            if (command[0].equalsIgnoreCase("**1v1**")) {
                player1Reward = 1;
                player2Reward = 1;
            } else if (command[0].equalsIgnoreCase("**2v2**")) {
                player1Reward = 4;
                player2Reward = 4;
            } else {
                return;
            }

            //time to hand out rewards
            //check both player's lastplayed pickup
            JSONObject player1JSON = getUser(player1);
            JSONObject player2JSON = getUser(player2);

//            player1JSON.putIfAbsent("lastPlayedPickup", java.time.LocalDateTime);
            player1JSON.putIfAbsent("lastPlayedPickup", LocalDateTime.of(0, 1, 1, 1, 0).toString());
            player2JSON.putIfAbsent("lastPlayedPickup", LocalDateTime.of(0, 1, 1, 1, 0).toString());


            player1JSON.putIfAbsent("streak", 0);
            player2JSON.putIfAbsent("streak", 0);
            int player1Streak = Integer.parseInt(player1JSON.get("streak").toString());
            int player2Streak = Integer.parseInt(player2JSON.get("streak").toString());


            boolean player1Receives = true;
            boolean player2Receives = true;

            long minutesPlayer1 = ChronoUnit.MINUTES.between(LocalDateTime.parse(player1JSON.get("lastPlayedPickup").toString()), LocalDateTime.now());
            long minutesPlayer2 = ChronoUnit.MINUTES.between(LocalDateTime.parse(player2JSON.get("lastPlayedPickup").toString()), LocalDateTime.now());

            if (minutesPlayer1 < 10L) {
                player1Receives = false;
                channel.sendMessage("It hasn't even been 10 minutes since your last pickup, what are you doing dude").queue();
            }
            if (minutesPlayer2 < 10L) {
                player2Receives = false;
                channel.sendMessage("It hasn't even been 10 minutes since your last pickup, what are you doing dude").queue();
            }


            try {

                JSONParser parser = new JSONParser();
                Object obj = parser.parse(new FileReader("./sharqcoin.json"));
                JSONArray users = (JSONArray) obj;

                users.remove(player1JSON);
                users.remove(player2JSON);

                player1Reward = getPlayerReward(player1Reward, player1JSON, player1Receives);
                player2Reward = getPlayerReward(player2Reward, player2JSON, player2Receives);

                String response = "";
                if (player1Receives) {
                    response += rewardMessage(player1JSON, player1Reward, player1Streak);
                }
                if (player2Receives) {
                    response += rewardMessage(player2JSON, player2Reward, player2Streak);
                }

                player1JSON.put("lastPlayedPickup", LocalDateTime.now().toString());
                player2JSON.put("lastPlayedPickup", LocalDateTime.now().toString());

                channel.sendMessage(response).queue();


                users.add(player1JSON);
                users.add(player2JSON);

                FileWriter jsonFile = new FileWriter("./sharqcoin.json");
                jsonFile.write(users.toJSONString());
                jsonFile.flush();
                jsonFile.close();


            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        }


    }

    private double getPlayerReward(double playerReward, JSONObject playerJSON, boolean playerReceives) {
        if (playerReceives) {
            if (Integer.parseInt(playerJSON.get("streak").toString()) == 0) {
                playerJSON.put("streak", 1);
            } else if (LocalDateTime.now().getDayOfMonth() - (LocalDateTime.parse(playerJSON.get("lastPlayedPickup").toString())).getDayOfMonth() == 1) {
                playerJSON.put("streak", Integer.parseInt(playerJSON.get("streak").toString()) + 1);
                if (Integer.parseInt(playerJSON.get("streak").toString()) % 3 == 0) {
                    playerReward += THREE_DAY_STREAK_REWARD;
                }
            } else if (LocalDateTime.now().getDayOfMonth() - (LocalDateTime.parse(playerJSON.get("lastPlayedPickup").toString())).getDayOfMonth() == 0) {

            } else {
                playerJSON.put("streak", 0);
            }

            playerJSON.put("amount", Double.parseDouble(playerJSON.get("amount").toString()) + playerReward);
        }
        return playerReward;
    }

    private String rewardMessage(JSONObject user, double reward, int streak) {
        String response = "";
        response += "Pickup rewards for " + user.get("Name").toString() + ": " + reward + "<:sharqcoin:413785618573819905>";
        if (streak != 0 && streak % 3 == 0) {
            response += ", " + streak + " day streak! \uD83D\uDD25 \n";
        } else {
            response += "\n";
        }
        return response;
    }

    private JSONObject getUser(User targetUser) {
        JSONParser parser = new JSONParser();

        try {

            Object obj = parser.parse(new FileReader("./sharqcoin.json"));
            JSONArray users = (JSONArray) obj;


            for (Object u : users) {
                JSONObject user = (JSONObject) u;
                if (targetUser.getId().equalsIgnoreCase(user.get("id").toString())) {
                    user.put("Name", targetUser.getName());
                    FileWriter jsonFile = new FileWriter("./sharqcoin.json");
                    jsonFile.write(users.toJSONString());
                    jsonFile.flush();
                    jsonFile.close();
                    return user;
                }
            }


            JSONObject newUser = new JSONObject();
            newUser.put("Name", targetUser.getName());
            newUser.put("id", targetUser.getId());
            newUser.put("amount", 0.0);
            users.add(newUser);

            FileWriter jsonFile = new FileWriter("./sharqcoin.json");
            jsonFile.write(users.toJSONString());
            jsonFile.flush();
            jsonFile.close();
            return newUser;

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

}
