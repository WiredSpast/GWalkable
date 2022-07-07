package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public class ItemDirection {
    private static Map<String, ItemDirection[]> directions = new HashMap();
    public final int x;
    public final int y;
    public final int z;
    public final int angle;
    public final int id;

    protected ItemDirection(JSONObject object) {
        this.x = object.getInt("x");
        this.y = object.getInt("y");
        this.z = object.getInt("z");
        this.angle = object.getInt("angle");
        this.id = object.getInt("id");
    }

    public static ItemDirection[] getDirectionsByClassName(String className) {
        return directions.get(className.split("\\*")[0]);
    }

    private static JSONObject readJsonFromUrl(String url) throws Exception {
        InputStream is = (new URL(url)).openStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        String jsonText = readAll(rd);
        return new JSONObject(jsonText);
    }

    private static String readAll(BufferedReader rd) throws IOException {
        StringBuilder sb = new StringBuilder();

        int cp;
        while((cp = rd.read()) != -1) {
            sb.append((char)cp);
        }

        return sb.toString();
    }

    static {
        try {
            JSONObject directionsJSON = readJsonFromUrl("https://raw.githubusercontent.com/WiredSpast/FurniDirections/main/directions.json");
            directionsJSON.getJSONArray("itemDirections").toList().stream()
                    .map((item) -> (HashMap)item).map(JSONObject::new)
                    .forEach((item) -> {
                        String className = item.getString("classname");
                        JSONArray directionsArray = item.getJSONArray("directions");
                        ItemDirection[] itemDirections = new ItemDirection[directionsArray.length()];

                        for(int i = 0; i < directionsArray.length(); ++i) {
                            itemDirections[i] = new ItemDirection(directionsArray.getJSONObject(i));
                        }

                        directions.put(className, itemDirections);
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}