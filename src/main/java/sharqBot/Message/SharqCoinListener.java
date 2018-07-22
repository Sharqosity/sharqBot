package sharqBot.Message;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.io.*;
import java.nio.channels.FileChannel;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SharqCoinListener extends ListenerAdapter {

    private final int DUEL_REWARD = 100;
    private final int DOUBLES_REWARD = 300;
    private final int CTF_REWARD = 700;
    private final int CA_REWARD = 500;
    private final int TDM_REWARD = 700;
    private final int THREE_DAY_STREAK_REWARD = 500;
    private final int MODE_BONUS = 300;

    private final int PROMOTE_COST = 399;

    private ArrayList<OpenBet> openBets = new ArrayList<>();
    private ArrayList<OpenBet> closedBets = new ArrayList<>();

    public SharqCoinListener(JDA api) {

        Runnable sushiSunday = () -> {
            Guild sushiServer = api.getGuildById("435908303181185024");
            MessageChannel announcements = sushiServer.getTextChannelById("435909048081317888");
            int weeksElapsed = 0;
            int[] bonusModeArray = {-1, -1, -1, -1};
            weeksElapsed = getWeeksElapsed();
            Random gen = new Random(weeksElapsed / 4);
            for (int i = 0; i <= weeksElapsed % 4; i++) {
                int nextMode = gen.nextInt(4);
                while (nextMode == bonusModeArray[0] || nextMode == bonusModeArray[1] || nextMode == bonusModeArray[2])
                    nextMode = gen.nextInt(4);

                bonusModeArray[i] = nextMode;
            }
            String mode = "";
            switch (bonusModeArray[weeksElapsed % 4]) {
                case 0: mode = "doubles"; break;
                case 1: mode = "clan arena"; break;
                case 2: mode = "TDM"; break;
                case 3: mode = "CTF"; break;
            }
            System.out.println(mode);
            announcements.sendMessage("@everyone\n" +
                    "\uD83C\uDF63 **SUSHI SUNDAY HAS BEGUN** \uD83C\uDF63:\n" +
                    "Come together for some PUGs for the next **5 hours**. \n" +
                    "\n" +
                    "\uD83C\uDF63 **Bonus Mode is: " + mode + "** \n" +
                    "Enjoy bonus SharqCoins when you queue for " + mode + ".\n" +
                    "\n" +
                    "**2X<:sharqcoin:413785618573819905> SharqCoins** are now Enabled.").queue();
        };


        Runnable backupSharqCoinDatabase = () -> {
            try {
                FileChannel src = new FileInputStream("./sharqcoin.json").getChannel();
                String fileName = (LocalDateTime.now().toString() + ".json").replaceAll(":", ".");
                File copyTo = new File("./sharqcoin backups/" + fileName);
                copyTo.createNewFile();
                FileChannel dest = new FileOutputStream(copyTo.getPath()).getChannel();
                dest.transferFrom(src, 0, src.size());
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("sharqcoin.json backed up!");
        };

//        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
        executorService.scheduleAtFixedRate(backupSharqCoinDatabase, 1, 1, TimeUnit.HOURS);


        LocalDateTime nextSunday;
        long initialDelay = 0L;
        if(LocalTime.now().isBefore(LocalTime.of(13,0))) {
            nextSunday = LocalDateTime.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            initialDelay = ChronoUnit.MINUTES.between(LocalDateTime.now(), LocalDateTime.of(nextSunday.toLocalDate(),LocalTime.of(13,0)));

        } else if (LocalTime.now().isAfter(LocalTime.of(13,0))) {
            nextSunday = LocalDateTime.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
            initialDelay = ChronoUnit.MINUTES.between(LocalDateTime.now(), LocalDateTime.of(nextSunday.toLocalDate(),LocalTime.of(13,0)));


        }
        executorService.scheduleAtFixedRate(sushiSunday,initialDelay,10080,TimeUnit.MINUTES);
    }

    private static int getWeeksElapsed() {

        LocalDateTime start = LocalDateTime.of(2018, 5, 26, 1, 0); //day before start of sushi sundays
        LocalDateTime now = LocalDateTime.now();

        return (int) ChronoUnit.WEEKS.between(start, now);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        if (event.isFromType(ChannelType.TEXT) || (event.isFromType(ChannelType.PRIVATE))) {

            Message message = event.getMessage();

            String content = message.getRawContent();
            MessageChannel channel = event.getChannel();

            String[] command = content.split(" ", 4);

            if (command[0].equalsIgnoreCase("!faq")) {
                EmbedBuilder sharqCoinReply = new EmbedBuilder();
                sharqCoinReply.setTitle("Sharqcoin FAQ");
                sharqCoinReply.setDescription("hottest digital meme currency right now");
                sharqCoinReply.setThumbnail("https://i.imgur.com/jg49fpv.png");
                sharqCoinReply.setColor(Color.decode("#FFDF00"));
                sharqCoinReply.addField("What is Sharqcoin?", "-Sharqcoin is a fun way of rewarding participation and activity in the sushiflex community!", false);
                sharqCoinReply.addField("How can I get Sharqcoin?", "- Play pickups! You earn 1<:sharqcoin:413785618573819905> everytime you start a duel pickup, 4<:sharqcoin:413785618573819905> for doubles. Hit a 3-day streak:fire: for a 5<:sharqcoin:413785618573819905> reward!\n" +
                        "- Win community events, including tournaments, mapping competitions, and more for big sharqcoin payouts!\n" +
                        "- Help organize or stream aforementioned community events!\n" +
                        "- Host Sushiflex servers - 15<:sharqcoin:413785618573819905> per server per month!", false);
                sharqCoinReply.addField("What can I do with Sharqcoin?", "- You can send sharqcoin to other users with `!send amount @user`\n" +
                        "- Bet on sushiflex tournament games\n" +
                        "- Hoard it and become a millionaire", false);
                sharqCoinReply.addField("Other Commands", "Check your current sharqcoin balance with `!wallet`, and view the leaderboards with `!top5`", false);
                channel.sendMessage(sharqCoinReply.build()).queue();


            } else if (command[0].equalsIgnoreCase("!wallet")) {
                JSONObject userFound = JSONDude.getUser(message.getAuthor());
                assert userFound != null;
                channel.sendMessage(new EmbedBuilder().setTitle(userFound.get("Name").toString() + ", you have " + ((double) Integer.parseInt(userFound.get("amount").toString())) / 100 + "<:sharqcoin:413785618573819905> in your wallet.").build()).queue();


            } else if (command[0].equalsIgnoreCase("!send")) {
                if (event.isFromType(ChannelType.PRIVATE)) {
                    return;
                }
                EmbedBuilder sendReply = new EmbedBuilder();
                if (message.getAuthor() == message.getMentionedUsers().get(0)) {
                    sendReply.setTitle("You cannot send to yourself!");
                    channel.sendMessage(sendReply.build()).queue();
                    return;
                } else if (Double.parseDouble(command[1]) < 0.01) {
                    sendReply.setTitle("Please send an amount higher than 0.01!");
                    channel.sendMessage(sendReply.build()).queue();
                    return;
                }
                int sendAmount = (int) (Math.round(Double.parseDouble(command[1]) * 100));
                try {
                    User recipient = (message.getMentionedUsers().get(0));
                    JSONObject targetUser = JSONDude.getUser(recipient);
                    JSONObject userFound = JSONDude.getUser(message.getAuthor());

                    assert userFound != null;
                    if (sendAmount > Integer.parseInt(userFound.get("amount").toString())) {
                        sendReply.setTitle("Insufficient funds!");
                        channel.sendMessage(sendReply.build()).queue();

                    } else if (sendAmount > 0) {
                        JSONParser parser = new JSONParser();
                        Object obj = parser.parse(new FileReader("./sharqcoin.json"));
                        JSONArray users = (JSONArray) obj;
                        users.remove(JSONDude.getUser(recipient));
                        users.remove(JSONDude.getUser(message.getAuthor()));

                        userFound.put("amount", Integer.parseInt(userFound.get("amount").toString()) - sendAmount);
                        assert targetUser != null;
                        targetUser.put("amount", Integer.parseInt(targetUser.get("amount").toString()) + sendAmount);

                        if (command.length > 3) {
                            sendReply.setTitle(((double) sendAmount) / 100 + "<:sharqcoin:413785618573819905> sent to " + targetUser.get("Name").toString() + ". Message: " + command[3]);
                            channel.sendMessage(sendReply.build()).queue();

                        } else {
                            sendReply.setTitle(((double) sendAmount / 100) + "<:sharqcoin:413785618573819905> sent to " + targetUser.get("Name").toString() + ".");
                            channel.sendMessage(sendReply.build()).queue();

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
                        sharqCoinReply.addField(usersArray.get(i).get("Name").toString(), ((double) Integer.parseInt(usersArray.get(i).get("amount").toString())) / 100 + "<:sharqcoin:413785618573819905>", false);
                    }
                    channel.sendMessage(sharqCoinReply.build()).queue();
                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                }


            } else if (message.getAuthor().getId().equalsIgnoreCase("177022387903004673") && command[1].equalsIgnoreCase("pickup")) { //message sent by pubobot
                if (event.isFromType(ChannelType.PRIVATE)) {
                    return;
                }
                try {
                    JSONParser parser = new JSONParser();
                    Object obj = parser.parse(new FileReader("./sharqcoin.json"));
                    JSONArray users = (JSONArray) obj;
                    StringBuilder response = new StringBuilder();

                    for (User u : message.getMentionedUsers()) {
                        int mode = 0;
                        int playerReward;
                        if (command[0].equalsIgnoreCase("**1v1**")) {
                            playerReward = DUEL_REWARD;
                            mode = 1;
                        } else if (command[0].equalsIgnoreCase("**2v2**")) {
                            playerReward = DOUBLES_REWARD;
                            mode = 2;
                        } else if (command[0].equalsIgnoreCase("**CA**")) {
                            playerReward = CA_REWARD;
                            mode = 3;
                        } else if (command[0].equalsIgnoreCase("**TDM**")) {
                            playerReward = TDM_REWARD;
                            mode = 4;
                        } else if (command[0].equalsIgnoreCase("**CTF**")) {
                            playerReward = CTF_REWARD;
                            mode = 5;
                        } else {
                            channel.sendMessage("Pickup mode not found!").queue();
                            return;
                        }

                        //Checks if current time is during Sushi Sunday. Do not run this in a non-EST timezone lol
                        if (LocalDateTime.now().getDayOfWeek() == DayOfWeek.SUNDAY) {
                            if (LocalDateTime.now().toLocalTime().isAfter(LocalTime.of(13, 0)) && LocalDateTime.now().toLocalTime().isBefore(LocalTime.of(18, 0)))
                                playerReward *= 2;
                        }

                        JSONObject playerJSON = JSONDude.getUser(u);
                        users.remove(playerJSON);
                        boolean playerReceives = true;
                        long minutesSinceLastPickup = ChronoUnit.MINUTES.between(LocalDateTime.parse(playerJSON.get("lastPlayedPickup").toString()), LocalDateTime.now());

                        if (mode > 1) {
                            if (minutesSinceLastPickup < 20L) {
                                playerReceives = false;
                                channel.sendMessage("It hasn't even been 20 minutes since your last pickup <@" + playerJSON.get("id") + ">, what are you doing dude").queue();
                            }
                        } else {
                            if (minutesSinceLastPickup < 10L) {
                                playerReceives = false;
                                channel.sendMessage("It hasn't even been 10 minutes since your last pickup <@" + playerJSON.get("id") + ">, what are you doing dude").queue();
                            }
                        }
                        response.append(getPlayerReward(playerReward, playerJSON, playerReceives, mode));
                        playerJSON.put("lastPlayedPickup", LocalDateTime.now().toString());
                        //tracks amount of pickups played
                        switch (mode) {
                            case 1:
                                playerJSON.put("pickupsPlayed_1v1", playerJSON.get("pickupsPlayed_1v1").toString() + 1);
                                break;
                            case 2:
                                playerJSON.put("pickupsPlayed_2v2", playerJSON.get("pickupsPlayed_2v2").toString() + 1);
                                break;
                            case 3:
                                playerJSON.put("pickupsPlayed_CA", playerJSON.get("pickupsPlayed_CA").toString() + 1);
                                break;
                            case 4:
                                playerJSON.put("pickupsPlayed_TDM", playerJSON.get("pickupsPlayed_TDM").toString() + 1);
                                break;
                            case 5:
                                playerJSON.put("pickupsPlayed_CTF", playerJSON.get("pickupsPlayed_CTF").toString() + 1);
                                break;
                        }

                        users.add(playerJSON);

                        FileWriter jsonFile = new FileWriter("./sharqcoin.json");
                        jsonFile.write(users.toJSONString());
                        jsonFile.flush();
                        jsonFile.close();
                    }
                    channel.sendMessage(response.toString()).queue();
                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                }
            } else if (command[0].equalsIgnoreCase("!openBet")) { //!openbet player1 player2 event

                if (event.isFromType(ChannelType.PRIVATE)) {
                    return;
                } else if (command.length < 4) {
                    return;
                }
                boolean roleFound = false;
                for (Role r : message.getGuild().getMember(message.getAuthor()).getRoles()) {
                    if (r.getName().equalsIgnoreCase("Bet Organizer") || r.getName().equalsIgnoreCase("Discord Management")) {
                        roleFound = true;
                        break;
                    }
                }
                if (!roleFound) {
                    return;
                }
                String player1Name = command[1];
                String player2Name = command[2];
                String eventName = command[3];

                if (player1Name.equalsIgnoreCase(player2Name)) {
                    return;
                }
                for (OpenBet o : openBets) {
                    if (player1Name.equalsIgnoreCase(o.getPlayer1()) || player1Name.equalsIgnoreCase(o.getPlayer2()) || player2Name.equalsIgnoreCase(o.getPlayer1()) || player2Name.equalsIgnoreCase(o.getPlayer2())) {
                        return;
                    }
                }
                openBets.add(new OpenBet(player1Name, player2Name, eventName));
                EmbedBuilder newOpenBet = new EmbedBuilder();
                newOpenBet.setTitle("New bet opened!");
                newOpenBet.setDescription("PM me with `!bet amount playerName` to bet!");
                newOpenBet.setColor(Color.decode("#6A018C"));
                newOpenBet.addField(eventName, player1Name + " vs. " + player2Name, false);
                channel.sendMessage(newOpenBet.build()).queue();

            } else if (command[0].equalsIgnoreCase("!closebet")) { //!closebet onePlayerName
                if (event.isFromType(ChannelType.PRIVATE)) {
                    return;
                }

                String onePlayer = command[1];
                boolean roleFound = false;
                for (Role r : message.getGuild().getMember(message.getAuthor()).getRoles()) {
                    if (r.getName().equalsIgnoreCase("Bet Organizer") || r.getName().equalsIgnoreCase("Discord Management")) {
                        roleFound = true;
                        break;
                    }
                }
                if (!roleFound) {
                    return;
                }
                OpenBet closed = null;
                for (OpenBet o : openBets) {
                    if (onePlayer.equalsIgnoreCase(o.getPlayer1()) || onePlayer.equalsIgnoreCase(o.getPlayer2())) {
                        openBets.remove(o);
                        closedBets.add(o);
                        closed = o;
                        break;
                    }
                }
                if (closed == null) {
                    return;
                }
                channel.sendMessage("Betting for " + closed.getPlayer1() + " vs. " + closed.getPlayer2() + " has closed! Watch the game and wait for the results!").queue();

            } else if (command[0].equalsIgnoreCase("!finishbet")) { //!finishbet winner
                if (event.isFromType(ChannelType.PRIVATE)) {
                    return;
                }

                String winner = command[1];
                boolean roleFound = false;
                for (Role r : message.getGuild().getMember(message.getAuthor()).getRoles()) {
                    if (r.getName().equalsIgnoreCase("Bet Organizer") || r.getName().equalsIgnoreCase("Discord Management")) {
                        roleFound = true;
                        break;
                    }
                }
                if (!roleFound) {
                    return;
                }
                OpenBet finalResults = null;
                for (OpenBet o : closedBets) {
                    if (winner.equalsIgnoreCase(o.getPlayer1()) || winner.equalsIgnoreCase(o.getPlayer2())) {
                        finalResults = o.close(winner);
                        closedBets.remove(o);
                        break;
                    }
                }
                if (finalResults == null) {
                    return;
                }
                EmbedBuilder betStats = new EmbedBuilder();
                if (winner.equalsIgnoreCase(finalResults.getPlayer1())) {
                    if (finalResults.getPlayer1Bets().size() == 0 && finalResults.getPlayer2Bets().size() == 0) {
                        betStats.setTitle(finalResults.getPlayer1() + " wins vs. " + finalResults.getPlayer2() + "!");
                        betStats.setDescription("Not bets were placed.");
                    } else {
                        betStats.setTitle(finalResults.getPlayer1() + " wins vs. " + finalResults.getPlayer2() + "!");
                        betStats.setDescription("Stats!");
                        betStats.setColor(Color.decode("#852eff"));
                        double p1Percentage = (double) finalResults.getP1Total() / (double) (finalResults.getP1Total() + finalResults.getP2Total());
                        p1Percentage = Math.round(p1Percentage * 100);
                        betStats.addField(finalResults.getPlayer1() + " (" + p1Percentage + "%): ", finalResults.getPlayer1Bets().size() + " bets, " + ((double) finalResults.getP1Total()) / 100 + "<:sharqcoin:413785618573819905> total.", false);
                        betStats.addField(finalResults.getPlayer2() + " (" + (100 - p1Percentage) + "%): ", finalResults.getPlayer2Bets().size() + " bets, " + ((double) finalResults.getP2Total()) / 100 + "<:sharqcoin:413785618573819905> total.", false);
                        if (!(finalResults.getPlayer1Bets().size() == 0)) {
                            StringBuilder topBets1 = new StringBuilder();
                            List<Bet> topP1List = finalResults.getPlayer1TopBets();
                            for (int i = 0; i < topP1List.size(); i++) {
                                topBets1.append(topP1List.get(i).getUser().getName() + " (" + ((double) topP1List.get(i).getAmount()) / 100 + "<:sharqcoin:413785618573819905>)");
                                if (i != topP1List.size() - 1) {
                                    topBets1.append(", ");
                                }
                            }
                            betStats.addField("Top bets for " + finalResults.getPlayer1() + ": ", topBets1.toString(), false);
                        }

                        if (!(finalResults.getPlayer2Bets().size() == 0)) {
                            StringBuilder topBets2 = new StringBuilder();
                            List<Bet> topP2List = finalResults.getPlayer2TopBets();
                            for (int i = 0; i < topP2List.size(); i++) {
                                topBets2.append(topP2List.get(i).getUser().getName() + " (" + ((double) topP2List.get(i).getAmount()) / 100 + "<:sharqcoin:413785618573819905>)");
                                if (i != topP2List.size() - 1) {
                                    topBets2.append(", ");
                                }
                            }
                            betStats.addField("Top bets for " + finalResults.getPlayer2() + ": ", topBets2.toString(), false);
                        }
                    }
                } else if (winner.equalsIgnoreCase(finalResults.getPlayer2())) {
                    if (finalResults.getPlayer2Bets().size() == 0 && finalResults.getPlayer1Bets().size() == 0) {
                        betStats.setTitle(finalResults.getPlayer2() + " wins vs. " + finalResults.getPlayer1() + "!");
                        betStats.setDescription("Not bets were placed.");
                    } else {
                        betStats.setTitle(finalResults.getPlayer2() + " wins vs. " + finalResults.getPlayer1() + "!");
                        betStats.setDescription("Stats!");
                        betStats.setColor(Color.decode("#852eff"));
                        double p2Percentage = (double) finalResults.getP2Total() / (double) (finalResults.getP2Total() + finalResults.getP1Total());
                        p2Percentage = Math.round(p2Percentage * 100);
                        betStats.addField(finalResults.getPlayer2() + " (" + p2Percentage + "%): ", finalResults.getPlayer2Bets().size() + " bets, " + ((double) finalResults.getP2Total()) / 100 + "<:sharqcoin:413785618573819905> total.", false);
                        betStats.addField(finalResults.getPlayer1() + " (" + (100 - p2Percentage) + "%): ", finalResults.getPlayer1Bets().size() + " bets, " + ((double) finalResults.getP1Total()) / 100 + "<:sharqcoin:413785618573819905> total.", false);
                        if (!(finalResults.getPlayer2Bets().size() == 0)) {
                            StringBuilder topBets2 = new StringBuilder();
                            List<Bet> topP2List = finalResults.getPlayer2TopBets();
                            for (int i = 0; i < topP2List.size(); i++) {
                                topBets2.append(topP2List.get(i).getUser().getName() + " (" + ((double) topP2List.get(i).getAmount()) / 100 + "<:sharqcoin:413785618573819905>)");
                                if (i != topP2List.size() - 1) {
                                    topBets2.append(", ");
                                }
                            }
                            betStats.addField("Top bets for " + finalResults.getPlayer2() + ": ", topBets2.toString(), false);
                        }
                        if (!(finalResults.getPlayer1Bets().size() == 0)) {
                            StringBuilder topBets1 = new StringBuilder();
                            List<Bet> topP1List = finalResults.getPlayer1TopBets();
                            for (int i = 0; i < topP1List.size(); i++) {
                                topBets1.append(topP1List.get(i).getUser().getName() + " (" + ((double) topP1List.get(i).getAmount()) / 100 + "<:sharqcoin:413785618573819905>)");
                                if (i != topP1List.size() - 1) {
                                    topBets1.append(", ");
                                }
                            }
                            betStats.addField("Top bets for " + finalResults.getPlayer1() + ": ", topBets1.toString(), false);
                        }
                    }
                }
                channel.sendMessage(betStats.build()).queue();
            } else if (command[0].equalsIgnoreCase("!openBets")) {
                EmbedBuilder openBetsList = new EmbedBuilder();
                openBetsList.setTitle("Current Open Bets");
                openBetsList.setDescription("PM me with `!bet amount playerName` to bet!");
                openBetsList.setColor(Color.decode("#6A018C"));
                for (OpenBet o : openBets) {
                    openBetsList.addField(o.getEventName(), o.getPlayer1() + " vs. " + o.getPlayer2(), false);
                }
                if (openBets.size() == 0) {
                    openBetsList.addField("", "No current open bets.", false);
                }
                channel.sendMessage(openBetsList.build()).queue();
            } else if (command[0].equalsIgnoreCase("!closedBets")) {
                EmbedBuilder closedBetsList = new EmbedBuilder();
                closedBetsList.setTitle("In progress games!");
                closedBetsList.setDescription("Watch and wait for the result!");
                closedBetsList.setColor(Color.decode("#6C0EF7"));
                for (OpenBet o : closedBets) {
                    closedBetsList.addField(o.getEventName(), o.getPlayer1() + " vs. " + o.getPlayer2(), false);
                }
                if (closedBets.size() == 0) {
                    closedBetsList.addField("", "No currently closed bets in progress.", false);
                }
                channel.sendMessage(closedBetsList.build()).queue();
            } else if (command[0].equalsIgnoreCase("!bet")) { //!bet amount playerName

                if (event.isFromType(ChannelType.TEXT)) {
                    return;
                }
                System.out.println(java.time.LocalDateTime.now().toString() + " " + message.getAuthor().getName() + ": " + content);

                int betAmount;
                if (command.length < 3) {
                    channel.sendMessage("Usage: `!bet amount user`").queue();
                    return;
                }
                if (command[1].equalsIgnoreCase("all")) {
                    betAmount = Integer.parseInt(JSONDude.getUser(message.getAuthor()).get("amount").toString());
                } else {
                    try {
                        if (Double.parseDouble(command[1]) < 1) {
                            channel.sendMessage("Please bet at least 1!").queue();
                            return;
                        }
                    } catch (NumberFormatException ignored) {
                        channel.sendMessage("Usage: `!bet amount user`").queue();
                        return;
                    }
                    betAmount = inputToInt(Double.parseDouble(command[1]));
                }
                String playerToBet = command[2];
                User bettingUser = message.getAuthor();
                //find bet
                boolean playerFound = false;
                for (OpenBet o : openBets) {
                    if (playerToBet.equalsIgnoreCase(o.getPlayer1())) {
                        playerFound = true;
                        for (Bet b : o.getPlayer1Bets()) {
                            if (message.getAuthor() == b.getUser()) {
                                channel.sendMessage("You have already bet on this game!").queue();
                                return;
                            }

                        }
                        for (Bet b : o.getPlayer2Bets()) {
                            if (message.getAuthor() == b.getUser()) {
                                channel.sendMessage("You have already bet on this game!").queue();
                                return;
                            }

                        }
                        if (addBet(channel, betAmount, bettingUser)) {
                            o.addToPlayer1Bets(new Bet(bettingUser, betAmount));
                            channel.sendMessage("Bet cast for " + playerToBet + "! Amount: " + ((double) betAmount) / 100).queue();
                        }
                        break;
                    }
                    if (playerToBet.equalsIgnoreCase(o.getPlayer2())) {
                        playerFound = true;
                        for (Bet b : o.getPlayer1Bets()) {
                            if (message.getAuthor() == b.getUser()) {
                                channel.sendMessage("You have already bet on this game!").queue();
                                return;
                            }
                        }
                        for (Bet b : o.getPlayer2Bets()) {
                            if (message.getAuthor() == b.getUser()) {
                                channel.sendMessage("You have already bet on this game!").queue();
                                return;
                            }
                        }
                        if (addBet(channel, betAmount, bettingUser)) {
                            o.addToPlayer2Bets(new Bet(bettingUser, betAmount));
                            channel.sendMessage("Bet cast for " + playerToBet + "! Amount: " + ((double) betAmount) / 100).queue();
                        }
                        break;
                    }
                }
                if (!playerFound) {
                    channel.sendMessage("Player \"" + playerToBet + "\" was not found in any active bets!").queue();
                }
            } else if (command[0].equalsIgnoreCase("!withdrawbet")) {
                System.out.println(message.getAuthor().getName() + ": " + content);

            } else if (command[0].equalsIgnoreCase("+promote")) {


                if (channel.getId().equalsIgnoreCase("435988653660176396")) { //GLOBAL
                    promote(message, channel.getId(), "GLOBAL");
                } else if (channel.getId().equalsIgnoreCase("435988103388332054")) { //NORTH AMERICA
                    promote(message, channel.getId(), "NA");
                } else if (channel.getId().equalsIgnoreCase("435988172556599296")) { //EUROPE
                    promote(message, channel.getId(), "EU");
                } else if (channel.getId().equalsIgnoreCase("450090865789108225")) { //AUSTRALIA
                    promote(message, channel.getId(), "AU");
                } else if (channel.getId().equalsIgnoreCase("435988193783971840")) { //SOUTHEAST ASIA
                    promote(message, channel.getId(), "SEA");
                } else {
                    channel.sendMessage("You cannot promote in this channel!").queue();
                }
            }
        }
    }

    private boolean addBet(MessageChannel channel, int betAmount, User bettingUser) {
        try {

            JSONObject bettingUserJSON = JSONDude.getUser(bettingUser);

            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader("./sharqcoin.json"));
            JSONArray users = (JSONArray) obj;

            if (betAmount > Integer.parseInt(bettingUserJSON.get("amount").toString())) {
                channel.sendMessage("Insufficient funds!").queue();
                return false;
            } else {
                users.remove(JSONDude.getUser(bettingUser));

                bettingUserJSON.put("amount", Integer.parseInt(bettingUserJSON.get("amount").toString()) - betAmount);

                users.add(bettingUserJSON);

                FileWriter jsonFile = new FileWriter("./sharqcoin.json");
                jsonFile.write(users.toJSONString());
                jsonFile.flush();
                jsonFile.close();

            }

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return true;

    }

    private int inputToInt(double input) {
        return (int) (Math.round(input * 100));
    }

    private String getPlayerReward(int playerReward, JSONObject playerJSON, boolean playerReceives, int mode) {
        String response = "";

        int streak = Integer.parseInt(playerJSON.get("streak").toString());
        if (playerReceives) {

            response += "Pickup rewards for " + playerJSON.get("Name").toString() + ": " + ((double) playerReward) / 100 + "<:sharqcoin:413785618573819905>";

            //checks streak
            if (streak == 0) {
                playerJSON.put("streak", 1);
//                response += "\n";
            } else if (LocalDateTime.now().getDayOfMonth() - (LocalDateTime.parse(playerJSON.get("lastPlayedPickup").toString())).getDayOfMonth() == 1) {
                if ((streak + 1) % 3 == 0) {
                    playerReward += THREE_DAY_STREAK_REWARD;
                    response += ". " + (streak + 1) + " day streak! \uD83D\uDD25 +" + ((double) THREE_DAY_STREAK_REWARD) / 100 + "<:sharqcoin:413785618573819905> \n";

                } else {
//                    response += "\n";
                }

                playerJSON.put("streak", Integer.parseInt(playerJSON.get("streak").toString()) + 1);

            } else if (LocalDateTime.now().getDayOfMonth() - (LocalDateTime.parse(playerJSON.get("lastPlayedPickup").toString())).getDayOfMonth() == 0) {
//                response += "\n";
            } else {
                playerJSON.put("streak", 0);
//                response += "\n";
            }


            //checks for sushi sunday mode bonus
//            if (LocalDateTime.now().getDayOfWeek() == DayOfWeek.SUNDAY) {
//                if (LocalDateTime.now().toLocalTime().isAfter(LocalTime.of(13, 0)) && LocalDateTime.now().toLocalTime().isBefore(LocalTime.of(18, 0))) {
//
//                    int weeksElapsed = 0;
//                    weeksElapsed = getWeeksElapsed();
//                    switch (weeksElapsed%4) {


            if (LocalDateTime.now().getDayOfWeek() == DayOfWeek.SUNDAY) {
                if (LocalDateTime.now().toLocalTime().isAfter(LocalTime.of(13, 0)) && LocalDateTime.now().toLocalTime().isBefore(LocalTime.of(18, 0))) {


                    int weeksElapsed = 0;
                    int[] bonusModeArray = {-1, -1, -1, -1};

                    weeksElapsed = getWeeksElapsed();
                    Random gen = new Random(weeksElapsed / 4);
                    for (int i = 0; i <= weeksElapsed % 4; i++) {
                        int nextMode = gen.nextInt(4);
                        while (nextMode == bonusModeArray[0] || nextMode == bonusModeArray[1] || nextMode == bonusModeArray[2])
                            nextMode = gen.nextInt(4);

                        bonusModeArray[i] = nextMode;
                    }
                    int randomMode = gen.nextInt(4);

                    switch (bonusModeArray[weeksElapsed % 4]) {
                        //doubles
                        case 0:
                            if (mode == 2) {
                                playerReward += MODE_BONUS;
                                response += ". Sushi Sundays doubles bonus! +" + ((double) MODE_BONUS) / 100 + "<:sharqcoin:413785618573819905> \n";
                            } else {
                                response += "\n";
                            }
                            break;

                        //ca
                        case 1:
                            if (mode == 3) {
                                playerReward += MODE_BONUS;
                                response += ". Sushi Sundays clan arena bonus! +" + ((double) MODE_BONUS) / 100 + "<:sharqcoin:413785618573819905> \n";
                            } else {
                                response += "\n";
                            }
                            break;

                        //tdm
                        case 2:
                            if (mode == 4) {
                                playerReward += MODE_BONUS;
                                response += ". Sushi Sundays tdm bonus! +" + ((double) MODE_BONUS) / 100 + "<:sharqcoin:413785618573819905> \n";
                            } else {
                                response += "\n";
                            }
                            break;

                        //ctf
                        case 3:
                            if (mode == 5) {
                                playerReward += MODE_BONUS;
                                response += ". Sushi Sundays ctf bonus! +" + ((double) MODE_BONUS) / 100 + "<:sharqcoin:413785618573819905> \n";
                            } else {
                                response += "\n";
                            }
                            break;

                    }
                } else {
                    response += "\n";
                }
            } else {
                response += "\n";
            }


            playerJSON.put("amount", Integer.parseInt(playerJSON.get("amount").toString()) + playerReward);
        }
        return response;

    }

    private void promote(Message message, String channelId, String roleName) {

        MessageChannel channel = message.getChannel();

        //checks if promoter has role he is promoting to
        boolean foundRole = false;
        for (Role r : message.getMember().getRoles()) {
            if (r.getName().equalsIgnoreCase(roleName)) {
                foundRole = true;
            }
        }
        if (!foundRole) {
            channel.sendMessage("You cannot promote in this channel without the " + roleName + " role!").queue();
            return;
        }

        JSONObject promotingUser = JSONDude.getUser(message.getAuthor());

        //checks if promoter has enough sharqcoin
        assert promotingUser != null;
        if (399 > Integer.parseInt(promotingUser.get("amount").toString())) {
            channel.sendMessage("Insufficient funds! (4<:sharqcoin:413785618573819905> required)").queue();
            return;
        }

        //checks if promoter has promoted in the last 10 min
        long minutesSinceLastPromote = ChronoUnit.MINUTES.between(LocalDateTime.parse(promotingUser.get("lastPromote").toString()), LocalDateTime.now());
        if (minutesSinceLastPromote < 10L) {
            String minutesLeft = "" + (10L - minutesSinceLastPromote);
            channel.sendMessage("Please wait " + minutesLeft + " minutes before promoting again!").queue();
            return;
        }




        //looks for roles and PMs
        Guild messageGuild = message.getGuild();
        for (Member m : messageGuild.getMembers()) {
            //checks if intended pm target is not original promoter
            if (m.getUser().getId().equalsIgnoreCase(message.getAuthor().getId())) {
                m.getUser().openPrivateChannel().queue((privateChannel -> privateChannel.sendMessage(((double)PROMOTE_COST)/100 + "<:sharqcoin:413785618573819905> deducted to promote!").queue()));
            } else {
                for (Role r : m.getRoles()) {
                    if (r.getName().equalsIgnoreCase(roleName)) {
                        m.getUser().openPrivateChannel().queue((privateChannel) -> privateChannel.sendMessage("Please add pickups in <#" + channelId + ">!").queue());
                    }
                }
            }
        }


        try {

            //deducts 4 sharqcoin
            JSONParser parser = new JSONParser();
            JSONArray users = (JSONArray) parser.parse(new FileReader("./sharqcoin.json"));

            users.remove(promotingUser);

            promotingUser.put("amount", Integer.parseInt(promotingUser.get("amount").toString()) - PROMOTE_COST);
            promotingUser.put("lastPromote", LocalDateTime.now().toString());


            channel.sendMessage("PMs sent to players with " + roleName + " role!").queue();


            users.add(promotingUser);
            FileWriter jsonFile = new FileWriter("./sharqcoin.json");
            jsonFile.write(users.toJSONString());
            jsonFile.flush();
            jsonFile.close();

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

}

//\uD83C\uDF63


