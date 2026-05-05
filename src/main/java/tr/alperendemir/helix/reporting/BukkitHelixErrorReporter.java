package tr.alperendemir.helix.reporting;

import tr.alperendemir.helix.api.reporting.HelixErrorReporter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;

public class BukkitHelixErrorReporter implements HelixErrorReporter {

    private static final DateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private final Deque<HelixErrorSession> sessions = new ArrayDeque<>();

    @Override
    public void beginSession() {
        this.sessions.push(new HelixErrorSession());
    }

    @Override
    public HelixErrorSession getSession() {
        return this.sessions.peek();
    }

    @Override
    public void endSession() {
        this.sessions.pop();
    }

    public void dumpErrors(File errorFolder) throws IOException {
        if (this.sessions.isEmpty() || this.getErrors().isEmpty()) return;

        if (!errorFolder.exists() && !errorFolder.mkdirs())
            throw new IOException("Could not create error folder: " + errorFolder.getAbsolutePath());

        File file;
        var count = 0;
        do {
            file = new File(errorFolder, FORMAT.format(new Date()) + (count == 0 ? "" : " (%d)".formatted(count)) + ".txt");
            count++;
        } while (file.exists());

        var spacing = "    ".getBytes(StandardCharsets.UTF_8);
        var nl = System.lineSeparator().getBytes(StandardCharsets.UTF_8);

        try(var os = new BufferedOutputStream(new FileOutputStream(file))) {
            for (var entry : this.getErrors().entrySet()) {
                var msg = entry.getKey();
                var severity = entry.getValue();

                os.write(severity.name().getBytes());
                os.write(spacing);
                os.write(msg.getBytes(StandardCharsets.UTF_8));
                os.write(nl);
            }
        }
    }
}
