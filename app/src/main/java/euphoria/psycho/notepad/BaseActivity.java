package euphoria.psycho.notepad;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;


public abstract class BaseActivity extends Activity {

    public static final int REQUEST_CODE_PERMISSIONS = 11;

    public void bindViews() {

    }

    protected abstract int getLayoutId();

    protected abstract String[] getNeedPermissions();

    protected abstract int getOptionsMenu();


    public void initView() {

    }

    public void initialize() {
        setContentView(getLayoutId());
        bindViews();
        initView();
    }


    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String[] permissions = getNeedPermissions();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && permissions != null
                && permissions.length > 0) {
            requestPermissions(permissions, REQUEST_CODE_PERMISSIONS);
        } else {
            initialize();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (getOptionsMenu() > 0) {
            getMenuInflater().inflate(getOptionsMenu(), menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Missing necessary permission: " + permissions[i], Toast.LENGTH_LONG).show();
                return;
            }
        }
        initialize();
    }
}
