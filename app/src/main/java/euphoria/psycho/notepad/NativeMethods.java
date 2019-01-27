package euphoria.psycho.notepad;

public class NativeMethods {
    static {
        System.loadLibrary("main");
    }

    public static native String startServer(String fileName, String staticDirectory);


}
