package euphoria.psycho.notepad;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.input.InputManager;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.Random;

public class C {
    public static boolean isNullOrWhiteSpace(CharSequence text) {
        if (text == null || text.length() == 0) return true;
        for (int i = 0, j = text.length(); i < j; i++) {
            if (!Character.isWhitespace(text.charAt(i))) return false;
        }
        return true;
    }

    public static String trimEnd(String s) {
        int len = s.length();
        if (len == 0) return s;

        while (s.charAt(len - 1) <= ' ') {
            len--;
        }
        return len < s.length() ? s.substring(0, len) : s;
    }

    public static String randomString() {
        StringBuilder sb = new StringBuilder();
        int max = 10;
        Random random = new Random();
        while (max-- > 0) {
            for (int i = 0; i < random.nextInt(20); i++) {
                sb.append(i);
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    public static void showSoftInput(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        /*
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
         */
    }

}
