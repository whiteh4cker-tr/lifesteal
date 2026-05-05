package tr.alperendemir.helix.config;

import tr.alperendemir.helix.api.Helix;
import tr.alperendemir.helix.api.config.ConfigComponent;
import tr.alperendemir.helix.api.config.ConfigEntry;
import tr.alperendemir.helix.api.config.Configuration;
import tr.alperendemir.helix.api.config.components.TreeConfigComponent;
import tr.alperendemir.helix.api.config.components.arrays.CompoundArrayConfigComponent;
import tr.alperendemir.helix.api.config.components.arrays.ParsedArrayConfigComponent;
import tr.alperendemir.helix.api.config.components.maps.ParsedMapConfigComponent;
import tr.alperendemir.helix.api.config.components.values.CompoundConfigComponent;
import tr.alperendemir.helix.api.config.components.values.ParsedValueConfigComponent;
import tr.alperendemir.helix.api.config.errors.ConfigError;
import tr.alperendemir.helix.api.logging.HelixLogger;
import tr.alperendemir.helix.api.logging.LoggerLevel;
import tr.alperendemir.helix.api.reporting.ErrorType;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public record SimpleConfiguration(File defaultFile, ConfigEntry entry) implements Configuration {

    private static final Yaml YAML = new Yaml();

    @Override
    public Configuration child(String key) {
        var child = this.entry.component(key);
        if (!(child instanceof ConfigEntry section)) {
            throw new IllegalArgumentException("Unexpected child type: " + child.getClass().getName());
        }

        return new SimpleConfiguration(this.defaultFile, section);
    }

    @Override
    public <C> ParsedValueConfigComponent<?, C> value(String key) {
        return this.entry.component(key);
    }

    @Override
    public <C> ParsedArrayConfigComponent<?, C> valueArray(String key) {
        return this.entry.component(key);
    }

    @Override
    public <T> CompoundConfigComponent<T> compound(String key) {
        return this.entry.component(key);
    }

    @Override
    public <T> CompoundArrayConfigComponent<T> compoundArray(String key) {
        return this.entry.component(key);
    }

    @Override
    public <T> ParsedMapConfigComponent<String, T> map(String key) {
        return this.entry.component(key);
    }

    @Override
    public void write(StringBuilder stringBuilder, int indent) {
        if (this.entry instanceof ConfigComponent component) {
            component.write(stringBuilder, indent);
        }
    }

    @Override
    public void write(OutputStream outputStream, int indent, boolean length) throws IOException {
        var sb = new StringBuilder();
        this.write(sb, indent);

        var bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        if (length) {
            var len = ByteBuffer.allocate(4).putInt(bytes.length).array();
            outputStream.write(len);
        }
        outputStream.write(bytes);
    }

    @Override
    public void write(Path path, int indent) throws IOException {
        Files.createDirectories(path.getParent());

        try (var out = Files.newOutputStream(path)) {
            this.write(out, indent);
        }
    }

    @Override
    public void write(File file, int indent) throws IOException {
        this.write(file.getAbsoluteFile().toPath(), indent);
    }

    @Override
    public void writeError(StringBuilder stringBuilder, ConfigError error, int indent) {
        if (this.entry instanceof ConfigComponent component) {
            component.writeError(stringBuilder, error, indent);
        }
    }

    @Override
    public void writeError(OutputStream outputStream, ConfigError error, int indent, boolean length) throws IOException {
        var sb = new StringBuilder();
        this.writeError(sb, error, indent);

        var bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        if (length) {
            var len = ByteBuffer.allocate(4).putInt(bytes.length).array();
            outputStream.write(len);
        }
        outputStream.write(bytes);
    }

    @Override
    public void writeError(Path path, ConfigError error, int indent) throws IOException {
        try (var out = Files.newOutputStream(path)) {
            this.writeError(out, error, indent);
        }
    }

    @Override
    public void writeError(File file, ConfigError error, int indent) throws IOException {
        this.writeError(file.getAbsoluteFile().toPath(), error, indent);
    }

    @Override
    public ConfigError read(Map<?, ?> data) {
        if (this.entry instanceof TreeConfigComponent component) {
            return component.read(data);
        }

        return null;
    }

    @Override
    public void save() {
        if (this.entry instanceof TreeConfigComponent component && component.parent() != null) {
            throw new IllegalStateException("Cannot save a config entry that is not root.");
        }

        try {
            this.write(this.defaultFile());
        } catch (IOException e) {
            HelixLogger.reportError(e);
        }
    }

    @Override
    public boolean load() {
        if (this.entry instanceof TreeConfigComponent component && component.parent() != null) {
            System.out.println(component.parent());
            throw new IllegalStateException("Cannot load a config entry that is not root.");
        }

        var parent = JavaPlugin.getProvidingPlugin(this.getClass())
                .getDataFolder()
                .getAbsoluteFile()
                .getParentFile()
                .getParentFile()
                .toPath();

        var relative = parent.relativize(this.defaultFile.getAbsoluteFile().toPath());

        try (var reader = new FileReader(this.defaultFile)) {
            Map<?, ?> parsed = YAML.load(reader);
            var errors = this.read(parsed);
            if (errors == null) {
                return true;
            }

            this.printErrors(errors, relative);
            return false;
        } catch (Exception exception) {
            this.printInvalidYaml(relative);
            HelixLogger.reportError(exception);
            return false;
        }
    }

    @Override
    public boolean loadOrCreate() {
        if (!this.defaultFile.isFile()) {
            this.save();
            return true;
        }

        return this.load();
    }

    private void printErrors(ConfigError error, Path relative) {
        var sb = new StringBuilder();
        this.writeError(sb, error);

        Helix.errors().reportError("Incorrect configuration <light:white>" + relative, ErrorType.ERROR);

        HelixLogger.error("<red>ERROR! <light:red>Multiple issues have been encountered while reloading!");
        HelixLogger.error("<light:red>Broken Config: <light:white>%s", relative);
        HelixLogger.error("<light:red>Below is a list of issues and possible fixes.");
        HelixLogger.error("<light:red>Any white-colored line is an explanation on the error and is not part of the actual file.");
        HelixLogger.error("<light:red>Any line starting with a '<green>+<light:red>' means that you need to add that line, excluding the '<green>+<light:red>' itself.");

        HelixLogger.println(LoggerLevel.ERROR);
        HelixLogger.println(LoggerLevel.ERROR);
        HelixLogger.println(LoggerLevel.ERROR);

        var lines = sb.toString().split("\n");
        for (var line : lines) {
            HelixLogger.error(line);
        }
    }

    private void printInvalidYaml(Path relative) {
        Helix.errors().reportError("Invalid yaml file <light:white>" + relative, ErrorType.ERROR);
        HelixLogger.error("<red>SEVERE ERROR! <light:red>Unable to load yaml file!");
        HelixLogger.error("<light:red>Path: <light:white>%s", relative);
        HelixLogger.error("<light:red>Please put your config through the following tool to find issues: <light:white>https://www.yamllint.com/");
        HelixLogger.error("<light:red>Below is the configuration that had the issue:");
        HelixLogger.println(LoggerLevel.ERROR);
        HelixLogger.println(LoggerLevel.ERROR);
        HelixLogger.println(LoggerLevel.ERROR);

        try {
            var lines = Files.readAllLines(this.defaultFile.toPath());
            for (var line : lines) {
                HelixLogger.error(line);
            }
        } catch (IOException ignored1) {
            HelixLogger.error("<red>!!! UNABLE TO READ CONTENTS OF CONFIG |||");
        }
    }
}
