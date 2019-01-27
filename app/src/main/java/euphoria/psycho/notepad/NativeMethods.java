package euphoria.psycho.notepad;

public class NativeMethods {
    static {
        System.loadLibrary("main");
    }

    public  static native void createDatabase(String fileName);
}
