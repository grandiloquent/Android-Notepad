package euphoria.psycho.notepad;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

public class NoteAdapter extends BaseAdapter {

    private final Context mContext;
    private final List<Note> mNotes;

    public void switchData(List<Note> notes) {
        mNotes.clear();
        mNotes.addAll(notes);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mNotes == null || mNotes.size() == 0  ? 0 : mNotes.size();
    }

    @Override
    public Note getItem(int i) {
        return mNotes == null || mNotes.size() == 0 ? null : mNotes.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.list_item, viewGroup, false);
            viewHolder = new ViewHolder();
            viewHolder.textView = view.findViewById(R.id.text_view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.textView.setText(mNotes.get(i).Title);
        return view;
    }

    public NoteAdapter(List<Note> notes, Context context) {
        mNotes = notes;
        mContext = context;
    }
}