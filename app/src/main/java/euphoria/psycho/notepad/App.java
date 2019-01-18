package euphoria.psycho.notepad;

import android.app.Application;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AndroidContext.initialize(this);
    }
}
