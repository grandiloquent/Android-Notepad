package euphoria.psycho.notepad;

import android.content.Context;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.util.Log;

import java.io.*;


public class Utils {

    private static Context sContext;

    public static void setContext(Context context) {
        sContext = context;
    }

    private static final String TAG = "Utils";

    public static Context getContext() {
        return sContext;
    }

    public static String readFileAsBase64String(File file) {

        if (file.getName().endsWith(".jpg"))
            return "data:image/jpeg;base64," + readFileAsBase64String(file.getAbsolutePath());
        return "";
    }

    public static String readFileAsBase64String(String path) {
        try {
            InputStream is = new FileInputStream(path);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Base64OutputStream b64os = new Base64OutputStream(baos, Base64.DEFAULT);
            byte[] buffer = new byte[8192];
            int bytesRead;
            try {
                while ((bytesRead = is.read(buffer)) > -1) {
                    b64os.write(buffer, 0, bytesRead);
                }
                return baos.toString();
            } catch (IOException e) {
                Log.e(TAG, "Cannot read file " + path, e);
                // Or throw if you prefer
                return "";
            } finally {
                closeQuietly(is);
                closeQuietly(b64os); // This also closes baos
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found " + path, e);
            // Or throw if you prefer
            return "";
        }
    }

    public static void closeQuietly(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
        }
    }
}
