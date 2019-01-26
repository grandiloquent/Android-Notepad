package euphoria.psycho.notepad.server;

import android.text.Html;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import euphoria.psycho.notepad.AndroidContext;
import euphoria.psycho.notepad.Note;

import static euphoria.psycho.notepad.server.Utils.addAll;
import static euphoria.psycho.notepad.server.Utils.closeQuietly;
import static euphoria.psycho.notepad.server.Utils.findVideoFile;
import static euphoria.psycho.notepad.server.Utils.getDefaultReason;
import static euphoria.psycho.notepad.server.Utils.getMimeTypeTable;
import static euphoria.psycho.notepad.server.Utils.isVideo;
import static euphoria.psycho.notepad.server.Utils.lookup;
import static euphoria.psycho.notepad.server.Utils.parseHeaders;
import static euphoria.psycho.notepad.server.Utils.parseQuery;
import static euphoria.psycho.notepad.server.Utils.parseURL;
import static euphoria.psycho.notepad.server.Utils.sliceHeader;
import static euphoria.psycho.notepad.server.Utils.sliceURL;
import static euphoria.psycho.notepad.server.Utils.substringAfter;
import static euphoria.psycho.notepad.server.Utils.substringAfterLast;
import static euphoria.psycho.notepad.server.Utils.substringBefore;
import static euphoria.psycho.notepad.server.Utils.trim;

public class SimpleServer {

    private static final byte[] BYTES_DOUBLE_LINE_FEED = new byte[]{'\r', '\n', '\r', '\n'};
    private static final byte[] BYTES_LINE_FEED = new byte[]{'\r', '\n'};
    private static final String DATE_FORMAT_GMT = " EEE, dd MMM yyyy hh:mm:ss 'GMT'";
    private static final int DEFAULT_BUFFER_SIZE = 8 * 1024;
    private static final String HEADER_VALUE_NO_CACHE = "no-cache";
    static final String HTTP_ACCEPT_RANGES = "Accept-Ranges";
    private static final String HTTP_CACHE_CONTROL = "Cache-Control";
    static final String HTTP_CONTENT_DISPOSITION = "Content-Disposition";
    private static final String HTTP_CONTENT_RANGE = "Content-Range";
    private static final String HTTP_CONTENT_TYPE = "Content-Type";
    private static final String HTTP_DATE = "Date";
    private static final String HTTP_RANGE = "Range";
    private static final int MILLIS_PER_SECOND = 1000;
    private static final int MILLIS_PER_MINUTE = MILLIS_PER_SECOND * 60; //     60,000
    private static final int MILLIS_PER_HOUR = MILLIS_PER_MINUTE * 60;   //  3,600,000
    static final int MILLIS_PER_DAY = MILLIS_PER_HOUR * 24;      // 86,400,000
    private static final int STATUS_CODE_BAD_REQUEST = 400;
    private static final int STATUS_CODE_INTERNAL_SERVER_ERROR = 500;
    private static final int STATUS_CODE_NOT_FOUND = 404;
    private static final int STATUS_CODE_OK = 200;
    private static final int STATUS_CODE_PARTIAL_CONTENT = 206;
    private static final String TAG = "Funny/SimpleServer";
    private static final String UTF_8 = "UTF-8";

    private static final byte[][] getmBytesMarkdown = new byte[][]{
/* 0 links */new byte[]{60, 33, 68, 79, 67, 84, 89, 80, 69, 32, 104, 116, 109, 108, 62, 60, 104, 116, 109, 108, 32, 108, 97, 110, 103, 61, 34, 101, 110, 34, 62, 60, 104, 101, 97, 100, 62, 60, 109, 101, 116, 97, 32, 99, 104, 97, 114, 115, 101, 116, 61, 34, 85, 84, 70, 45, 56, 34, 47, 62, 60, 109, 101, 116, 97, 32, 110, 97, 109, 101, 61, 34, 118, 105, 101, 119, 112, 111, 114, 116, 34, 32, 99, 111, 110, 116, 101, 110, 116, 61, 34, 119, 105, 100, 116, 104, 61, 100, 101, 118, 105, 99, 101, 45, 119, 105, 100, 116, 104, 44, 32, 105, 110, 105, 116, 105, 97, 108, 45, 115, 99, 97, 108, 101, 61, 49, 46, 48, 34, 47, 62, 60, 109, 101, 116, 97, 32, 104, 116, 116, 112, 45, 101, 113, 117, 105, 118, 61, 34, 88, 45, 85, 65, 45, 67, 111, 109, 112, 97, 116, 105, 98, 108, 101, 34, 32, 99, 111, 110, 116, 101, 110, 116, 61, 34, 105, 101, 61, 101, 100, 103, 101, 34, 47, 62, 60, 116, 105, 116, 108, 101, 62, 68, 111, 99, 117, 109, 101, 110, 116, 60, 47, 116, 105, 116, 108, 101, 62, 60, 108, 105, 110, 107, 32, 114, 101, 108, 61, 34, 115, 116, 121, 108, 101, 115, 104, 101, 101, 116, 34, 32, 104, 114, 101, 102, 61, 34, 114, 101, 115, 101, 116, 46, 99, 115, 115, 34, 47, 62, 60, 108, 105, 110, 107, 32, 114, 101, 108, 61, 34, 115, 116, 121, 108, 101, 115, 104, 101, 101, 116, 34, 32, 104, 114, 101, 102, 61, 34, 108, 97, 121, 111, 117, 116, 46, 99, 115, 115, 34, 47, 62, 60, 108, 105, 110, 107, 32, 114, 101, 108, 61, 34, 115, 116, 121, 108, 101, 115, 104, 101, 101, 116, 34, 32, 104, 114, 101, 102, 61, 34, 116, 111, 97, 115, 116, 46, 99, 115, 115, 34, 47, 62, 60, 108, 105, 110, 107, 32, 114, 101, 108, 61, 34, 115, 116, 121, 108, 101, 115, 104, 101, 101, 116, 34, 32, 104, 114, 101, 102, 61, 34, 104, 116, 116, 112, 115, 58, 47, 47, 99, 100, 110, 106, 115, 46, 99, 108, 111, 117, 100, 102, 108, 97, 114, 101, 46, 99, 111, 109, 47, 97, 106, 97, 120, 47, 108, 105, 98, 115, 47, 115, 105, 109, 112, 108, 101, 109, 100, 101, 47, 49, 46, 49, 49, 46, 50, 47, 115, 105, 109, 112, 108, 101, 109, 100, 101, 46, 109, 105, 110, 46, 99, 115, 115, 34, 47, 62, 60, 108, 105, 110, 107, 32, 114, 101, 108, 61, 34, 115, 116, 121, 108, 101, 115, 104, 101, 101, 116, 34, 32, 104, 114, 101, 102, 61, 34, 104, 116, 116, 112, 115, 58, 47, 47, 99, 100, 110, 46, 106, 115, 100, 101, 108, 105, 118, 114, 46, 110, 101, 116, 47, 110, 112, 109, 47, 116, 111, 97, 115, 116, 105, 102, 121, 45, 106, 115, 47, 115, 114, 99, 47, 116, 111, 97, 115, 116, 105, 102, 121, 46, 109, 105, 110, 46, 99, 115, 115, 34, 47, 62, 60, 115, 99, 114, 105, 112, 116, 32, 115, 114, 99, 61, 34, 104, 116, 116, 112, 115, 58, 47, 47, 99, 100, 110, 106, 115, 46, 99, 108, 111, 117, 100, 102, 108, 97, 114, 101, 46, 99, 111, 109, 47, 97, 106, 97, 120, 47, 108, 105, 98, 115, 47, 115, 105, 109, 112, 108, 101, 109, 100, 101, 47, 49, 46, 49, 49, 46, 50, 47, 115, 105, 109, 112, 108, 101, 109, 100, 101, 46, 109, 105, 110, 46, 106, 115, 34, 62, 60, 47, 115, 99, 114, 105, 112, 116, 62, 60, 98, 111, 100, 121, 62, 60, 100, 105, 118, 32, 99, 108, 97, 115, 115, 61, 34, 119, 114, 97, 112, 112, 101, 114, 34, 62, 60, 115, 101, 99, 116, 105, 111, 110, 32, 99, 108, 97, 115, 115, 61, 34, 112, 97, 110, 101, 108, 32, 95, 110, 97, 118, 34, 62, 60, 104, 101, 97, 100, 101, 114, 32, 99, 108, 97, 115, 115, 61, 34, 112, 97, 110, 101, 108, 95, 95, 104, 101, 97, 100, 101, 114, 34, 62, 60, 100, 105, 118, 32, 99, 108, 97, 115, 115, 61, 34, 99, 111, 110, 116, 97, 105, 110, 101, 114, 34, 62, 60, 102, 111, 114, 109, 32, 99, 108, 97, 115, 115, 61, 34, 115, 101, 97, 114, 99, 104, 45, 98, 111, 120, 34, 62, 60, 108, 97, 98, 101, 108, 32, 102, 111, 114, 61, 34, 115, 101, 97, 114, 99, 104, 45, 98, 111, 120, 95, 95, 105, 110, 112, 117, 116, 34, 32, 99, 108, 97, 115, 115, 61, 34, 115, 101, 97, 114, 99, 104, 45, 98, 111, 120, 95, 95, 108, 97, 98, 101, 108, 34, 62, 60, 47, 108, 97, 98, 101, 108, 62, 32, 60, 105, 110, 112, 117, 116, 32, 116, 121, 112, 101, 61, 34, 116, 101, 120, 116, 34, 32, 99, 108, 97, 115, 115, 61, 34, 115, 101, 97, 114, 99, 104, 45, 98, 111, 120, 95, 95, 105, 110, 112, 117, 116, 34, 47, 62, 60, 100, 105, 118, 32, 99, 108, 97, 115, 115, 61, 34, 115, 101, 97, 114, 99, 104, 45, 98, 111, 120, 95, 95, 99, 108, 101, 97, 114, 34, 32, 116, 105, 116, 108, 101, 61, 34, 67, 108, 101, 97, 114, 34, 62, 60, 47, 100, 105, 118, 62, 60, 47, 102, 111, 114, 109, 62, 60, 47, 100, 105, 118, 62, 60, 47, 104, 101, 97, 100, 101, 114, 62, 60, 110, 97, 118, 32, 99, 108, 97, 115, 115, 61, 34, 112, 97, 110, 101, 108, 95, 95, 99, 111, 110, 116, 101, 110, 116, 34, 62, 60, 100, 105, 118, 32, 99, 108, 97, 115, 115, 61, 34, 99, 111, 110, 116, 97, 105, 110, 101, 114, 32, 95, 110, 97, 118, 34, 62, 60, 109, 101, 110, 117, 32, 99, 108, 97, 115, 115, 61, 34, 110, 97, 118, 45, 116, 114, 101, 101, 34, 62, 60, 117, 108, 32, 99, 108, 97, 115, 115, 61, 34, 110, 97, 118, 45, 116, 114, 101, 101, 95, 95, 108, 105, 115, 116, 34, 62},
/* 1  */new byte[]{60, 47, 117, 108, 62, 60, 47, 109, 101, 110, 117, 62, 60, 47, 100, 105, 118, 62, 60, 47, 110, 97, 118, 62, 60, 47, 115, 101, 99, 116, 105, 111, 110, 62, 60, 109, 97, 105, 110, 32, 99, 108, 97, 115, 115, 61, 34, 112, 97, 110, 101, 108, 32, 95, 109, 97, 105, 110, 32, 95, 119, 105, 116, 104, 111, 117, 116, 45, 115, 105, 100, 101, 98, 108, 111, 99, 107, 34, 62, 60, 104, 101, 97, 100, 101, 114, 32, 99, 108, 97, 115, 115, 61, 34, 112, 97, 110, 101, 108, 95, 95, 104, 101, 97, 100, 101, 114, 32, 104, 101, 97, 100, 101, 114, 32, 95, 102, 105, 120, 101, 100, 32, 95, 99, 108, 97, 109, 112, 101, 100, 34, 62, 60, 100, 105, 118, 32, 99, 108, 97, 115, 115, 61, 34, 99, 111, 110, 116, 97, 105, 110, 101, 114, 32, 104, 101, 97, 100, 101, 114, 95, 95, 99, 111, 110, 116, 97, 105, 110, 101, 114, 34, 62, 60, 100, 105, 118, 32, 99, 108, 97, 115, 115, 61, 34, 104, 101, 97, 100, 101, 114, 95, 95, 116, 105, 116, 108, 101, 34, 62, 60, 104, 51, 32, 105, 100, 61, 34, 116, 105, 116, 108, 101, 34, 62, 60, 47, 104, 51, 62, 60, 100, 105, 118, 32, 99, 108, 97, 115, 115, 61, 34, 112, 97, 110, 101, 108, 45, 116, 114, 105, 103, 103, 101, 114, 34, 62, 60, 47, 100, 105, 118, 62, 60, 47, 100, 105, 118, 62, 60, 100, 105, 118, 32, 99, 108, 97, 115, 115, 61, 34, 104, 101, 97, 100, 101, 114, 95, 95, 99, 111, 110, 116, 114, 111, 108, 115, 34, 62, 60, 47, 100, 105, 118, 62, 60, 47, 100, 105, 118, 62, 60, 47, 104, 101, 97, 100, 101, 114, 62, 60, 100, 105, 118, 32, 99, 108, 97, 115, 115, 61, 34, 112, 97, 110, 101, 108, 95, 95, 104, 101, 97, 100, 101, 114, 32, 104, 101, 97, 100, 101, 114, 32, 95, 101, 109, 117, 108, 97, 116, 111, 114, 34, 32, 115, 116, 121, 108, 101, 61, 34, 104, 101, 105, 103, 104, 116, 58, 50, 50, 112, 120, 34, 62, 60, 47, 100, 105, 118, 62, 60, 115, 101, 99, 116, 105, 111, 110, 32, 99, 108, 97, 115, 115, 61, 34, 112, 97, 110, 101, 108, 95, 95, 99, 111, 110, 116, 101, 110, 116, 34, 62, 60, 116, 101, 120, 116, 97, 114, 101, 97, 32, 105, 100, 61, 34, 101, 100, 105, 116, 45, 116, 101, 120, 116, 34, 62, 60, 47, 116, 101, 120, 116, 97, 114, 101, 97, 62, 60, 47, 115, 101, 99, 116, 105, 111, 110, 62, 60, 47, 109, 97, 105, 110, 62, 60, 47, 100, 105, 118, 62, 60, 100, 105, 118, 32, 99, 108, 97, 115, 115, 61, 34, 116, 111, 97, 115, 116, 95, 95, 108, 97, 121, 111, 117, 116, 32, 116, 111, 97, 115, 116, 45, 45, 104, 105, 100, 100, 101, 110, 34, 62, 60, 100, 105, 118, 32, 99, 108, 97, 115, 115, 61, 34, 116, 111, 97, 115, 116, 95, 95, 99, 111, 110, 116, 97, 105, 110, 101, 114, 34, 62, 60, 100, 105, 118, 32, 99, 108, 97, 115, 115, 61, 34, 116, 111, 97, 115, 116, 95, 95, 99, 101, 108, 108, 34, 62, 60, 100, 105, 118, 32, 99, 108, 97, 115, 115, 61, 34, 116, 111, 97, 115, 116, 32, 116, 111, 97, 115, 116, 45, 45, 121, 101, 108, 108, 111, 119, 32, 97, 100, 100, 45, 109, 97, 114, 103, 105, 110, 34, 62, 60, 100, 105, 118, 32, 99, 108, 97, 115, 115, 61, 34, 116, 111, 97, 115, 116, 95, 95, 105, 99, 111, 110, 34, 62, 60, 115, 118, 103, 32, 118, 101, 114, 115, 105, 111, 110, 61, 34, 49, 46, 49, 34, 32, 99, 108, 97, 115, 115, 61, 34, 116, 111, 97, 115, 116, 95, 95, 115, 118, 103, 34, 32, 120, 109, 108, 110, 115, 61, 34, 104, 116, 116, 112, 58, 47, 47, 119, 119, 119, 46, 119, 51, 46, 111, 114, 103, 47, 50, 48, 48, 48, 47, 115, 118, 103, 34, 32, 120, 109, 108, 110, 115, 58, 120, 108, 105, 110, 107, 61, 34, 104, 116, 116, 112, 58, 47, 47, 119, 119, 119, 46, 119, 51, 46, 111, 114, 103, 47, 49, 57, 57, 57, 47, 120, 108, 105, 110, 107, 34, 32, 120, 61, 34, 48, 112, 120, 34, 32, 121, 61, 34, 48, 112, 120, 34, 32, 118, 105, 101, 119, 66, 111, 120, 61, 34, 48, 32, 48, 32, 51, 48, 49, 46, 54, 57, 49, 32, 51, 48, 49, 46, 54, 57, 49, 34, 32, 115, 116, 121, 108, 101, 61, 34, 101, 110, 97, 98, 108, 101, 45, 98, 97, 99, 107, 103, 114, 111, 117, 110, 100, 58, 110, 101, 119, 32, 48, 32, 48, 32, 51, 48, 49, 46, 54, 57, 49, 32, 51, 48, 49, 46, 54, 57, 49, 34, 32, 120, 109, 108, 58, 115, 112, 97, 99, 101, 61, 34, 112, 114, 101, 115, 101, 114, 118, 101, 34, 62, 60, 103, 62, 60, 112, 111, 108, 121, 103, 111, 110, 32, 112, 111, 105, 110, 116, 115, 61, 34, 49, 49, 57, 46, 49, 53, 49, 44, 48, 32, 49, 50, 57, 46, 54, 44, 50, 49, 56, 46, 52, 48, 54, 32, 49, 55, 50, 46, 48, 54, 44, 50, 49, 56, 46, 52, 48, 54, 32, 49, 56, 50, 46, 53, 52, 44, 48, 32, 32, 34, 62, 60, 47, 112, 111, 108, 121, 103, 111, 110, 62, 60, 114, 101, 99, 116, 32, 120, 61, 34, 49, 51, 48, 46, 53, 54, 51, 34, 32, 121, 61, 34, 50, 54, 49, 46, 49, 54, 56, 34, 32, 119, 105, 100, 116, 104, 61, 34, 52, 48, 46, 53, 50, 53, 34, 32, 104, 101, 105, 103, 104, 116, 61, 34, 52, 48, 46, 53, 50, 51, 34, 62, 60, 47, 114, 101, 99, 116, 62, 60, 47, 103, 62, 60, 47, 115, 118, 103, 62, 60, 47, 100, 105, 118, 62, 60, 100, 105, 118, 32, 99, 108, 97, 115, 115, 61, 34, 116, 111, 97, 115, 116, 95, 95, 99, 111, 110, 116, 101, 110, 116, 34, 62, 60, 112, 32, 99, 108, 97, 115, 115, 61, 34, 116, 111, 97, 115, 116, 95, 95, 116, 121, 112, 101, 34, 62, 83, 117, 99, 99, 101, 115, 115, 60, 112, 32, 99, 108, 97, 115, 115, 61, 34, 116, 111, 97, 115, 116, 95, 95, 109, 101, 115, 115, 97, 103, 101, 34, 62, 65, 110, 121, 111, 110, 101, 32, 119, 105, 116, 104, 32, 97, 99, 99, 101, 115, 115, 32, 99, 97, 110, 32, 118, 105, 101, 119, 32, 121, 111, 117, 114, 32, 105, 110, 118, 105, 116, 101, 100, 32, 118, 105, 115, 105, 116, 111, 114, 115, 46, 60, 47, 100, 105, 118, 62, 60, 100, 105, 118, 32, 99, 108, 97, 115, 115, 61, 34, 116, 111, 97, 115, 116, 95, 95, 99, 108, 111, 115, 101, 34, 62, 60, 115, 118, 103, 32, 118, 101, 114, 115, 105, 111, 110, 61, 34, 49, 46, 49, 34, 32, 120, 109, 108, 110, 115, 61, 34, 104, 116, 116, 112, 58, 47, 47, 119, 119, 119, 46, 119, 51, 46, 111, 114, 103, 47, 50, 48, 48, 48, 47, 115, 118, 103, 34, 32, 118, 105, 101, 119, 66, 111, 120, 61, 34, 48, 32, 48, 32, 49, 53, 46, 54, 52, 50, 32, 49, 53, 46, 54, 52, 50, 34, 32, 120, 109, 108, 110, 115, 58, 120, 108, 105, 110, 107, 61, 34, 104, 116, 116, 112, 58, 47, 47, 119, 119, 119, 46, 119, 51, 46, 111, 114, 103, 47, 49, 57, 57, 57, 47, 120, 108, 105, 110, 107, 34, 32, 101, 110, 97, 98, 108, 101, 45, 98, 97, 99, 107, 103, 114, 111, 117, 110, 100, 61, 34, 110, 101, 119, 32, 48, 32, 48, 32, 49, 53, 46, 54, 52, 50, 32, 49, 53, 46, 54, 52, 50, 34, 62, 60, 112, 97, 116, 104, 32, 102, 105, 108, 108, 45, 114, 117, 108, 101, 61, 34, 101, 118, 101, 110, 111, 100, 100, 34, 32, 100, 61, 34, 77, 56, 46, 56, 56, 50, 44, 55, 46, 56, 50, 49, 108, 54, 46, 53, 52, 49, 45, 54, 46, 53, 52, 49, 99, 48, 46, 50, 57, 51, 45, 48, 46, 50, 57, 51, 44, 48, 46, 50, 57, 51, 45, 48, 46, 55, 54, 56, 44, 48, 45, 49, 46, 48, 54, 49, 32, 32, 99, 45, 48, 46, 50, 57, 51, 45, 48, 46, 50, 57, 51, 45, 48, 46, 55, 54, 56, 45, 48, 46, 50, 57, 51, 45, 49, 46, 48, 54, 49, 44, 48, 76, 55, 46, 56, 50, 49, 44, 54, 46, 55, 54, 76, 49, 46, 50, 56, 44, 48, 46, 50, 50, 99, 45, 48, 46, 50, 57, 51, 45, 48, 46, 50, 57, 51, 45, 48, 46, 55, 54, 56, 45, 48, 46, 50, 57, 51, 45, 49, 46, 48, 54, 49, 44, 48, 99, 45, 48, 46, 50, 57, 51, 44, 48, 46, 50, 57, 51, 45, 48, 46, 50, 57, 51, 44, 48, 46, 55, 54, 56, 44, 48, 44, 49, 46, 48, 54, 49, 32, 32, 108, 54, 46, 53, 52, 49, 44, 54, 46, 53, 52, 49, 76, 48, 46, 50, 50, 44, 49, 52, 46, 51, 54, 50, 99, 45, 48, 46, 50, 57, 51, 44, 48, 46, 50, 57, 51, 45, 48, 46, 50, 57, 51, 44, 48, 46, 55, 54, 56, 44, 48, 44, 49, 46, 48, 54, 49, 99, 48, 46, 49, 52, 55, 44, 48, 46, 49, 52, 54, 44, 48, 46, 51, 51, 56, 44, 48, 46, 50, 50, 44, 48, 46, 53, 51, 44, 48, 46, 50, 50, 115, 48, 46, 51, 56, 52, 45, 48, 46, 48, 55, 51, 44, 48, 46, 53, 51, 45, 48, 46, 50, 50, 108, 54, 46, 53, 52, 49, 45, 54, 46, 53, 52, 49, 32, 32, 108, 54, 46, 53, 52, 49, 44, 54, 46, 53, 52, 49, 99, 48, 46, 49, 52, 55, 44, 48, 46, 49, 52, 54, 44, 48, 46, 51, 51, 56, 44, 48, 46, 50, 50, 44, 48, 46, 53, 51, 44, 48, 46, 50, 50, 99, 48, 46, 49, 57, 50, 44, 48, 44, 48, 46, 51, 56, 52, 45, 48, 46, 48, 55, 51, 44, 48, 46, 53, 51, 45, 48, 46, 50, 50, 99, 48, 46, 50, 57, 51, 45, 48, 46, 50, 57, 51, 44, 48, 46, 50, 57, 51, 45, 48, 46, 55, 54, 56, 44, 48, 45, 49, 46, 48, 54, 49, 76, 56, 46, 56, 56, 50, 44, 55, 46, 56, 50, 49, 122, 34, 62, 60, 47, 112, 97, 116, 104, 62, 60, 47, 115, 118, 103, 62, 60, 47, 100, 105, 118, 62, 60, 47, 100, 105, 118, 62, 60, 47, 100, 105, 118, 62, 60, 47, 100, 105, 118, 62, 60, 47, 100, 105, 118, 62, 60, 115, 99, 114, 105, 112, 116, 32, 115, 114, 99, 61, 34, 97, 112, 112, 46, 106, 115, 34, 62, 60, 47, 115, 99, 114, 105, 112, 116, 62, 60, 115, 99, 114, 105, 112, 116, 32, 115, 114, 99, 61, 34, 116, 111, 97, 115, 116, 46, 106, 115, 34, 62, 60, 47, 115, 99, 114, 105, 112, 116, 62, 60, 115, 99, 114, 105, 112, 116, 32, 115, 114, 99, 61, 34, 101, 100, 105, 116, 111, 114, 46, 106, 115, 34, 62, 60, 47, 115, 99, 114, 105, 112, 116, 62},
    };


    private final String HTTP_CONTENT_LENGTH = "Content-Length";
    private final ExecutorService mExecutorService;
    private final Hashtable<String, String> mMimeTypes = getMimeTypeTable();
    private final ServerSocket mServerSocket;
    private final String mStaticDirectory;
    private final String mURL;
    private final String mUploadDirectory;
    private byte[] mBytesTemplate;
    private int mPort;
    private Thread mThread;


    private SimpleServer(Builder builder) throws IOException {

        mPort = builder.mPort;
        mStaticDirectory = builder.mStaticDirectory;
        mUploadDirectory = builder.mUploadDirectory;
        InetAddress address = InetAddress.getByName(builder.mHost);
        byte[] bytes = address.getAddress();

        mServerSocket = new ServerSocket();//mPort, 0, InetAddress.getByAddress(bytes)
        mServerSocket.setSoTimeout(MILLIS_PER_SECOND * 20);
        mServerSocket.setReuseAddress(true);
        while (!available(mPort)) {
            mPort++;
        }
        mServerSocket.bind(new InetSocketAddress(InetAddress.getByAddress(bytes), mPort));

        mPort = mServerSocket.getLocalPort();
        mURL = "http://" + mServerSocket.getInetAddress().getHostAddress() + ":" + mPort;

        // Log.d(TAG, "[SimpleServer] ---> " + mURL);
        mExecutorService = Executors.newFixedThreadPool(4);


        startServer();
    }


    private void file(Socket socket, String fileName) {
        InputStream is = null;
        try {
            OutputStream os = socket.getOutputStream();
            List<String> headers = new ArrayList<>();
            String extension = substringAfterLast(fileName, '.');
            headers.add(HTTP_CONTENT_TYPE);
            headers.add(mMimeTypes.get(extension));//  "; charset=UTF-8"

            if (extension != null) {
                switch (extension.toLowerCase()) {
                    case "html":

                        headers.add(HTTP_CACHE_CONTROL);
                        headers.add("no-cache");
                        break;

//                    case ".js":
//                    case ".css": {
//                        headers.add(HTTP_CACHE_CONTROL);
//                        headers.add(HEADER_VALUE_NO_CACHE);
//                        break;
//                    }
                    case "js":
                    case "css":
                    case "png":

                        headers.add(HTTP_CACHE_CONTROL);

                        headers.add("public, max-age=31536000, stale-while-revalidate=2592000");
                        break;
                }
            }
            if (extension != null && extension.equals("png")) {
                File bitmap = null;
                if (bitmap == null) {
                    notFound(socket);
                    return;
                }
                is = new FileInputStream(bitmap);
                headers.add(HTTP_DATE);
                headers.add(new SimpleDateFormat(DATE_FORMAT_GMT, Locale.US).format(bitmap.lastModified()));
                headers.add(HTTP_CONTENT_LENGTH);
                headers.add(Long.toString(bitmap.length()));

            } else {
                try {
                    is = AndroidContext.instance().get().getAssets().open("server/" + fileName);

                } catch (Exception e) {
                    notFound(socket);
                    return;
                }

                headers.add(HTTP_DATE);
                headers.add(new SimpleDateFormat(DATE_FORMAT_GMT, Locale.US).format(new Date()));

            }


            writeHeaders(socket, STATUS_CODE_OK, headers);

            writeInputStream(socket, is, 0L);
            os.flush();
        } catch (Exception e) {
            e(e);
            send(socket, 500);
        } finally {
            closeQuietly(is);
            closeQuietly(socket);
        }
    }


    private List<String> generateGenericHeader(String mimeType, String cache) {
        List<String> headers = new ArrayList<>();

        headers.add(HTTP_CONTENT_TYPE);
        headers.add(mimeType);//  "; charset=UTF-8"
        headers.add(HTTP_CACHE_CONTROL);
        headers.add(cache);
        headers.add(HTTP_DATE);
        headers.add(new SimpleDateFormat(DATE_FORMAT_GMT, Locale.US).format(new Date()));
        return headers;
    }


    private FormHeader getFormHeader(byte[] data, int offset) {
        int index = lookup(data, BYTES_LINE_FEED, offset);
        if (index == -1) return null;
        FormHeader formHeader = new FormHeader();
        String contentDisposition = Utils.getString(Arrays.copyOfRange(data, offset, index));
        contentDisposition = substringAfter(contentDisposition, "filename=");
        contentDisposition = trim(contentDisposition, new char[]{'"'});
        formHeader.fileName = contentDisposition;
        index = index + BYTES_LINE_FEED.length;

        index = lookup(data, BYTES_DOUBLE_LINE_FEED, index);
        if (index == -1) return null;

        formHeader.start = index + BYTES_DOUBLE_LINE_FEED.length;

        return formHeader;
    }

    public String getURL() {
        return mURL;
    }

    public static boolean available(int port) {
//        if (port < MIN_PORT_NUMBER || port > MAX_PORT_NUMBER) {
//            throw new IllegalArgumentException("Invalid start port: " + port);
//        }

        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException e) {
        } finally {
            if (ds != null) {
                ds.close();
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    /* should not be thrown */
                }
            }
        }

        return false;
    }

    private void jsonGet(Socket socket, String url) {
        // Log.d(TAG, "[jsonGet] ---> ");
        try {


            long hash = Utils.safeParseLong(Utils.substringAfterLast(url, '/'));
            if (hash == -1) {
                send(socket, STATUS_CODE_BAD_REQUEST);
                return;
            }
            List<String> headers = generateGenericHeader("application/json; charset=utf-8", HEADER_VALUE_NO_CACHE);
            writeHeaders(socket, STATUS_CODE_OK, headers);
            // Log.d(TAG, "[jsonGet] ---> " + hash);
            Note note = DatabaseHelper.getInstance(AndroidContext.instance().get()).fetchNote(hash);
            if (note == null) {
                notFound(socket);
                return;
            }
            Gson gson = new Gson();

            socket.getOutputStream().write(Utils.getBytes(gson.toJson(note, Note.class)));


        } catch (Exception e) {

        } finally {
            closeQuietly(socket);
        }
    }

    private void jsonMarkdown(Socket socket, String url) {
        // Log.d(TAG, "[jsonGet] ---> ");
        try {

            long hash = Utils.safeParseLong(Utils.substringAfterLast(url, '/'));
            if (hash == -1) {
                send(socket, STATUS_CODE_BAD_REQUEST);
                return;
            }

            Note note = DatabaseHelper.getInstance(AndroidContext.instance().get()).fetchNote(hash);
            if (note == null) {
                notFound(socket);
                return;
            }

            List<String> headers = generateGenericHeader("application/octet-stream", "no-cache");
            writeHeaders(socket, STATUS_CODE_OK, headers);

            socket.getOutputStream().write(Utils.getBytes(note.Content));


        } catch (Exception e) {

        } finally {
            closeQuietly(socket);
        }
    }

    private void jsonUpdate(Socket socket, InputStream is, byte[] remaining) {

        try {
            byte[][] header = sliceHeader(is, remaining);
            List<String> headers = parseHeaders(header[0]);


            long length = 0L;

            for (int i = 0; i < headers.size(); i += 2) {
                // Log.d(TAG, "[jsonUpdate] ---> " + headers.get(i) + ": " + headers.get(i + 1));
                if (headers.get(i).equals(HTTP_CONTENT_LENGTH)) {

                    length = Long.parseLong(headers.get(i + 1));
                }
            }

            // Log.d(TAG, "[jsonUpdate] ---> " + header[1].length + " " + length + " " + Utils.getString(header[1]));
            Gson gson = new Gson();
            Note note = null;
            if (header[1] != null && header[1].length == length) {
                note = gson.fromJson(Utils.getString(header[1]), Note.class);

            } else {
                byte[] bytes = header[1] == null ? new byte[0] : header[1];

                int bufferSize = (int) (length - bytes.length);
                byte[] buffer = new byte[bufferSize];

                is.read(buffer);
                note = gson.fromJson(Utils.getString(addAll(bytes, buffer)), Note.class);

//                while ((len = is.read(buffer, 0, bufferSize)) != -1) {
//                    bytes = addAll(bytes, buffer);
//                    Log.d(TAG, "[jsonUpdate] ---> " + Utils.getString(buffer));
//                    break;
//                }

            }

            if (note.ID > -1)
                DatabaseHelper.getInstance(AndroidContext.instance().get()).update(note);
            else
                DatabaseHelper.getInstance(AndroidContext.instance().get()).insert(note);

            writeHeaders(socket, STATUS_CODE_OK,generateGenericHeader("application/json","no-cache"));

            String js=gson.toJson(note);
            socket.getOutputStream().write(gson.toJson(note).getBytes(UTF_8));


        } catch (Exception e) {
            Log.e(TAG, "[jsonUpdate] ---> ", e);
        } finally {
            closeQuietly(socket);
        }
    }

    private void markdown(Socket socket) {
        List<Note> titles = DatabaseHelper.getInstance(AndroidContext.instance().get()).fetchTitles();
        StringBuilder sb = new StringBuilder();
        for (Note note : titles) {
            sb.append("<li class=\"nav-tree__item\"><a class=\"nav-tree__link\" href=\"#" + note.ID + "\">"
                    + Html.escapeHtml(note.Title) + "</a></li>");
        }
        List<String> headers = generateGenericHeader("text/html", "no-cache");

        try {
            writeHeaders(socket, STATUS_CODE_OK, headers);
            socket.getOutputStream().write(getmBytesMarkdown[0]);
            socket.getOutputStream().write(sb.toString().getBytes(Charset.forName(UTF_8)));
            socket.getOutputStream().write(getmBytesMarkdown[1]);

        } catch (IOException e) {
            // Log.e(TAG, "[markdown] ---> ", e);
        } finally {
            closeQuietly(socket);
        }

    }


    private Position nextLine(byte[] data, byte[] boundary, int offset) {
        int length = data.length;
        int position = -1;
        int len = boundary.length;


        for (int i = offset; i < length; i++) {
            if (data[i] == 10) {
                position = i + 1;

                Position p = new Position();
                p.position = position;

                int lineLength = i - offset - 1;

                if (lineLength == len || lineLength == len + 2) {
                    for (int j = 0; j < len; j++) {
                        if (data[offset + j] != boundary[j]) {

                            p.isBoundaryLine = false;
                            p.isLastLine = false;
                            return p;
                        }
                    }

                    if (lineLength == len + 2 && data[offset + lineLength - 1] == 45 && data[offset + lineLength - 2] == 45) {
                        p.isBoundaryLine = true;
                        p.isLastLine = true;
                        return p;
                    } else {
                        p.isBoundaryLine = true;
                        p.isLastLine = false;
                        return p;
                    }
                }

                return p;
            }
        }
        return null;
    }

    private void notFound(Socket socket) {
        send(socket, STATUS_CODE_NOT_FOUND);
    }


    private void processRequest(Socket socket) {

        try {
            InputStream is = new BufferedInputStream(socket.getInputStream());
            byte[][] status = sliceURL(is);
            if (status == null || status.length < 1) {
                notFound(socket);
                return;
            }

            String[] u = parseURL(status[0]);
            // Log.d(TAG, "[processRequest] ---> " + u[1]);
            if (u[1].length() == 0/* / */) {
                markdown(socket);
                return;
            } else if (u[1].lastIndexOf('.') != -1) {

                file(socket, URLDecoder.decode(u[1], UTF_8));
                return;

            } else if (u[1].equals("upload")) {
                processUploadFile(socket, is, status[1]);
                return;


            } else if (u[1].startsWith("api/get/")) {
                jsonGet(socket, u[1]);
                return;

            } else if (u[1].startsWith("api/update")) {

                jsonUpdate(socket, is, status[1]);
                return;

            } else if (u[1].startsWith("api/download/")) {
                jsonMarkdown(socket, u[1]);
                return;
            } else {
                notFound(socket);
            }


            //d(toString(status[1]));

            //parseHeader(socket);
        } catch (Exception e) {
            e(e);


        }

        closeQuietly(socket);
    }

    private void processUploadFile(Socket socket, InputStream is, byte[] bytes) throws IOException {

        byte[][] header = sliceHeader(is, bytes);
        List<String> headers = parseHeaders(header[0]);

        String boundary = null;
        for (int i = 0; i < headers.size(); i += 2) {
            String key = headers.get(i);
            if (key.equalsIgnoreCase(HTTP_CONTENT_TYPE)) {
                if (headers.get(i + 1).startsWith("multipart/form-data;")) {
                    boundary = substringAfter(headers.get(i + 1), "boundary=");

                } else {

                    send(socket, STATUS_CODE_BAD_REQUEST);
                    return;
                }
            } else if (key.equalsIgnoreCase(HTTP_CONTENT_LENGTH)) {

            }
        }
        if (boundary == null) {

            send(socket, STATUS_CODE_BAD_REQUEST);
            return;
        } else {
            // Add two dashes in start according to the convention
            boundary = "--" + boundary;
        }

        if (header[1] == null) {

            send(socket, STATUS_CODE_BAD_REQUEST);
            return;
        }

        // handleLargeFile(socket, is, boundary, header[1]);

        byte[] boundaryBytes = boundary.getBytes(UTF_8);
        byte[] data = header[1];
        int offset = 0;
        OutputStream os = null;
        boolean exit = false;
        while (true) {

            if (exit) {


                // do not forget invoke outputStream.flush
                // otherwise maybe loss some data
                closeQuietly(os);
                send(socket, STATUS_CODE_OK);
                return;
            }
            Position position = nextLine(data, boundaryBytes, offset);
            if (position == null) {
                os.write(data, offset, data.length - offset);
                os.flush();

                byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                int len = is.read(buffer, 0, DEFAULT_BUFFER_SIZE);

                data = buffer;
                offset = 0;


            } else if (position.isBoundaryLine) {

                if (position.isLastLine) {


                    exit = true;
                } else {

                    FormHeader formHeader = getFormHeader(data, position.position);
                    if (formHeader == null) {
                        send(socket, STATUS_CODE_BAD_REQUEST);
                        return;
                    }

                    offset = formHeader.start;

                    os = new BufferedOutputStream(new FileOutputStream(new File(mUploadDirectory, formHeader.fileName)));

                }

            } else {
                if (data.length - position.position > boundaryBytes.length && Utils.byteArrayHasPrefix(boundaryBytes, data, position.position)) {

                    os.write(data, offset, position.position - offset - 2);
                    os.flush();

                } else {
                    os.write(data, offset, position.position - offset);
                    os.flush();
                }
                offset = position.position;
            }
        }


//        closeQuietly(os);
//        send(socket, STATUS_CODE_OK);
    }


    private String responseHeader(int statusCode, List<String> headers) {

        StringBuilder sb = new StringBuilder();

        sb.append("HTTP/1.1 ")
                .append(statusCode)
                .append(' ')
                .append(getDefaultReason(statusCode))
                .append("\r\n");

        if (headers != null) {
            int len = headers.size();
            if (len != 0) {
                assert (len % 2 == 0);
                len = len - 1;
                for (int i = 0; i < len; i++) {
                    sb.append(headers.get(i)).append(": ").append(headers.get(++i)).append("\r\n");
                }
            }
        }

        sb.append("\r\n");
        return sb.toString();
    }

    private void send(Socket socket, int statusCode) {
        send(socket, statusCode, null, new byte[0]);
    }

    private void send(Socket socket, int statusCode, List<String> headers, byte[]... bytes) {

        try {
            OutputStream os = socket.getOutputStream();

            byte[] header = responseHeader(statusCode, headers).getBytes(UTF_8);
            os.write(header, 0, header.length);
            if (bytes != null) {
                for (byte[] aByte : bytes) {
                    os.write(aByte, 0, aByte.length);
                }
            }
            os.flush();
        } catch (Exception e) {
            e(e);
        } finally {
            closeQuietly(socket);
        }
    }


    private void startServer() {
        d("Start Server");
        mThread = new Thread(() -> {
            while (true) {

                try {
                    Socket socket = mServerSocket.accept();
                    mExecutorService.submit(() -> processRequest(socket));

                } catch (SocketTimeoutException ignore) {
                } catch (IOException e) {
                    e(e);
                }
            }
        });
        mThread.start();
    }


    private void writeHeaders(Socket socket, int statusCode, List<String> headers) throws IOException {
        byte[] header = responseHeader(statusCode, headers).getBytes(UTF_8);
        socket.getOutputStream().write(header);
    }

    private void writeInputStream(Socket socket, InputStream is, long skip) throws IOException {
        if (skip > 0) {
            is.skip(skip);
        }
        OutputStream os = socket.getOutputStream();

        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int len;
        while ((len = is.read(buffer, 0, DEFAULT_BUFFER_SIZE)) != -1) {
            os.write(buffer, 0, len);
        }
    }


    private static void d(String message) {
        // Log.e(TAG, message);
    }

    private static void e(Exception e) {

    }

    private static void e(String e) {
        // Log.e(TAG, e);
    }


    public static class Builder {
        private final String mHost;
        private final int mPort;
        private String mStaticDirectory;
        private String mUploadDirectory;
        private String[] mVideoDirectory;

        public Builder(String host, int port) {

            mHost = host;
            mPort = port;
        }

        public SimpleServer build() {
            try {
                return new SimpleServer(this);
            } catch (IOException e) {
                // java.net.BindException: bind failed: EADDRINUSE (Address already in use)
                return null;
            }
        }

        public Builder setStaticDirectory(String staticDirectory) {
            mStaticDirectory = staticDirectory;
            return this;
        }

        Builder setUploadDirectory(String uploadDirectory) {
            mUploadDirectory = uploadDirectory;
            return this;
        }

        public Builder setVideoDirectory(String[] videoDirectory) {
            mVideoDirectory = videoDirectory;
            return this;
        }
    }

    private static class FormHeader {
        String fileName;
        int start;
    }

    private static class Position {
        boolean isBoundaryLine;
        boolean isLastLine;
        int position;
    }

    private static class FormData {
        byte[] buffer;
        boolean hasNext;
    }
}