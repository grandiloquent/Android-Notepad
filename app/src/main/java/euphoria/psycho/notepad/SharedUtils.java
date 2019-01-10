package euphoria.psycho.notepad;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Environment;

import java.io.*;
import java.nio.charset.Charset;

public class SharedUtils {

    private static Context sContext;


    public static File getExternalStorageDirectoryFile(String fileName) {
        return new File(Environment.getExternalStorageDirectory(), fileName);
    }

    public static Context getContext() {
        return sContext;
    }

    public static void setContext(Context context) {
        sContext = context;
    }

    public static int requestPermissions(Activity activity) {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int r = 99;
            activity.requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, r);
            return r;
        }
        return 0;

    }

    public static void writeStringToFile(File dst, String content) {
        FileOutputStream os = null;

        try {

            os = new FileOutputStream(dst);

            byte[] buffer = content.getBytes(Charset.forName("utf-8"));
            os.write(buffer, 0, buffer.length);
            os.close();

        } catch (Exception e) {

        }
    }

    public static String readStringFromFile(File src) {

        FileInputStream in = null;
        try {
            in = new FileInputStream(src);
            InputStreamReader reader = new InputStreamReader(in, Charset.forName("utf-8"));

            BufferedReader bufferedReader = new BufferedReader(reader);

            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append('\n');
            }
            bufferedReader.close();
            in.close();
            return stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


}
