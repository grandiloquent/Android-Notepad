package euphoria.psycho.notepad.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class BottomBar extends ViewGroup {
    private static final String TAG = "TAG/" + BottomBar.class.getCanonicalName();

    public BottomBar(Context context) {
        this(context, null);
    }

    public BottomBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        int mw = getMeasuredWidth();
        int w = mw / count;

        int top = 0;
        int right = w;
        int bottom = getMeasuredHeight();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            int offset = (i * w);
            int left = i * w;//(w - child.getMeasuredWidth()) / 2 + offset;
            Log.d(TAG, "onLayout: " + w + " " + left + " " + child.getMeasuredWidth() + " " + child.getMeasuredHeight());
            child.layout(left, top, left + w, bottom);
            "".trim();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int count = getChildCount();
        int mw = getMeasuredWidth();
        int w = mw / count;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;
            measureChild(child, w, heightMeasureSpec);
        }
        /*
        Log.d(TAG, "onMeasure: "
                + "width = " + MeasureSpec.toString(widthMeasureSpec) + "\n"
                + "height = " + MeasureSpec.toString(heightMeasureSpec));
        */
    }
}
