package euphoria.psycho.notepad;

import android.app.Activity;
import android.content.Intent;
import android.graphics.*;
import android.net.Uri;
import android.os.Environment;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class TextDrawUtils {

    private static int getColor() {

        int[] colors = new int[]{
                0XFFF44336,
                0XFFE91E63,
                0XFF9C27B0,
                0XFF673AB7,
                0XFF3F51B5,
                0XFF2196F3,
        };
        //Random r=new Random();
        //r.setSeed(new Date().getTime());
        int v = new Random(new Date().getTime()).nextInt(colors.length);
        return colors[v - 1 > 0 ? v - 1 : 0];
    }

    private static File getFile() {
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

    private static void triggerScanFile(File file, Activity activity) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(file);
        mediaScanIntent.setData(uri);
        activity.sendBroadcast(mediaScanIntent);

    }

    private static void saveJpg(Bitmap bitmap, Activity activity) {

        File file = getFile();
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            triggerScanFile(file, activity);
            Toast.makeText(activity, file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            os.close();
            bitmap.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void drawTextImage(String text, int width, Activity activity) {

        Bitmap qr = BitmapFactory.decodeResource(activity.getResources(), R.mipmap.webwxgetmsgimg);

        int titleFontSize = 30;
        int normalFontSize = 22;
        int verticalMargin = 30;
        int horizontalMargin = 30;

        String[] lines = text.split("\n", 2);
        String title = lines[0];
        String content = lines[1];

        final TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);

        textPaint.setTextSize(titleFontSize);
        StaticLayout titleLayout = new StaticLayout(title, textPaint, width, Layout.Alignment.ALIGN_CENTER, 1.25f, 0.0f, false);


        textPaint.setTextSize(normalFontSize);
        StaticLayout contentLayout = new StaticLayout(content, textPaint, width, Layout.Alignment.ALIGN_NORMAL, 1f, 0.0f, false);


        int height = titleLayout.getHeight() + (verticalMargin << 1) + contentLayout.getHeight();


        Bitmap bitmap = Bitmap.createBitmap(width + (horizontalMargin << 1), height+ verticalMargin + verticalMargin * 3 - 15 + qr.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(getColor());
        textPaint.setColor(Color.WHITE);

        canvas.translate(horizontalMargin, verticalMargin);
        textPaint.setTextSize(titleFontSize);
        titleLayout.draw(canvas);

        canvas.translate(0, titleLayout.getHeight() + verticalMargin);

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(3);
        paint.setAntiAlias(true);
        paint.setPathEffect(new DashPathEffect(new float[]{5, 10}, 0));
        canvas.drawLine(0, 0, width, 0, paint);

        canvas.translate(0, verticalMargin >> 1);
        textPaint.setTextSize(normalFontSize);
        contentLayout.draw(canvas);
        canvas.drawLine(0, contentLayout.getHeight() + 15+ verticalMargin, width, contentLayout.getHeight() + 15+ verticalMargin, paint);


        canvas.drawBitmap(qr, (width - qr.getWidth()) >> 1, contentLayout.getHeight() + 60, paint);
        saveJpg(bitmap, activity);
        qr.recycle();
    }

    public static void drawTextImageNoQr(String text, int width, Activity activity) {


        int titleFontSize = 30;
        int normalFontSize = 22;
        int verticalMargin = 30;
        int horizontalMargin = 30;

        String[] lines = text.split("\n", 2);
        String title = lines[0];
        String content = lines[1];

        final TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);

        textPaint.setTextSize(titleFontSize);
        StaticLayout titleLayout = new StaticLayout(title, textPaint, width, Layout.Alignment.ALIGN_CENTER, 1.25f, 0.0f, false);


        textPaint.setTextSize(normalFontSize);
        StaticLayout contentLayout = new StaticLayout(content, textPaint, width, Layout.Alignment.ALIGN_NORMAL, 1f, 0.0f, false);


        int height = titleLayout.getHeight() + (verticalMargin << 1) + contentLayout.getHeight();


        Bitmap bitmap = Bitmap.createBitmap(width + (horizontalMargin << 1), height + verticalMargin+ verticalMargin * 3, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(getColor());
        textPaint.setColor(Color.WHITE);

        canvas.translate(horizontalMargin, verticalMargin);
        textPaint.setTextSize(titleFontSize);
        titleLayout.draw(canvas);

        canvas.translate(0, titleLayout.getHeight() + verticalMargin);

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(3);
        paint.setAntiAlias(true);
        paint.setPathEffect(new DashPathEffect(new float[]{5, 10}, 0));
        canvas.drawLine(0, 0, width, 0, paint);

        canvas.translate(0, verticalMargin >> 1);
        textPaint.setTextSize(normalFontSize);
        contentLayout.draw(canvas);
        canvas.drawLine(0, contentLayout.getHeight() + 15+ verticalMargin, width, contentLayout.getHeight() + 15+ verticalMargin, paint);


        saveJpg(bitmap, activity);

    }
}
