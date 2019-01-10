package euphoria.psycho.notepad;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

public class MainActivity extends Activity {

    private static final int ACTIVITY_EDIT = 0x1;
    private static final int ACTIVITY_VIEW = 0x2;
    private static final int CONTEXT_MENU_EDIT_NOTE = 0x1;
    private static final int CONTEXT_MENU_DELETE_NOTE = 0x2;
    private static final int MENU_ADD_NOTE = 0x1;
    private EditText mEditText;
    private ListView mListView;
    private NoteAdapter mNoteAdapter;

    private void addNote() {
        Intent intent = new Intent(this, EditNoteActivity.class);
        startActivityForResult(intent, ACTIVITY_EDIT);
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
        mNoteAdapter = new NoteAdapter(Databases.getInstance().fetchTitles(), this);

        mListView.setAdapter(mNoteAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                viewNote(mNoteAdapter.getItem(i).ID);
            }
        });

        mEditText = findViewById(R.id.edit_text);

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence != null && charSequence.toString().trim().length() > 0) {
                    mNoteAdapter.switchData(Databases.getInstance().searchTitle(charSequence.toString().trim()));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        mEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (i == KeyEvent.KEYCODE_ENTER && mEditText.getText().toString().trim().length() > 0) {

                    mNoteAdapter.switchData(Databases.getInstance().searchTitles(mEditText.getText().toString().trim()));

                    return true;
                }
                return false;
            }
        });
    }

    private void refreshListView() {

        mNoteAdapter.switchData(Databases.getInstance().fetchTitles());
    }

    private void viewNote(long id) {
        Intent intent = new Intent(this, ViewActivity.class);
        intent.putExtra(Constants.EXTRA_ID, id);
        startActivityForResult(intent, ACTIVITY_VIEW);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_ADD_NOTE, 0, "添加笔记");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ADD_NOTE:
                addNote();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(0, CONTEXT_MENU_EDIT_NOTE, 0, "编辑笔记");
        menu.add(0, CONTEXT_MENU_DELETE_NOTE, 0, "删除笔记");

        super.onCreateContextMenu(menu, v, menuInfo);
    }

    private void deleteNote(Note note) {
        Databases.getInstance().deleteNote(note);
        refreshListView();
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        initialize();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && (requestCode == ACTIVITY_EDIT || requestCode == ACTIVITY_VIEW)) {
            refreshListView();
        }
    }
}
