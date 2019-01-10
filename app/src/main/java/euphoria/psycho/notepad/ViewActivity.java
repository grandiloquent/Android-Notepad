package euphoria.psycho.notepad;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.*;
import android.text.style.BackgroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.io.File;
import java.io.FileOutputStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static euphoria.psycho.notepad.Constants.EXTRA_ID;
import static euphoria.psycho.notepad.Constants.EXTRA_SEARCH;


public class ViewActivity extends Activity {

    private static final int MENU_EDIT = 0x3;
    private static final int MENU_PICTURE = 0x5;
    private static final int MENU_PICTURE_NO = 0x7;
    private static final int MENU_MARKDOWN = 0x8;

    private static final int REQUEST_EDIT_ACTIVITY = 0x2;
    private Note mNote;
    private ScrollView mScrollView;
    private TextView mTextView;

    private static void bringPointIntoView(TextView textView,
                                           ScrollView scrollView, int offset) {
        int line = textView.getLayout().getLineForOffset(offset);
        //+ 0.5
        int y = (int) ((line) * textView.getLineHeight());
        scrollView.scrollTo(0, y);
    }

    private void editNote() {
        Intent intent = new Intent(this, EditNoteActivity.class);
        intent.putExtra(EXTRA_ID, mNote.ID);
        startActivityForResult(intent, REQUEST_EDIT_ACTIVITY);
    }

    private void highlightString(String input) {
        //Get the text from text view and create a spannable string
        SpannableString spannableString = new SpannableString(mTextView.getText());

        //Get the previous spans and remove them
        BackgroundColorSpan[] backgroundSpans = spannableString.getSpans(0, spannableString.length(), BackgroundColorSpan.class);

        for (BackgroundColorSpan span : backgroundSpans) {
            spannableString.removeSpan(span);
        }

        //Search for all occurrences of the keyword in the string
        int indexOfKeyword = spannableString.toString().indexOf(input);

        int firstPosition = 0;
        while (indexOfKeyword > 0) {
            if (firstPosition == 0) {
                firstPosition = indexOfKeyword;
            }
            //Create a background color span on the keyword
            spannableString.setSpan(new BackgroundColorSpan(Color.YELLOW), indexOfKeyword, indexOfKeyword + input.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            //Get the next index of the keyword
            indexOfKeyword = spannableString.toString().indexOf(input, indexOfKeyword + input.length());
        }
        final int p = firstPosition;
        //Set the final text on TextView
        mTextView.setText(spannableString);
        // bringPointIntoView(mTextView, mScrollView, p);
        if (firstPosition > 0) {
            mScrollView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    bringPointIntoView(mTextView, mScrollView, p);
                }
            }, 1000);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_EDIT, 0, "编辑");
        menu.add(0, MENU_PICTURE, 0, "转换为图片(二维码)");
        menu.add(0, MENU_PICTURE_NO, 0, "转换为图片");
        menu.add(0, MENU_MARKDOWN, 0, "转换为HTML");

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_EDIT:
                editNote();
                return true;
            case MENU_PICTURE:
                TextDrawUtils.drawTextImage(mTextView.getText().toString(), 580, this);
                //generatePicture(mTextView.getText().toString());
                return true;
            case MENU_PICTURE_NO:
                TextDrawUtils.drawTextImageNoQr(mTextView.getText().toString(), 580, this);
                //generatePicture(mTextView.getText().toString());
                return true;
            case android.R.id.home:
                finish();
                return true;
            case MENU_MARKDOWN:
                markdown2html(mTextView.getText().toString());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);
        Intent intent = getIntent();
        long id = intent.getLongExtra(EXTRA_ID, 0);
        if (id == 0) finish();
        mNote = Databases.getInstance().fetchNote(id);
        mNote.ID = id;
        mTextView = (TextView) findViewById(R.id.text_view);
        mTextView.setText(mNote.Content);
        mScrollView = (ScrollView) findViewById(R.id.scroll_view);


        String searchWord = intent.getStringExtra(EXTRA_SEARCH);
        if (searchWord == null) return;
        highlightString(searchWord);


    }

    private void generatePicture(String str) {
        final TextPaint textPaint = buildTextPaint(this);

        String[] splited = str.trim().split("\n", 2);

        String title = splited[0].trim();
        String content = splited[1].trim();

        int width = 580;
        int offsetVertical = 30;

        textPaint.setTextSize(30);
        StaticLayout titleStaticLayout = new StaticLayout(title, textPaint, width, Layout.Alignment.ALIGN_NORMAL, 1.25f, 0.0f, false);


        int titleHeight = titleStaticLayout.getHeight();
        textPaint.setTextSize(22);

        StaticLayout contentStaticLayout = new StaticLayout(content, textPaint, width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

        int contentHeight = contentStaticLayout.getHeight();

        Bitmap bitmap = Bitmap.createBitmap(width + (offsetVertical * 2), titleHeight + contentHeight + (offsetVertical * 3), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        canvas.translate(offsetVertical, offsetVertical);
        canvas.drawColor(0xFFFFFFFF);


        textPaint.setColor(Color.RED);
        textPaint.setTextSize(30);
        titleStaticLayout.draw(canvas);

        canvas.translate(0, offsetVertical * 2);


        textPaint.setColor(0xFF333333);
        textPaint.setTextSize(22);
        contentStaticLayout.draw(canvas);

        FileOutputStream out;

        try {
            File file = generateFile();
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();
            bitmap.recycle();
            triggerScanFile(file);
            Toast.makeText(this, file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void triggerScanFile(File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(file);
        mediaScanIntent.setData(uri);
        sendBroadcast(mediaScanIntent);

    }

    private File generateFile() {
        File topDirectory = new File(Environment.getExternalStorageDirectory(), "Notes");
        if (!topDirectory.exists()) {
            topDirectory.mkdir();
        }
        File imagesDirectory = new File(topDirectory, "images");
        if (!imagesDirectory.exists()) {
            imagesDirectory.mkdir();
        }
        Format formatter = new SimpleDateFormat("YYYY-MM-dd_hh-mm-ss");//
        File targetFile = new File(imagesDirectory, formatter.format(new Date()) + ".jpg");
        return targetFile;
    }

    private TextPaint buildTextPaint(Context context) {

        TextPaint textPaint = new TextPaint();

        textPaint.density = context.getResources().getDisplayMetrics().density;
        textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        //textPaint.setTextAlign(Paint.Align.LEFT);
        return textPaint;
    }

    private void markdown2html(String text) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(text);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String content = renderer.render(document);
        String patternString = SharedUtils.readStringFromFile(new File(Environment.getExternalStorageDirectory(), "/Notes/dom.html"));

        String output = patternString.replace("###", content);

        output = encodeImage(output);
        SharedUtils.writeStringToFile(new File(Environment.getExternalStorageDirectory(), "/Notes/a.html"), output);

    }

    private String encodeImage(String content) {
        Pattern pattern = Pattern.compile("src=\"([^\"]+jpg)\"");

        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            File file = new File(Environment.getExternalStorageDirectory(), "/Notes/" + matcher.group(1));
            if (file.exists()) {
                String base64 = Utils.readFileAsBase64String(file);
                content = content.replace(matcher.group(), "src=\"" + base64 + "\"");
            }
        }
        return content;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT_ACTIVITY && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        }
    }
}
