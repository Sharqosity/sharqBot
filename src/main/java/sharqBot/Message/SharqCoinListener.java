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
import java.io.*;
import java.nio.channels.FileChannel;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SharqCoinListener extends ListenerAdapter {

    private final int DUEL_REWARD = 100;
    private final int DOUBLES_REWARD = 400;
    private final int THREE_DAY_STREAK_REWARD = 500;


    public SharqCoinListener() {
        Runnable backupSharqCoinDatabase = () -> {
            try {
                FileChannel src = new FileInputStream("./sharqcoin.json").getChannel();
                String fileName = (LocalDateTime.now().toString()+ ".json").replaceAll(":",".");
                File copyTo = new File("./sharqcoin backups/" + fileName);
                copyTo.createNewFile();
                FileChannel dest = new FileOutputStream(copyTo.getPath()).getChannel();
                dest.transferFrom(src,0,src.size());
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("sharqcoin.json backed up!");
        };

        ScheduledExecutorService backupService = Executors.newSingleThreadScheduledExecutor();
        backupService.scheduleAtFixedRate(backupSharqCoinDatabase, 1, 1, TimeUnit.HOURS);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {


//        if (!event.isFromType(ChannelType.TEXT)) {
//            return;
//        } else if (event.getAuthor().isBot() && !message.getAuthor().getId().equalsIgnoreCase("177022387903004673")) {
//            return;
//        }

        if(event.isFromType(ChannelType.TEXT)) {

            Message message = event.getMessage();

            String content = message.getRawContent();
            MessageChannel channel = event.getChannel();

            String[] command = content.split(" ", 4);

            if (command[0].equalsIgnoreCase("!faq")) {
                EmbedBuilder sharqCoinReply = new EmbedBuilder();
                sharqCoinReply.setTitle("Sharqcoin FAQ");
                sharqCoinReply.setDescription("hottest digital meme currency right now");
                sharqCoinReply.setThumbnail("https://i.imgur.com/jg49fpv.png");
//            sharqCoinReply.setImage("https://i.imgur.com/jg49fpv.png");
                sharqCoinReply.setColor(Color.decode("#FFDF00"));
                sharqCoinReply.addField("What is Sharqcoin?", "-Sharqcoin is a fun way of rewarding participation and activity in the sushiflex community!", false);
                sharqCoinReply.addField("How can I get Sharqcoin?", "- Play pickups! You earn 1<:sharqcoin:413785618573819905> everytime you start a duel pickup, 4<:sharqcoin:413785618573819905> for doubles. Hit a 3-day streak:fire: for a 5<:sharqcoin:413785618573819905> reward!\n" +
                        "- Win community events, including tournaments, mapping competitions, and more for big sharqcoin payouts!\n" +
                        "- Help organize or stream aforementioned community events!\n" +
                        "- Host Sushiflex servers - 10<:sharqcoin:413785618573819905> per server per month!", false);
                sharqCoinReply.addField("What can I do with Sharqcoin?", "- You can send sharqcoin to other users with !send amount @user\n" +
                        "- Bet on sushiflex tournament games (planned feature coming soon)\n" +
                        "- Hoard it and become a millionaire", false);
                sharqCoinReply.addField("Commands", "Check your current sharqcoin balance with !wallet, and view the leaderboards with !top5", false);
                channel.sendMessage(sharqCoinReply.build()).queue();

            } else if (command[0].equalsIgnoreCase("!wallet")) {

                JSONObject userFound = getUser(message.getAuthor());

                assert userFound != null;
                System.out.println(userFound.get("amount").toString());
                channel.sendMessage("You have " + ((double)Integer.parseInt(userFound.get("amount").toString()))/100 + "<:sharqcoin:413785618573819905> in your wallet.").queue();
                System.out.println(((double)Integer.parseInt(userFound.get("amount").toString()))/100);
            } else if (command[0].equalsIgnoreCase("!send")) {

                if(Double.parseDouble(command[1]) < 0.01) {
                    channel.sendMessage("Please send an amount higher than 0.01!").queue();
                    return;
                }

                int sendAmount = (int)(Math.round(Double.parseDouble(command[1])*100));
                try {
                    User recipient = (message.getMentionedUsers().get(0));
                    JSONObject targetUser = getUser(recipient);
                    JSONObject userFound = getUser(message.getAuthor());

                    assert userFound != null;
                    if (sendAmount > Integer.parseInt(userFound.get("amount").toString())) {
                        channel.sendMessage("Insufficient funds!").queue();
                    } else if (sendAmount > 0) {
                        JSONParser parser = new JSONParser();
                        Object obj = parser.parse(new FileReader("./sharqcoin.json"));
                        JSONArray users = (JSONArray) obj;

                        System.out.println(users);
                        System.out.println(userFound);
                        System.out.println(targetUser);

                        users.remove(getUser(recipient));
                        users.remove(getUser(message.getAuthor()));
                        System.out.println(users);


                        userFound.put("amount", Integer.parseInt(userFound.get("amount").toString()) - sendAmount);
                        assert targetUser != null;
                        targetUser.put("amount", Integer.parseInt(targetUser.get("amount").toString()) + sendAmount);

                        if (command.length > 3) {
                            channel.sendMessage(((double)sendAmount)/100 + "<:sharqcoin:413785618573819905> sent to " + targetUser.get("Name").toString() + ". Message: " + command[3]).queue();
                        } else {
                            channel.sendMessage(((double)sendAmount/100) + "<:sharqcoin:413785618573819905> sent to " + targetUser.get("Name").toString() + ".").queue();
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
                        sharqCoinReply.addField(usersArray.get(i).get("Name").toString(), ((double)Integer.parseInt(usersArray.get(i).get("amount").toString()))/100 + "<:sharqcoin:413785618573819905>", false);
                    }
                    channel.sendMessage(sharqCoinReply.build()).queue();

                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                }

            } else if (message.getAuthor().getId().equalsIgnoreCase("177022387903004673") && command[1].equalsIgnoreCase("pickup")) { //message sent by pubobot

                try {
                    JSONParser parser = new JSONParser();
                    Object obj = parser.parse(new FileReader("./sharqcoin.json"));
                    JSONArray users = (JSONArray) obj;

                    //get player's users
                    User player1 = message.getMentionedUsers().get(0);
                    User player2 = message.getMentionedUsers().get(1);


                    int player1Reward, player2Reward;
                    //check mode
                    if (command[0].equalsIgnoreCase("**1v1**")) {
                        player1Reward = DUEL_REWARD;
                        player2Reward = DUEL_REWARD;
                    } else if (command[0].equalsIgnoreCase("**2v2**")) {
                        player1Reward = DOUBLES_REWARD;
                        player2Reward = DOUBLES_REWARD;
                    } else {
                        return;
                    }

                    //time to hand out rewards
                    //check both player's lastplayed pickup
                    JSONObject player1JSON = getUser(player1);
                    JSONObject player2JSON = getUser(player2);

                    users.remove(player1JSON);
                    users.remove(player2JSON);

//            player1JSON.putIfAbsent("lastPlayedPickup", java.time.LocalDateTime);
//                player1JSON.putIfAbsent("lastPlayedPickup", LocalDateTime.of(0, 1, 1, 1, 0).toString());
//                player2JSON.putIfAbsent("lastPlayedPickup", LocalDateTime.of(0, 1, 1, 1, 0).toString());
//                player1JSON.putIfAbsent("streak", 0);
//                player2JSON.putIfAbsent("streak", 0);

                    int player1Streak = Integer.parseInt(player1JSON.get("streak").toString());
                    int player2Streak = Integer.parseInt(player2JSON.get("streak").toString());


                    boolean player1Receives = true;
                    boolean player2Receives = true;

                    long minutesPlayer1 = ChronoUnit.MINUTES.between(LocalDateTime.parse(player1JSON.get("lastPlayedPickup").toString()), LocalDateTime.now());
                    long minutesPlayer2 = ChronoUnit.MINUTES.between(LocalDateTime.parse(player2JSON.get("lastPlayedPickup").toString()), LocalDateTime.now());

                    if (minutesPlayer1 < 10L) {
                        player1Receives = false;
                        channel.sendMessage("It hasn't even been 10 minutes since your last pickup <@" + player1JSON.get("id") + ">, what are you doing dude").queue();
                    }
                    if (minutesPlayer2 < 10L) {
                        player2Receives = false;
                        channel.sendMessage("It hasn't even been 10 minutes since your last pickup <@" + player2JSON.get("id") + ">, what are you doing dude").queue();
                    }





//                    player1Reward = getPlayerReward(player1Reward, player1JSON, player1Receives).getReward();
//                    player2Reward = getPlayerReward(player2Reward, player2JSON, player2Receives).getReward();
//
//                    boolean player1StreakEarned = getPlayerReward(player1Reward,player1JSON,player1Receives).getStreakEarned(); //spaghetti code
//                    boolean player2StreakEarned = getPlayerReward(player2Reward,player2JSON,player2Receives).getStreakEarned(); //spaghetti code


                    String response = "";
                    response += getPlayerReward(player1Reward,player1JSON,player1Receives);
                    response += getPlayerReward(player2Reward,player2JSON,player2Receives);

//                    response += rewardMessage(player1JSON, player1Reward, player1Streak,player1StreakEarned);
//
//                    response += rewardMessage(player2JSON, player2Reward, player2Streak,player2StreakEarned);


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
        } else if (event.isFromType(ChannelType.PRIVATE)) {
            Message message = event.getMessage();

            String content = message.getRawContent();
            MessageChannel channel = event.getChannel();

            String[] command = content.split(" ", 4);

            if(command[0].equalsIgnoreCase("!bet")) {

            } else if (command[0].equalsIgnoreCase("!withdrawbet")){

            }
        } else {
            return;
        }


    }

    private String getPlayerReward(int playerReward, JSONObject playerJSON, boolean playerReceives) {
        String response = "";

        int streak = Integer.parseInt(playerJSON.get("streak").toString());
        if (playerReceives) {

            response += "Pickup rewards for " + playerJSON.get("Name").toString() + ": " + ((double)playerReward)/100 + "<:sharqcoin:413785618573819905>";

            if (streak == 0) {
                playerJSON.put("streak", 1);
                response += "\n";
            } else if (LocalDateTime.now().getDayOfMonth() - (LocalDateTime.parse(playerJSON.get("lastPlayedPickup").toString())).getDayOfMonth() == 1) {
                if ((streak + 1) % 3 == 0) {
                    playerReward += THREE_DAY_STREAK_REWARD;
                    response += ". " + streak + " day streak! \uD83D\uDD25 +" + ((double)THREE_DAY_STREAK_REWARD)/100 + "<:sharqcoin:413785618573819905> \n";

                } else {
                    response += "\n";
                }

                playerJSON.put("streak", Integer.parseInt(playerJSON.get("streak").toString()) + 1);

            } else if (LocalDateTime.now().getDayOfMonth() - (LocalDateTime.parse(playerJSON.get("lastPlayedPickup").toString())).getDayOfMonth() == 0) {
                response += "\n";
            } else {
                playerJSON.put("streak", 0);
                response += "\n";
            }

            playerJSON.put("amount", Integer.parseInt(playerJSON.get("amount").toString()) + playerReward);


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
            newUser.put("amount", 0);
            newUser.put("streak", 0);
            newUser.put("lastPlayedPickup", LocalDateTime.of(0, 1, 1, 1, 0).toString());
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
