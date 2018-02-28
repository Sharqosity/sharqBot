package sharqBot;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.requests.SessionReconnectQueue;
import sharqBot.Message.JSONDude;
import sharqBot.Message.Listener;
import sharqBot.Message.SharqCoinListener;

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
        try {
            in = new FileReader("./token/dropbox.txt");
        } catch (FileNotFoundException e) {
            System.out.println("Dropbox token not found!");
            e.printStackTrace();
            System.exit(0);
        }
        br = new BufferedReader(in);
        String dropBoxToken = null;
        try {
            dropBoxToken = br.readLine();
        } catch (IOException e) {
            System.out.println("dropbox.txt read error!");
            e.printStackTrace();
            System.exit(0);
        }



        JSONDude.dropBoxToken = dropBoxToken;

//        try {
//            JSONDude.getFile("/ReflexServerBot/sharqcoin.json");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        // Create Dropbox client
        DbxRequestConfig config = new DbxRequestConfig("dropbox/java-tutorial", "en_US");
        DbxClientV2 client = new DbxClientV2(config, dropBoxToken);

        // Get current account info
        FullAccount account = null;
        try {
            account = client.users().getCurrentAccount();
        } catch (DbxException e) {
            e.printStackTrace();
        }
        System.out.println(account.getName().getDisplayName());

        // Get files and folder metadata from Dropbox root directory
        ListFolderResult result = null;
        try {
            result = client.files().listFolder("");
        } catch (DbxException e) {
            e.printStackTrace();
        }
        while (true) {
            for (Metadata metadata : result.getEntries()) {
                System.out.println(metadata.getPathLower());
            }

            if (!result.getHasMore()) {
                break;
            }

            try {
                result = client.files().listFolderContinue(result.getCursor());
            } catch (DbxException e) {
                e.printStackTrace();
            }
        }

        JDABuilder jdaBuilder = new JDABuilder(AccountType.BOT);
        jdaBuilder.setReconnectQueue(new SessionReconnectQueue());

        jdaBuilder.setToken(token);
        jdaBuilder.addEventListener(new Listener());
        jdaBuilder.addEventListener(new SharqCoinListener());
//        jdaBuilder.addEventListener(new PickupListener());

        System.out.println("Start time: "+ java.time.LocalDateTime.now());

//        System.out.println(java.time.LocalDateTime.of(2018,2,23,10,18));
        jdaBuilder.useSharding(0, 2).buildBlocking(JDA.Status.AWAITING_LOGIN_CONFIRMATION);
        Thread.sleep(5000);
        jdaBuilder.useSharding(1, 2).buildBlocking(JDA.Status.AWAITING_LOGIN_CONFIRMATION);



//        JDA api = jdaBuilder.buildBlocking();
    }
}
