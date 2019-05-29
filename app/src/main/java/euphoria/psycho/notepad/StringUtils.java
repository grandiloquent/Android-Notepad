package euphoria.psycho.notepad;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.Editable;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class StringUtils {

    public static void orderEditText(EditText editText) {
        if (editText == null) return;
        int length = editText.getText().length();
        if (length == 0) return;
        Editable editable = editText.getText();

        int start = editText.getSelectionStart();
        if (start == length) {
            start--;
        }
        while (start > 0) {
            char ch = editable.charAt(start);
            if (ch == '\n' && editable.charAt(start - 1) == '\n') {
                start += 1;
                break;
            }
            start--;
        }

        int end = editText.getSelectionEnd();
        while (end < length) {
            char ch = editable.charAt(end);
            if (ch == '\n' && end + 1 < length && editable.charAt(end + 1) == '\n') {
                break;
            }
            end++;
        }
        String text = editable.subSequence(start, end).toString();

        String[] lines = text.split("\n");

        List<String> arrayList = new ArrayList<>();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (arrayList.indexOf(line) == -1) {
                arrayList.add(line);
            }
        }
        Collator collator = Collator.getInstance(Locale.CHINA);
        Collections.sort(arrayList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return collator.compare(o1, o2);
            }
        });

        StringBuilder stringBuilder = new StringBuilder();
        for (String line : arrayList) {
            stringBuilder.append(line).append('\n');
        }

        editable.replace(start, end, stringBuilder.toString());
    }

    public static List<String> readAllLines(String fileName) throws IOException {
        FileInputStream in = new FileInputStream(fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "utf-8"));
        String line;
        List<String> lines = new ArrayList<>();


        while ((line = reader.readLine()) != null)
            lines.add(line);

        reader.close();

        return lines;
    }

    public static void transformTraditionalChinese(Context context) {
        ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager == null) return;

        ClipData clipData = manager.getPrimaryClip();
        if (clipData == null || clipData.getItemCount() == 0) return;
        if (clipData.getItemAt(0).getText() == null) return;
        String content = clipData.getItemAt(0).getText().toString();

        int rows = (content.length() + 9) / 10;
        if (rows < 3) rows = 3;
        char[][] chars = new char[rows][10];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < 10; j++) {
                chars[i][j] = '。';
            }
        }
        int j = 9;
        for (int i = 0; i < content.length(); i++) {
            if (i != 0 && i % rows == 0) {
                j--;
            }
            chars[i % rows][j] = content.charAt(i);
        }
        StringBuilder stringBuilder = new StringBuilder();
        int start = 0;
        for (int i = 0; i < 10; i++) {
            if (chars[0][i] == '。') start++;
            else break;
        }

        for (int i = 0; i < rows; i++) {

            for (int x = start; x < 10; x++) {

                stringBuilder.append(chars[i][x]);
            }
            stringBuilder.append('\n');
        }
        manager.setPrimaryClip(ClipData.newPlainText(null, stringBuilder.toString().replaceAll("。", "    ")));

    }

    public static void transformTraditionalChineseSpace(Context context) {
        ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager == null) return;

        ClipData clipData = manager.getPrimaryClip();
        if (clipData == null || clipData.getItemCount() == 0) return;
        if (clipData.getItemAt(0).getText() == null) return;
        String content = clipData.getItemAt(0).getText().toString();

        int rows = (content.length() + 9) / 10;
        if (rows < 3) rows = 3;
        char[][] chars = new char[rows][20];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < 20; j++) {
                chars[i][j] = '。';
            }
        }
        int j = 19;
        for (int i = 0; i < content.length(); i++) {
            if (i != 0 && i % rows == 0) {
                j -= 2;
            }
            chars[i % rows][j] = content.charAt(i);
        }
        StringBuilder stringBuilder = new StringBuilder();
        int start = 0;
        for (int i = 0; i < 20; i++) {
            if (chars[0][i] == '。') start++;
            else break;
        }

        for (int i = 0; i < rows; i++) {

            for (int x = start; x < 20; x++) {

                stringBuilder.append(chars[i][x]);
            }
            stringBuilder.append('\n');
        }
        manager.setPrimaryClip(ClipData.newPlainText(null, stringBuilder.toString().replaceAll("。", "    ")));

    }

    public static void writeAllText(String fileName, String content) throws IOException {
        FileOutputStream out = new FileOutputStream(fileName);

        byte[] buffer = content.getBytes("utf-8");
        out.write(buffer);

        out.close();


    }


    public static String replaceAll(String text, String[][] pairs) {
        if(pairs==null){
            return  text;
        }
        for (int i = 0; i < pairs.length; i++) {
            text = text.replaceAll(pairs[i][0], pairs[i][1]);
        }
        return text;
    }
}
/*
package euphoria.psycho.notepad;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

public class StringUtils {
    public static void transformTraditionalChinese(Context context) {
        ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager == null) return;

        ClipData clipData = manager.getPrimaryClip();
        if (clipData == null || clipData.getItemCount() == 0) return;
        if (clipData.getItemAt(0).getText() == null) return;
        String content = clipData.getItemAt(0).getText().toString();

        int rows = (content.length() + 9) / 10;
        if (rows < 3) rows = 3;
        char[][] chars = new char[rows][20];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < 20; j++) {
                chars[i][j] = '。';
            }
        }
        int j = 19;
        for (int i = 0; i < content.length(); i++) {
            if (i != 0 && i % rows == 0) {
                j -= 2;
            }
            chars[i % rows][j] = content.charAt(i);
        }
        StringBuilder stringBuilder = new StringBuilder();
        int start = 0;
        for (int i = 0; i < 20; i++) {
            if (chars[0][i] == '。') start++;
            else break;
        }

        for (int i = 0; i < rows; i++) {

            for (int x = start; x < 20; x++) {

                stringBuilder.append(chars[i][x]);
            }
            stringBuilder.append('\n');
        }
        manager.setPrimaryClip(ClipData.newPlainText(null, stringBuilder.toString().replaceAll("。", "    ")));

    }
}

 */