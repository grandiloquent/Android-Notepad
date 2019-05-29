package euphoria.psycho.notepad;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
public class YoudaoUtils {
    public static String generateRequestUrl(String query) {
//        final Pattern pattern = Pattern.compile("[\u4E00-\u9FA5]");
//        Matcher matcher = pattern.matcher(query);
        String appKey = "1f5687b5a6b94361";
        String salt = String.valueOf(System.currentTimeMillis());
        String from = "";
        from = "EN";
        String to = "";
        to = "zh-CHS";
        String sign = md5(appKey + query + salt + "2433z6GPFslGhUuQltdWP7CPlbk8NZC0");
        Map<String, String> params = new HashMap();
        params.put("q", query);
        params.put("from", from);
        params.put("to", to);
        params.put("sign", sign);
        params.put("salt", salt);
        params.put("appKey", appKey);
        String url = getUrlWithQueryString("http://openapi.youdao.com/api", params);
        return url;
    }

    private static String encode(String input) {
        if (input == null) {
            return "";
        }
        try {
            return URLEncoder.encode(input, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return input;
    }
    private static String getUrlWithQueryString(String url, Map<String, String> params) {
        if (params == null) {
            return url;
        }
        StringBuilder builder = new StringBuilder(url);
        if (url.contains("?")) {
            builder.append("&");
        } else {
            builder.append("?");
        }
        int i = 0;
        for (String key : params.keySet()) {
            String value = params.get(key);
            if (value == null) { // 过滤空的key
                continue;
            }
            if (i != 0) {
                builder.append('&');
            }
            builder.append(key);
            builder.append('=');
            builder.append(encode(value));
            i++;
        }
        return builder.toString();
    }
    private static String md5(String string) {
        if (string == null) {
            return null;
        }
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F'};
        try {
            byte[] btInput = string.getBytes("utf-8");
            /** 获得MD5摘要算法的 MessageDigest 对象 */
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            /** 使用指定的字节更新摘要 */
            mdInst.update(btInput);
            /** 获得密文 */
            byte[] md = mdInst.digest();
            /** 把密文转换成十六进制的字符串形式 */
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (byte byte0 : md) {
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            return null;
        }
    }
    public static String extractJSON(JSONObject js, String word) {
        StringBuilder builder = new StringBuilder();
        if (js.has("basic")) {
            try {
                JSONObject basic = js.getJSONObject("basic");
                if (basic.has("us-phonetic")) {
                    builder.append('/').append(basic.getString("us-phonetic")).append('/');
                } else if (basic.has("phonetic")) {
                    builder.append('/').append(basic.getString("phonetic")).append('/');
                }
                if (basic.has("explains")) {
                    JSONArray explains = basic.getJSONArray("explains");
                    for (int i = 0; i < explains.length(); i++) {
                        builder.append(explains.getString(i)).append(';');
                    }
                    builder.append('\n');
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (js.has("translation")) {
            try {
                JSONArray translation = js.getJSONArray("translation");
                if (translation.length() == 1 && translation.getString(0).equalsIgnoreCase(word)) {
                } else {
                    for (int i = 0; i < translation.length(); i++) {
                        builder.append(translation.getString(i)).append(';');
                    }
                    builder.append('\n');
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (js.has("web")) {
            try {
                JSONArray web = js.getJSONArray("web");
                for (int i = 0; i < web.length(); i++) {
                    JSONObject j = web.getJSONObject(i);
                    builder.append(j.getString("key").toLowerCase()).append(":");
                    JSONArray ja = j.getJSONArray("value");
                    for (int k = 0; k < ja.length(); k++) {
                        builder.append(ja.getString(k)).append(';');
                    }
                    builder.append('\n');
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return builder.toString();
    }
}