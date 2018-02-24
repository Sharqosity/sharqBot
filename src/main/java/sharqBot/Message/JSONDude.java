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

public class JSONDude {

    public static JSONObject getUser(User targetUser) {
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
