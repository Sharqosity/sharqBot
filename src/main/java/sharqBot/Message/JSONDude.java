package sharqBot.Message;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;

public class JSONDude {

    public static JSONArray getServerLists() {
        JSONParser parser = new JSONParser();
        JSONArray lists = new JSONArray();
        try {
            Object obj = parser.parse(new FileReader("./lists.json"));
            lists = (JSONArray) obj;

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return lists;
    }

}
