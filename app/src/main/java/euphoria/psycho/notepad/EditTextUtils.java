package euphoria.psycho.notepad;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.Editable;
import android.util.Log;
import android.widget.EditText;

public class EditTextUtils {

    private static ClipboardManager sClipboardManager;

    public static void insertBefore(EditText editText, String text) {

        int start = editText.getSelectionStart();
        editText.getText().insert(start, text);
        editText.setSelection(start);
    }

    public static void cutLine(EditText editText) {
        String text = editText.getText().toString();
        int len = text.length();

        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        if (start == len) {
            start--;
        }
        if (start == end && text.charAt(start) == '\n') {
            start--;

            while (start > 0 && text.charAt(start) != '\n') {
                start--;
            }
            if (text.charAt(start) == '\n') {
                start++;
            }

        } else {


            while (start > 0 && text.charAt(start) != '\n') {
                start--;
            }
            if (text.charAt(start) == '\n') {
                start++;
            }

            while (end < len && text.charAt(end) != '\n') {
                end++;
            }

        }
        String str = text.substring(start, end);
        if (sClipboardManager == null) {
            sClipboardManager = (ClipboardManager) editText.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        }
        sClipboardManager.setPrimaryClip(ClipData.newPlainText(null, str));
        editText.getText().delete(start, end);
    }

    public static void showActionMode(EditText editText) {
        String text = editText.getText().toString();
        int len = text.length();

        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        if (start == len) {
            start--;
        }
        if (start == end && text.charAt(start) == '\n' && !isChinese(text.charAt(start))) {
            start--;

            while (start > 0 && text.charAt(start) != '\n' && !isChinese(text.charAt(start))) {
                start--;
            }
            if (text.charAt(start) == '\n' || isChinese(text.charAt(start))) {
                start++;
            }

        } else {


            while (start > 0 && text.charAt(start) != '\n' && !isChinese(text.charAt(start))) {
                start--;
            }
            if (text.charAt(start) == '\n'||isChinese(text.charAt(start))) {
                start++;
            }

            while (end < len && text.charAt(end) != '\n' && !isChinese(text.charAt(end))) {
                end++;
            }

        }
        editText.setSelection(start, end);

        editText.showContextMenu();

    }

    public static boolean isChinese(char c) {

        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);

        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS

                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS

                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A

                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION

                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION

                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {

            return true;

        }

        return false;

    }

    public static void deleteLine(EditText editText) {
        String text = editText.getText().toString();
        int len = text.length();

        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        if (start == len) {
            start--;
        }
        if (start == end && text.charAt(start) == '\n') {
            start--;

            while (start > 0 && text.charAt(start) != '\n') {
                start--;
            }
            if (text.charAt(start) == '\n') {
                start++;
            }

        } else {


            while (start > 0 && text.charAt(start) != '\n') {
                start--;
            }
            if (text.charAt(start) == '\n') {
                start++;
            }

            while (end < len && text.charAt(end) != '\n') {
                end++;
            }


        }
        editText.getText().delete(start, end);
    }

    public static String selectLine(EditText editText) {

        String text = editText.getText().toString();
        int len = text.length();

        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        if (start == len) {
            start--;
        }
        if (start == end && text.charAt(start) == '\n') {
            start--;

            while (start > 0 && text.charAt(start) != '\n') {
                start--;
            }
            if (text.charAt(start) == '\n') {
                start++;
            }

        } else {


            while (start > 0 && text.charAt(start) != '\n') {
                start--;
            }
            if (text.charAt(start) == '\n') {
                start++;
            }

            while (end < len && text.charAt(end) != '\n') {
                end++;
            }


        }
        editText.setSelection(start, end);

        // String str=text.substring(start, end);

        return text.substring(start, end);
    }
}
