package euphoria.psycho.notepad;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import org.javia.arity.Symbols;
import org.javia.arity.SyntaxException;
import org.javia.arity.Util;

import java.util.ArrayList;
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
    private static final int MENU_S_SUBTRACTION = 0x8;
    private EditText mEditText;
    private boolean mFinished = false;
    private Note mNote;
    private Symbols mSymbols;

    private boolean mUpdated = false;

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
        mEditText = (EditText) findViewById(R.id.edit_text);


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
}
