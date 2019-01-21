package euphoria.psycho.notepad;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class C {
    // https://github.com/JetBrains/intellij-community/blob/master/platform/util-rt/src/com/intellij/openapi/util/text/StringUtilRt.java

    public static final int THREAD_LOCAL_BUFFER_LENGTH = 1024 * 20;
    protected static final ThreadLocal<byte[]> BUFFER = new ThreadLocal<byte[]>() {
        @Override
        protected byte[] initialValue() {
            return new byte[THREAD_LOCAL_BUFFER_LENGTH];
        }
    };
    private static final boolean USE_FILE_CHANNELS = "true".equalsIgnoreCase(System.getProperty("idea.fs.useChannels"));

    public static boolean charsEqualIgnoreCase(char a, char b) {
        return charsMatch(a, b, true);
    }

    public static boolean charsMatch(char c1, char c2, boolean ignoreCase) {
        return compare(c1, c2, ignoreCase) == 0;
    }

    public static int compare(char c1, char c2, boolean ignoreCase) {
        // duplicating String.equalsIgnoreCase logic
        int d = c1 - c2;
        if (d == 0 || !ignoreCase) {
            return d;
        }
        // If characters don't match but case may be ignored,
        // try converting both characters to uppercase.
        // If the results match, then the comparison scan should
        // continue.
        char u1 = toUpperCase(c1);
        char u2 = toUpperCase(c2);
        d = u1 - u2;
        if (d != 0) {
            // Unfortunately, conversion to uppercase does not work properly
            // for the Georgian alphabet, which has strange rules about case
            // conversion.  So we need to make one last check before
            // exiting.
            d = toLowerCase(u1) - toLowerCase(u2);
        }
        return d;
    }

    public static void copy( File fromFile,  File toFile) throws IOException {


        FileOutputStream fos = new FileOutputStream(toFile);
        try {
            FileInputStream fis = new FileInputStream(fromFile);
            try {
                copy(fis, fos);
            } finally {
                fis.close();
            }
        } finally {
            fos.close();
        }

        long timeStamp = fromFile.lastModified();
        if (timeStamp < 0) {
        } else if (!toFile.setLastModified(timeStamp)) {
        }
    }

    public static void copy( InputStream inputStream,  OutputStream outputStream) throws IOException {
        if (USE_FILE_CHANNELS && inputStream instanceof FileInputStream && outputStream instanceof FileOutputStream) {
            final FileChannel fromChannel = ((FileInputStream) inputStream).getChannel();
            try {
                final FileChannel toChannel = ((FileOutputStream) outputStream).getChannel();
                try {
                    fromChannel.transferTo(0, Long.MAX_VALUE, toChannel);
                } finally {
                    toChannel.close();
                }
            } finally {
                fromChannel.close();
            }
        } else {
            final byte[] buffer = getThreadLocalBuffer();
            while (true) {
                int read = inputStream.read(buffer);
                if (read < 0) break;
                outputStream.write(buffer, 0, read);
            }
        }
    }

    public static boolean createDirectory(File path) {
        return path.isDirectory() || path.mkdirs();
    }

    //    private static boolean deleteRecursively( File file) {
//        File[] files = file.listFiles();
//        if (files != null) {
//            for (File child : files) {
//                if (!deleteRecursively(child)) return false;
//            }
//        }
//
//        return deleteFile(file);
//    }
    public static boolean createIfNotExists(File file) {
        if (file.exists()) return true;
        try {
            if (!createParentDirs(file)) return false;

            OutputStream s = new FileOutputStream(file);
            s.close();
            return true;
        } catch (IOException e) {

            return false;
        }
    }

    public static boolean createParentDirs(File file) {
        File parentPath = file.getParentFile();
        return parentPath == null || createDirectory(parentPath);
    }

    public static boolean endsWithIgnoreCase(CharSequence text, CharSequence suffix) {
        int l1 = text.length();
        int l2 = suffix.length();
        if (l1 < l2) return false;

        for (int i = l1 - 1; i >= l1 - l2; i--) {
            if (!charsEqualIgnoreCase(text.charAt(i), suffix.charAt(i + l2 - l1))) {
                return false;
            }
        }

        return true;
    }

    public static boolean extensionEquals(String filePath, String extension) {
        int extLen = extension.length();
        if (extLen == 0) {
            int lastSlash = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));
            return filePath.indexOf('.', lastSlash + 1) == -1;
        }
        int extStart = filePath.length() - extLen;
        return extStart >= 1 && filePath.charAt(extStart - 1) == '.'
                && filePath.regionMatches(true, extStart, extension, 0, extLen);
    }

    public static String formatFileSize(long fileSize, String unitSeparator) {
        if (fileSize < 0) throw new IllegalArgumentException("Invalid value: " + fileSize);
        if (fileSize == 0) return '0' + unitSeparator + 'B';
        int rank = (int) ((Math.log10(fileSize) + 0.0000021714778384307465) / 3);  // (3 - Math.log10(999.995))
        double value = fileSize / Math.pow(1000, rank);
        String[] units = {"B", "kB", "MB", "GB", "TB", "PB", "EB"};
        return new DecimalFormat("0.##").format(value) + unitSeparator + units[rank];
    }

    public static String getExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index < 0) return "";
        return fileName.substring(index + 1);
    }

    public static CharSequence getExtension(CharSequence fileName) {
        return getExtension(fileName, "");
    }

    public static CharSequence getExtension(CharSequence fileName, String defaultValue) {
        int index = lastIndexOf(fileName, '.', 0, fileName.length());
        if (index < 0) {
            return defaultValue;
        }
        return fileName.subSequence(index + 1, fileName.length());
    }

    public static File getParentFile(File file) {
        int skipCount = 0;
        File parentFile = file;
        while (true) {
            parentFile = parentFile.getParentFile();
            if (parentFile == null) {
                return null;
            }
            if (".".equals(parentFile.getName())) {
                continue;
            }
            if ("..".equals(parentFile.getName())) {
                skipCount++;
                continue;
            }
            if (skipCount > 0) {
                skipCount--;
                continue;
            }
            return parentFile;
        }
    }

    public static byte[] getThreadLocalBuffer() {
        return BUFFER.get();
    }

    public static int indexOfIgnoreCase(String where, String what, int fromIndex) {
        return indexOfIgnoreCase((CharSequence) where, what, fromIndex);
    }

    public static int indexOfIgnoreCase(CharSequence where, CharSequence what, int fromIndex) {
        int targetCount = what.length();
        int sourceCount = where.length();

        if (fromIndex >= sourceCount) {
            return targetCount == 0 ? sourceCount : -1;
        }

        if (fromIndex < 0) {
            fromIndex = 0;
        }

        if (targetCount == 0) {
            return fromIndex;
        }

        char first = what.charAt(0);
        int max = sourceCount - targetCount;

        for (int i = fromIndex; i <= max; i++) {
            /* Look for first character. */
            if (!charsEqualIgnoreCase(where.charAt(i), first)) {
                //noinspection StatementWithEmptyBody,AssignmentToForLoopParameter
                while (++i <= max && !charsEqualIgnoreCase(where.charAt(i), first)) ;
            }

            /* Found first character, now look at the rest of v2 */
            if (i <= max) {
                int j = i + 1;
                int end = j + targetCount - 1;
                //noinspection StatementWithEmptyBody
                for (int k = 1; j < end && charsEqualIgnoreCase(where.charAt(j), what.charAt(k)); j++, k++)
                    ;

                if (j == end) {
                    /* Found whole string. */
                    return i;
                }
            }
        }

        return -1;
    }

    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean isEmptyOrSpaces(CharSequence s) {
        if (isEmpty(s)) {
            return true;
        }
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) > ' ') {
                return false;
            }
        }
        return true;
    }

    public static boolean isNullOrWhiteSpace(CharSequence text) {
        if (text == null || text.length() == 0) return true;
        for (int i = 0, j = text.length(); i < j; i++) {
            if (!Character.isWhitespace(text.charAt(i))) return false;
        }
        return true;
    }

    public static int lastIndexOf(CharSequence s, char c, int start, int end) {
        start = Math.max(start, 0);
        for (int i = Math.min(end, s.length()) - 1; i >= start; i--) {
            if (s.charAt(i) == c) return i;
        }
        return -1;
    }

    public static byte[] loadBytes(InputStream stream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        copy(stream, buffer);
        return buffer.toByteArray();
    }

    public static byte[] loadBytes(InputStream stream, int length) throws IOException {
        if (length == 0) {
            return new byte[0];
        }
        byte[] bytes = new byte[length];
        int count = 0;
        while (count < length) {
            int n = stream.read(bytes, count, length - count);
            if (n <= 0) break;
            count += n;
        }
        return bytes;
    }

    public static List<String> loadLines(BufferedReader reader) throws IOException {
        List<String> lines = new ArrayList<String>();
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }
        return lines;
    }

    public static char[] loadText(Reader reader, int length) throws IOException {
        char[] chars = new char[length];
        int count = 0;
        while (count < chars.length) {
            int n = reader.read(chars, count, chars.length - count);
            if (n <= 0) break;
            count += n;
        }
        if (count == chars.length) {
            return chars;
        } else {
            char[] newChars = new char[count];
            System.arraycopy(chars, 0, newChars, 0, count);
            return newChars;
        }
    }

    public static double parseDouble(String string, double defaultValue) {
        if (string != null) {
            try {
                return Double.parseDouble(string);
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }

    public static int parseInt(String string, int defaultValue) {
        if (string != null) {
            try {
                return Integer.parseInt(string);
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }

    public static long parseLong(String string, long defaultValue) {
        if (string != null) {
            try {
                return Long.parseLong(string);
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }

    public static String randomString() {
        StringBuilder sb = new StringBuilder();
        int max = 10;
        Random random = new Random();
        while (max-- > 0) {
            for (int i = 0; i < random.nextInt(20); i++) {
                sb.append(i);
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    public static String replace(final String text, final String oldS, final String newS, final boolean ignoreCase) {
        if (text.length() < oldS.length()) return text;

        StringBuilder newText = null;
        int i = 0;

        while (i < text.length()) {
            final int index = ignoreCase ? indexOfIgnoreCase(text, oldS, i) : text.indexOf(oldS, i);
            if (index < 0) {
                if (i == 0) {
                    return text;
                }

                newText.append(text, i, text.length());
                break;
            } else {
                if (newText == null) {
                    if (text.length() == oldS.length()) {
                        return newS;
                    }
                    newText = new StringBuilder(text.length() - i);
                }

                newText.append(text, i, index);
                newText.append(newS);
                i = index + oldS.length();
            }
        }
        return newText != null ? newText.toString() : "";
    }

    public static char toLowerCase(char a) {
        if (a < 'A' || a >= 'a' && a <= 'z') return a;
        if (a <= 'Z') return (char) (a + ('a' - 'A'));
        return Character.toLowerCase(a);
    }

    public static CharSequence toUpperCase(CharSequence s) {
        StringBuilder answer = null;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            char upCased = toUpperCase(c);
            if (answer == null && upCased != c) {
                answer = new StringBuilder(s.length());
                answer.append(s.subSequence(0, i));
            }
            if (answer != null) {
                answer.append(upCased);
            }
        }
        return answer == null ? s : answer;
    }

    public static char toUpperCase(char a) {
        if (a < 'a') return a;
        if (a <= 'z') return (char) (a + ('A' - 'a'));
        return Character.toUpperCase(a);
    }

    public static String trimEnd(String s) {
        int len = s.length();
        if (len == 0) return s;

        while (s.charAt(len - 1) <= ' ') {
            len--;
        }
        return len < s.length() ? s.substring(0, len) : s;
    }
}
