package euphoria.psycho.notepad;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import euphoria.psycho.notepad.server.DatabaseHelper;
import euphoria.psycho.notepad.server.ServerActivity;

public class MainActivity extends Activity {

    private static final int ACTIVITY_EDIT = 0x1;
    private static final int ACTIVITY_VIEW = 0x2;
    private static final int CONTEXT_MENU_DELETE_NOTE = 0x2;
    private static final int CONTEXT_MENU_EDIT_NOTE = 0x1;
    private static final int MENU_ADD_NOTE = 0x1;
    private static final int MENU_START_SERVER = 0x2;
    private static final int MENU_RESEVER_STRIGN = 0x3;
    private static final int MENU_CHINESE_STRIGN = 0x4;
    private static final int MENU_CHINESE_STRIGN_1 = 0x5;

    private EditText mEditText;
    private ListView mListView;
    private NoteAdapter mNoteAdapter;

    private void addNote() {
        Intent intent = new Intent(this, EditNoteActivity.class);
        startActivityForResult(intent, ACTIVITY_EDIT);
    }

    private void deleteNote(Note note) {
        try {
            DatabaseHelper helper = DatabaseHelper.getInstance(AndroidContext.instance().get());
            Note n = helper.fetchNote(note.ID);
            String firstline = n.Content.trim().indexOf('\n') == -1 ? n.Content : n.Content.substring(0, n.Content.trim().indexOf('\n'));
            firstline = firstline.replaceAll("[\"<>|:*?/\\\\]+", " ");
            if (firstline.indexOf("# ") != -1) {
                firstline = firstline.substring(firstline.indexOf("# ") + 2);
            }
            File targetDirectory = new File(Environment.getExternalStorageDirectory(), "Notes");
            if (!targetDirectory.isDirectory()) targetDirectory.mkdir();
            targetDirectory = new File(targetDirectory, "Documents");
            if (!targetDirectory.isDirectory()) targetDirectory.mkdir();
            File targetFile = new File(targetDirectory, firstline + ".md");
            FileOutputStream outputStream = new FileOutputStream(targetFile);
            OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
            writer.write(n.Content);
            writer.flush();
            writer.close();
            helper.deleteNote(note);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        refreshListView();
    }

    private void editNote(long id) {
        Intent intent = new Intent(this, EditNoteActivity.class);
        intent.putExtra(Constants.EXTRA_ID, id);
        startActivityForResult(intent, ACTIVITY_EDIT);
    }

    private void initialize() {

        setContentView(R.layout.activity_main);

        mListView = findViewById(R.id.list_view);
        registerForContextMenu(mListView);
        mNoteAdapter = new NoteAdapter(DatabaseHelper.getInstance(AndroidContext.instance().get()).fetchTitles(), this);

        mListView.setAdapter(mNoteAdapter);

        mListView.setOnItemClickListener((adapterView, view, i, l) -> viewNote(mNoteAdapter.getItem(i).ID));

        mEditText = findViewById(R.id.edit_text);

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence != null && charSequence.toString().trim().length() > 0) {
                    mNoteAdapter.switchData(DatabaseHelper.getInstance(AndroidContext.instance().get()).searchTitle(charSequence.toString().trim()));
                } else {
                    mNoteAdapter.switchData(DatabaseHelper.getInstance(AndroidContext.instance().get()).fetchTitles());
                }
            }
        });
        mEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (i == KeyEvent.KEYCODE_ENTER && mEditText.getText().toString().trim().length() > 0) {

                    mNoteAdapter.switchData(DatabaseHelper.getInstance(AndroidContext.instance().get()).searchTitles(mEditText.getText().toString().trim()));

                    return true;
                }
                return false;
            }
        });
    }

    private void refreshListView() {

        mNoteAdapter.switchData(DatabaseHelper.getInstance(AndroidContext.instance().get()).fetchTitles());
    }

    private void startServer() {
        Intent intent = new Intent(this, ServerActivity.class);
        startActivity(intent);
    }

    private void viewNote(long id) {
        Intent intent = new Intent(this, ViewActivity.class);
        intent.putExtra(Constants.EXTRA_ID, id);
        startActivityForResult(intent, ACTIVITY_VIEW);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && (requestCode == ACTIVITY_EDIT || requestCode == ACTIVITY_VIEW)) {
            refreshListView();
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId()) {
            case CONTEXT_MENU_EDIT_NOTE:
                editNote(mNoteAdapter.getItem(menuInfo.position).ID);
                return true;
            case CONTEXT_MENU_DELETE_NOTE:
                deleteNote(mNoteAdapter.getItem(menuInfo.position));
                return true;

        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedUtils.setContext(this);
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int r = 99;
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, r);
            return;
        }
        initialize();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(0, CONTEXT_MENU_EDIT_NOTE, 0, "编辑笔记");
        menu.add(0, CONTEXT_MENU_DELETE_NOTE, 0, "删除笔记");

        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_ADD_NOTE, 0, "添加笔记");
        menu.add(0, MENU_START_SERVER, 0, "服务");
        menu.add(0, MENU_RESEVER_STRIGN, 0, "翻转字符串");
        menu.add(0, MENU_CHINESE_STRIGN, 0, "翻转中文字符串");
        menu.add(0, MENU_CHINESE_STRIGN_1, 0, "传统中文");

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ADD_NOTE:
                addNote();
                return true;
            case MENU_START_SERVER:
                startServer();
                return true;
            case MENU_RESEVER_STRIGN:
                reverseString();
                return true;
            case MENU_CHINESE_STRIGN:
                StringUtils.transformTraditionalChinese(this);
                return true;
            case MENU_CHINESE_STRIGN_1:
                StringUtils.transformTraditionalChineseSpace(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }




    private void reverseString() {

        ClipboardManager manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = manager.getPrimaryClip();
        if (clipData == null || clipData.getItemCount() == 0) return;
        if (clipData.getItemAt(0).getText() == null) return;
        String content = clipData.getItemAt(0).getText().toString();
        StringBuilder stringBuilder = new StringBuilder(content.length());

        manager.setPrimaryClip(ClipData.newPlainText(null, stringBuilder.reverse().toString()));


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        initialize();
    }
}
