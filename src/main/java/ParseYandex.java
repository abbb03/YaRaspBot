import org.json.JSONArray;
import org.json.JSONObject;

public class ParseYandex {
    public static String json;

    public static String parse() {
        StringBuilder stringBuilder = new StringBuilder();
        JSONObject obj = new JSONObject(json);

        String from = obj.getJSONObject("search").getJSONObject("from").getString("title");
        String to = obj.getJSONObject("search").getJSONObject("to").getString("title");
        String direction = "Из " + from + " в " + to + "\n";

        stringBuilder.append(direction);

        JSONArray arr = obj.getJSONArray("segments");
        for (int i = 0; i < arr.length(); i++) {
            String departure = arr.getJSONObject(i).getString("departure").substring(0, 5);
            String arrival = arr.getJSONObject(i).getString("arrival").substring(0, 5);
            String route = departure + " - " + arrival + "\n";
            stringBuilder.append(route);
        }
        return stringBuilder.toString();
    }
}