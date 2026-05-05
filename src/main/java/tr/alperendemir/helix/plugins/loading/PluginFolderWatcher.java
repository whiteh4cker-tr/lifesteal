package tr.alperendemir.helix.plugins.loading;

import tr.alperendemir.helix.api.helper.FileHelper;
import tr.alperendemir.helix.api.logging.HelixLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchService;

public class PluginFolderWatcher extends Thread {

    private final Path pluginFolder;
    private final Runnable fileChangedCallback;
    private boolean running;

    public PluginFolderWatcher(File pluginFolder, Runnable fileChangedCallback) {
        this.pluginFolder = pluginFolder.getAbsoluteFile().toPath();
        if(!Files.exists(this.pluginFolder)) {
            try {
                Files.createDirectories(this.pluginFolder);
            } catch (IOException e) {
                HelixLogger.reportError(e);
                throw new RuntimeException(e);
            }
        }


        this.fileChangedCallback = fileChangedCallback;
    }

    public void shutdown() {
        this.running = false;
        this.interrupt();
    }

    @Override
    public void run() {
        try {
            this.running = true;
            this.runWatch();
        } catch (InterruptedException e) {
            this.interrupt();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void runWatch() throws InterruptedException, IOException {
        try (var watcher = FileSystems.getDefault().newWatchService()) {
            this.pluginFolder.register(
                    watcher,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY
            );
            this.runWatchLoop(watcher);
        }
    }

    private void runWatchLoop(WatchService watcher) throws InterruptedException {
        while (this.running) {
            var key = watcher.take();

            for (var event : key.pollEvents()) {
                var kind = event.kind();
                if (kind == StandardWatchEventKinds.OVERFLOW) continue;

                var ev = (WatchEvent<Path>) event;
                var fileName = ev.context();
                var child = this.pluginFolder.resolve(fileName);

                if (!FileHelper.isJar(child.toFile())) {
                    continue;
                }

                this.fileChangedCallback.run();
            }

            if (!key.reset()) break;
        }
    }
}
