package euphoria.psycho.notepad;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


public class TranslateUtils {

    public static String chineseBaidu(String query) {
        TransApi transApi = new TransApi("20190312000276185", "sdK6QhtFE64Qm0ID_SjG");
        String result = transApi.getTransResult(query, "en", "zh");

        try {
            if (result == null) return null;
            JSONObject object = new JSONObject(result);
            JSONArray array = object.getJSONArray("trans_result");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < array.length(); i++) {
                sb.append(array.getJSONObject(i).getString("dst"));
            }
            return sb.toString();
        } catch (JSONException e) {

            e.printStackTrace();
            return null;
        }

    }

    public static String englishBaidu(String query) {
        TransApi transApi = new TransApi("20190312000276185", "sdK6QhtFE64Qm0ID_SjG");
        String result = transApi.getTransResult(query, "zh", "en");

        try {
            if (result == null) return null;
            JSONObject object = new JSONObject(result);
            JSONArray array = object.getJSONArray("trans_result");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < array.length(); i++) {
                sb.append(array.getJSONObject(i).getString("dst"));
            }
            return sb.toString();
        } catch (JSONException e) {

            e.printStackTrace();
            return null;
        }

    }

    public static String chineseGoogle(String query) {
//        Log.e("__TAG___", "chineseGoogle");

        String address = "https://translate.google.cn/translate_a/single?client=gtx&sl=auto&tl="
                + "zh" + "&dt=t&dt=bd&ie=UTF-8&oe=UTF-8&dj=1&source=icon&q=" + Uri.encode(query);

        try {
            URL url = new URL(address);

            HttpURLConnection urlcon = (HttpURLConnection) url.openConnection();
            urlcon.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 5.1; rv:2.0) Gecko/20100101 Firefox/4.0");

            urlcon.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    urlcon.getInputStream(), "utf-8"));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            urlcon.disconnect();

            JSONObject jsonObject = new JSONObject(sb.toString());
            JSONArray jsonArray = jsonObject.getJSONArray("sentences");

            sb.setLength(0);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                sb.append(object.getString("trans"));
            }
            String result = sb.toString();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("__TAG___", e.getMessage());

        }
        return null;
    }

    public static String englishGoogle(String query) {
//        Log.e("__TAG___", "chineseGoogle");

        String address = "https://translate.google.cn/translate_a/single?client=gtx&sl=auto&tl="
                + "en" + "&dt=t&dt=bd&ie=UTF-8&oe=UTF-8&dj=1&source=icon&q=" + Uri.encode(query);

        try {
            URL url = new URL(address);

            HttpURLConnection urlcon = (HttpURLConnection) url.openConnection();
            urlcon.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 5.1; rv:2.0) Gecko/20100101 Firefox/4.0");

            urlcon.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    urlcon.getInputStream(), "utf-8"));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            urlcon.disconnect();

            JSONObject jsonObject = new JSONObject(sb.toString());
            JSONArray jsonArray = jsonObject.getJSONArray("sentences");

            sb.setLength(0);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                sb.append(object.getString("trans"));
            }
            String result = sb.toString();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("__TAG___", e.getMessage());

        }
        return null;
    }
}
