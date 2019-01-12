package euphoria.psycho.notepad;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import org.javia.arity.Symbols;
import org.javia.arity.SyntaxException;
import org.javia.arity.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static euphoria.psycho.notepad.Constants.EXTRA_ID;


public class EditNoteActivity extends Activity {

    private static final int MENU_CALCULATE = 0x3;
    private static final int MENU_FORMAT = 0x2;
    private static final int MENU_S_ASTERISK = 0x11;
    private static final int MENU_S_CODE = 0x5;
    private static final int MENU_S_DIVIDE = 0x10;
    private static final int MENU_S_HEAD = 0x6;
    private static final int MENU_S_PARENTHESIS = 0x7;
    private static final int MENU_S_SORT_BY = 0x7;
    private static final int MENU_S_SORT_BY_YEAR = 0x8;
    private static final int MENU_S_SUBTRACTION = 0x8;
    private EditText mEditText;

    private boolean mFinished = false;
    private Note mNote;
    private Symbols mSymbols;
    private boolean mUpdated = false;

    private void actionSort() {
        String s = getSelectedText(mEditText);
        if (s != null) {
            s = sort(s, new Comparator<String>() {

                @Override
                public int compare(String o1, String o2) {

                    return o1.compareTo(o2);
                }
            });
            replaceSelectedText(mEditText, s);

        }
    }

    private void actionSortByYear() {
        String s = getSelectedText(mEditText);
        if (s != null) {
            s = sort(s, new Comparator<String>() {
                final Pattern p = Pattern.compile("\\([0-9]{4}\\)");

                @Override
                public int compare(String o1, String o2) {
                    Matcher m1 = p.matcher(o1);
                    Matcher m2 = p.matcher(o2);

                    String s1 = m1.find() ? m1.group() : "";
                    String s2 = m2.find() ? m2.group() : "";
                    return s1.compareTo(s2);
                }
            });
            replaceSelectedText(mEditText, s);

        }
    }

    private void bindButton() {
        findViewById(R.id.item1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replaceString(MENU_S_PARENTHESIS);
            }
        });
        findViewById(R.id.item2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replaceString(MENU_S_SUBTRACTION);
            }
        });
        findViewById(R.id.item3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replaceString(MENU_S_ASTERISK);
            }
        });
        findViewById(R.id.item4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replaceString(MENU_S_CODE);
            }
        });

    }

    private void calculateExpression() {
        if (mSymbols == null) {
            mSymbols = new Symbols();
        }

        String input = mEditText.getText().toString();

        Pattern pattern = Pattern.compile("[0-9\\+\\-\\*\\.\\(\\)\\=/]+");
        Matcher matcher = pattern.matcher(input.split("\\={5}")[0]);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(input).append("\n\n\n=====\n\n\n");
        List<Double> results = new ArrayList<>();
        while (matcher.find()) {
            stringBuilder.append(matcher.group()).append(" => ");
            try {
                String result = Util.doubleToString(mSymbols.eval(matcher.group()), -1);
                results.add(Double.parseDouble(result));
                stringBuilder.append(result).append("\n\n");
            } catch (SyntaxException e) {
                stringBuilder.append(e.message);
            }
        }
        double addAll = 0;

        for (double i : results) {
            addAll += i;
        }
        stringBuilder.append("相加总结果：").append(addAll).append("\n\n\n");
        mEditText.setText(stringBuilder.toString());
    }

    private void replaceString(int type) {
        int si = mEditText.getSelectionStart();
        int ei = mEditText.getSelectionEnd();
        String str = mEditText.getText().toString();

        String s = str.substring(si, ei);
        String v = null;
        switch (type) {
            case MENU_S_CODE:
                v = Fomatter.formatCode(s);

                break;
            case MENU_S_HEAD:
                v = Fomatter.formatHead(s);
                break;
            case MENU_S_PARENTHESIS:
                v = " (" + s.trim() + ") ";
                break;
            case MENU_S_SUBTRACTION:
                v = "- " + s.trim();
                break;
            case MENU_S_DIVIDE:
                v = "/" + s.trim();
                break;
            case MENU_S_ASTERISK:
                v = "*" + s.trim();
                break;
        }
        String ASTERISKt = si != 0 ? str.substring(0, si) : "";
        String end = str.length() - 1 != ei ? str.substring(ei, str.length()) : "";

        mEditText.setText(ASTERISKt + v + end);
        mEditText.setSelection(si);
    }

    private void updateNote() {
        String content = mEditText.getText().toString();
        if (content == null || content.trim().length() == 0) return;
        if (mNote == null) {
            mNote = new Note();

            mNote.Title = content.split("\n")[0].trim();
            mNote.Content = content.trim();
            Databases.getInstance().insert(mNote);
        } else {
            mNote.Title = content.split("\n")[0].trim();
            mNote.Content = content.trim();
            Databases.getInstance().update(mNote);
        }
        mUpdated = true;
    }

    public static String getSelectedText(EditText editText) {
        CharSequence c = editText.getText();
        if (isNullOrWhiteSpace(c)) return null;
        String s = c.toString();
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        if (end > start)
            return s.substring(editText.getSelectionStart(), editText.getSelectionEnd());

        return null;
    }

    public static boolean isNullOrWhiteSpace(CharSequence charSequence) {
        if (charSequence == null) return true;
        int length = charSequence.length();
        for (int i = 0; i < length; i++) {
            if (!Character.isWhitespace(charSequence.charAt(i))) return false;
        }
        return true;
    }

    public static <T> String joining(List<T> list, String separator) {
        StringBuilder builder = new StringBuilder();
        for (T t : list) {
            builder.append(t).append(separator);
        }
        return builder.toString();
    }

    public static void replaceSelectedText(EditText editText, String str) {
        CharSequence c = editText.getText();
        if (isNullOrWhiteSpace(c)) return;
        String s = c.toString();
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        if (end > start) {
            String r1 = s.substring(0, start);
            String r2 = s.substring(end);

            editText.setText(r1 + str + r2);
        }


    }

    public static String sort(String s, Comparator<String> comparator) {
        if (s == null) return null;
        String[] lines = s.split("\n");
        List<String> sortLines = new ArrayList<>();
        String ch = new String(Character.toChars(160));

        for (String l : lines) {
            String s2 = l.trim().replace(ch," ");
            // Immoral Tales (1973)
            int i = sortLines.indexOf(s2);
            if (s2.length() == 0 || sortLines.indexOf(s2) != -1) continue;
            sortLines.add(s2);
        }
        Collections.sort(sortLines, comparator);
        return joining(sortLines, "\n");
    }

    @Override
    public void finish() {
        updateNote();
        if (mUpdated) {
            setResult(RESULT_OK);
        }
        mFinished = true;
        super.finish();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        mEditText = findViewById(R.id.edit_text);

        Intent intent = getIntent();

        long id = intent.getLongExtra(EXTRA_ID, 0);

        if (id != 0) {
            mNote = Databases.getInstance().fetchNote(id);
            mNote.ID = id;

            mEditText.setText(mNote.Content);

        }
        bindButton();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_CALCULATE, 0, "计算(公式)");
        menu.add(0, MENU_FORMAT, 0, "格式化");

        menu.add(0, MENU_S_HEAD, 0, "标题");

        menu.add(0, MENU_S_DIVIDE, 0, "/");
        menu.add(0, MENU_S_SORT_BY, 0, "排序");
        menu.add(0, MENU_S_SORT_BY_YEAR, 0, "排序(年代)");

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_CALCULATE:
                calculateExpression();
                return true;
            case android.R.id.home:
                finish();
                return true;
            case MENU_FORMAT:
                String[] lines = mEditText.getText().toString().split("\n");
                StringBuilder stringBuilder = new StringBuilder();

                for (String l : lines) {
                    if (l.trim().length() > 0) {
                        stringBuilder.append(l).append('\n').append('\n');
                    }
                }
                mEditText.setText(stringBuilder.toString());
                return true;
            case MENU_S_SORT_BY:
                actionSort();
                return true;
            case MENU_S_SORT_BY_YEAR:
                actionSortByYear();
                return true;
            case MENU_S_HEAD:

            case MENU_S_DIVIDE:
                replaceString(item.getItemId());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        if (!mFinished)
            updateNote();
        super.onPause();
    }
}
