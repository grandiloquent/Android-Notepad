package euphoria.psycho.notepad;

import android.text.TextUtils;

public class Fomatter {

    public static String formatCode(String s) {
        if (TextUtils.isEmpty(s)) return "``";

        if (s.contains("\n")) {
            return String.format("\n```\n%s\n```\n", s.trim());
        } else {
            return " `" + s.trim() + "` ";
        }
    }

    public static String formatHead(String s) {
        if (s.trim().length() == 0)
            return "### ";
        return "### " + s.trim() + "\n";
    }


}
