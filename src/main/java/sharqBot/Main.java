package sharqBot;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import sharqBot.Message.Listener;
import sharqBot.Music.PlayerControl;
import sharqBot.Pickup.PickupListener;

import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class Main {
    private static boolean muntTTSIsOn = false;

    public static boolean isMuntTTSIsOn() {
        return muntTTSIsOn;
    }

    public static void setMuntTTSIsOn(boolean b) {
        muntTTSIsOn = b;
    }

    public static void main(String[] args) throws LoginException, InterruptedException, RateLimitedException {
        FileReader in = null;

        try {
            in = new FileReader("./token/token.txt");
        } catch (FileNotFoundException e) {
            System.out.println("Token not found!");
            e.printStackTrace();
            System.exit(0);
        }
        BufferedReader br = new BufferedReader(in);
        String token = null;
        try {
            token = br.readLine();
        } catch (IOException e) {
            System.out.println("token.txt read error!");
            e.printStackTrace();
            System.exit(0);
        }

        JDABuilder jdaBuilder = new JDABuilder(AccountType.BOT);
        jdaBuilder.setToken(token);
        jdaBuilder.addEventListener(new Listener());
        jdaBuilder.addEventListener(new PlayerControl());
        jdaBuilder.addEventListener(new PickupListener());
        JDA api = jdaBuilder.buildBlocking();
    }
}
