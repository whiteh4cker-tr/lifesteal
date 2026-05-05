package tr.alperendemir.helix.api.helper;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.ZipFile;

@SuppressWarnings("unused")
public final class FileHelper {

    private FileHelper() {

    }

    @Nullable
    public static String getType(File file) {
        try {
            return Files.probeContentType(file.toPath());
        } catch (IOException e) {
            return getExtension(file);
        }
    }

    public static boolean isJar(File file) {
        return isJar(getType(file));
    }

    public static boolean isJar(String type) {
        return "application/java-archive".equals(type) || "jar".equals(type);
    }

    public static long fileSize(File file) {
        try {
            return Files.size(file.toPath());
        } catch (IOException ignored) {
            return -1;
        }
    }

    public static long entrySize(File file, String entry) {
        try(var zip = new ZipFile(file)) {
            return zip.getEntry(entry).getSize();
        } catch (IOException e) {
            return -1;
        }
    }

    public static long compressedEntrySize(File file, String entry) {
        try(var zip = new ZipFile(file)) {
            return zip.getEntry(entry).getCompressedSize();
        } catch (IOException e) {
            return -1;
        }
    }

    public static String getExtension(File file) {
        var name = file.getName();

        var extensionDot = name.lastIndexOf('.');
        if (extensionDot == -1) return null;

        return name.substring(extensionDot + 1);
    }

    public static String getName(File file) {
        var name = file.getName();

        var extensionDot = name.lastIndexOf('.');
        if (extensionDot == -1) return name;

        return name.substring(0, extensionDot);
    }

}
