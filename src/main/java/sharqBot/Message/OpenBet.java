package sharqBot.Message;

import net.dv8tion.jda.core.entities.User;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class OpenBet {

    private ArrayList<Bet> player1Bets = new ArrayList<Bet>();
    private ArrayList<Bet> player2Bets = new ArrayList<Bet>();

    private int p1Total = 0;
    private int p2Total = 0;


    private String player1;
    private String player2;
    private String eventName;



    private List<Bet> player1TopBets, player2TopBets;

    public OpenBet(String player1, String player2, String eventName) {
        this.player1 = player1;
        this.player2 = player2;
        this.eventName = eventName;
    }

    public OpenBet close(String winner) {
        calcTotals();

        player1Bets.sort(Comparator.comparingInt(Bet::getAmount).reversed());
        player2Bets.sort(Comparator.comparingInt(Bet::getAmount).reversed());

        if(player1Bets.size() == 0) {
            player1TopBets = new ArrayList<>();
        } else if (player1Bets.size() < 3) {
            player1TopBets = player1Bets.subList(0, player1Bets.size());


        } else {
            player1TopBets = player1Bets.subList(0, 2);

        }

        if(player2Bets.size() == 0) {
            player2TopBets = new ArrayList<>();
        } else if (player2Bets.size() < 3) {
            player2TopBets = player2Bets.subList(0, player2Bets.size());

        } else {
            player2TopBets = player2Bets.subList(0, 2);

        }

        if (winner.equalsIgnoreCase(player1)) {
            payWinners(player1Bets,p2Total,p1Total,player1,player2);
            return this;
        } else if (winner.equalsIgnoreCase(player2)) {
            payWinners(player2Bets,p1Total,p2Total,player2,player1);
            return this;
        }
        return null;
    }

    //do not call this twice LOL
    private void calcTotals() {

        for (Bet b : player1Bets) {
            p1Total += b.getAmount();
        }
        for (Bet b : player2Bets) {
            p2Total += b.getAmount();
        }
    }

    //mega bad code
    private void payWinners(ArrayList<Bet> winnerList, int loserTotal, int winnerTotal, String winner, String loser) {
        double ratio;
        if(winnerTotal == 0) {
            ratio = 1;
            return;
        } else {
            ratio = 1 + ((((double) loserTotal) / ((double) winnerTotal)));
        }

        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader("./sharqcoin.json"));
            JSONArray users = (JSONArray) obj;
            for (Bet b : winnerList) {
                JSONObject better = getUser(b.getUser());
                users.remove(getUser(b.getUser()));
                better.put("amount", Integer.parseInt(better.get("amount").toString()) + Math.round(b.getAmount()*ratio *100) / 100);
                users.add(better);
                b.getUser().openPrivateChannel().queue(privateChannel -> {
                    privateChannel.sendMessage(winner + " wins vs. " + loser + "! \nYou win: " +((double)(/*Integer.parseInt(better.get("amount").toString()) +*/ Math.round(b.getAmount()*ratio)))/100 +"<:sharqcoin:413785618573819905>").queue();
                });

                FileWriter jsonFile = new FileWriter("./sharqcoin.json");
                jsonFile.write(users.toJSONString());
                jsonFile.flush();
                jsonFile.close();
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
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


    public ArrayList<Bet> getPlayer1Bets() {
        return player1Bets;
    }

    public ArrayList<Bet> getPlayer2Bets() {
        return player2Bets;
    }

    public void addToPlayer1Bets(Bet bet) {
        player1Bets.add(bet);
    }

    public void addToPlayer2Bets(Bet bet) {
        player2Bets.add(bet);
    }

    public String getPlayer1() {
        return player1;
    }

    public String getPlayer2() {
        return player2;
    }


    public String getEventName() {
        return eventName;
    }


    public int getP1Total() {
        return p1Total;
    }

    public int getP2Total() {
        return p2Total;
    }

    public List<Bet> getPlayer1TopBets() {
        return player1TopBets;
    }

    public List<Bet> getPlayer2TopBets() {
        return player2TopBets;
    }

}
