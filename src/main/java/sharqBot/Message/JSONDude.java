package sharqBot.Message;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import net.dv8tion.jda.core.entities.User;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

public class JSONDude {

    public static String dropBoxToken;



    public static JSONObject getUser(User targetUser) {
        // Create Dropbox client
        DbxRequestConfig config = new DbxRequestConfig("reflex-server-bot", "en_US");
        DbxClientV2 client = new DbxClientV2(config, dropBoxToken);

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



            try (InputStream in = new FileInputStream("./sharqcoin.json")) {
                FileMetadata metadata = client.files().uploadBuilder("./ReflexServerBot/sharqcoin.json")
                        .uploadAndFinish(in);
            } catch (DbxException e) {
                e.printStackTrace();
            }

            return newUser;
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void getFile(String foldername) {

        try {

            URL url = new URL("https://content.dropboxapi.com/2/files/download");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            String parameters = "{\"path\": \"" + foldername + "\"}";

            conn.addRequestProperty ("Authorization", dropBoxToken);
            conn.addRequestProperty ("Dropbox-API-Arg", parameters);
            conn.setDoOutput(true);

            if (conn.getResponseCode() != 200) {
                System.out.println(conn.getResponseMessage());
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                System.out.println(output);
            }

            conn.disconnect();

        } catch (IOException e) {

            e.printStackTrace();

        }

    }

    public static void putFile(String foldername, String path) {

        try {

            URL url = new URL("https://content.dropboxapi.com/2/files/upload");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            String parameters = "{\"path\": \"" + foldername + "\"}";

            conn.setRequestProperty("Content-Type", "application/octet-stream");
            conn.addRequestProperty ("Authorization", dropBoxToken);
            conn.addRequestProperty ("Dropbox-API-Arg", parameters);
            conn.setRequestMethod("POST");


            conn.setDoOutput(true);

            Path pathFile = Paths.get(path);
            byte[] data = Files.readAllBytes(pathFile);

            DataOutputStream writer = new DataOutputStream(conn.getOutputStream());
            /*
            writer.writeBytes(parameters);
            writer.flush();
            */
            writer.write(data);
            writer.flush();

            if (writer != null)
                writer.close();

            if (conn.getResponseCode() != 200) {
                System.out.println(conn.getResponseMessage());
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                System.out.println(output);
            }

            conn.disconnect();

        } catch (MalformedURLException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        }

    }
}
