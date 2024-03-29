package euphoria.psycho.notepad.server;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.File;

import euphoria.psycho.notepad.NativeMethods;

import static euphoria.psycho.notepad.server.Utils.getDeviceIP;


public class ServerService extends Service {
    public static final boolean DEBUG = true;
    private static final String CHANNEL_ID = "_tag_";
    private static final int FOREGROUND_ID = 1 << 1;
    private static final int PORT = 8090;
    private static final String TAG = "TAG/" + ServerService.class.getCanonicalName();
    private final IBinder mBinder = new ServerBinder();
    private String mUrl;

    private Notification buildForegroundNotification() {
        Notification.Builder b = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            b = new Notification.Builder(this, CHANNEL_ID);
        } else {
            b = new Notification.Builder(this);
        }

        b
                .setContentTitle("视频")
                .setContentText("开启视频分享服务")
                .setSmallIcon(android.R.drawable.stat_sys_download);

        return (b.build());
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "视频";
            String description = "视频分享服务";
            int importance = NotificationManager.IMPORTANCE_NONE;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public String getURL() {
        return mUrl;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();


        mUrl = NativeMethods.startServer(new File(Environment.getExternalStorageDirectory(), "notes_notepad.db").getAbsolutePath(),
                new File(Environment.getExternalStorageDirectory(), "server").getAbsolutePath());

        if (DEBUG) {
            Log.d(TAG, "onCreate: " + mUrl);
        }


//        mSimpleServer = new SimpleServer.Builder(getDeviceIP(this), PORT)
//                .setStaticDirectory(cacheDirectory.getAbsolutePath())
//                .build();
//        if (mSimpleServer != null)
//            Log.e(TAG, mSimpleServer.getURL());

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        startForeground(FOREGROUND_ID, buildForegroundNotification());
        return super.onStartCommand(intent, flags, startId);
    }

    public class ServerBinder extends Binder {
        public ServerService getService() {
            return ServerService.this;
        }
    }
}
