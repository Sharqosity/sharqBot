package sharqBot;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.requests.SessionReconnectQueue;
import sharqBot.Message.Listener;

import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Main {

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
        jdaBuilder.setReconnectQueue(new SessionReconnectQueue());
        jdaBuilder.setToken(token);
        JDA api = jdaBuilder.buildBlocking();

        jdaBuilder.addEventListener(new Listener(api));

        System.out.println("Start time: "+ java.time.LocalDateTime.now());

        jdaBuilder.useSharding(0, 2).buildBlocking(JDA.Status.AWAITING_LOGIN_CONFIRMATION);
        Thread.sleep(5000);
        jdaBuilder.useSharding(1, 2).buildBlocking(JDA.Status.AWAITING_LOGIN_CONFIRMATION);


    }
}
