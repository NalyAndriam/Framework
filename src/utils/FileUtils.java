package utils;

public class FileUtils {
    public static String getSimpleFileName(String fileName, String extension) {
        return fileName.substring(0, (fileName.length() - extension.length()) - 1);
    }

    private FileUtils() {
    }
}
