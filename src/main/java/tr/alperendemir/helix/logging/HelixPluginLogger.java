package tr.alperendemir.helix.logging;

import tr.alperendemir.helix.api.Helix;
import tr.alperendemir.helix.api.logging.LoggerLevel;
import tr.alperendemir.helix.api.logging.HelixLogger;
import tr.alperendemir.helix.api.reporting.ErrorType;
import org.bukkit.Bukkit;

import java.io.BufferedOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/**
 * Handles printing for the interactive shell, uses the default printing system instead of a logger to circumvent
 * the default logging prefix.
 * */
public final class HelixPluginLogger extends HelixLogger {

    private static final Object LOCK = new Object();

    private static final PrintStream OUTPUT;
    private static final PrintStream ERROR;

    private static boolean printingCaret;

    static {
        OUTPUT = newPrintStream(FileDescriptor.out);
        ERROR = newPrintStream(FileDescriptor.err);
    }

    @Override
    public void printCaret0(boolean print) {
        HelixPluginLogger.printingCaret = print;
    }

    @Override
    public boolean isPrintingCaret0() {
        return HelixPluginLogger.printingCaret;
    }

    @Override
    public void syncLog0(Runnable runnable) {
        synchronized (LOCK) {
            runnable.run();
        }
    }

    @Override
    public void down0(int lines) {
        OUTPUT.print("\033[" + lines + "B");
    }

    @Override
    public void up0(int lines) {
        OUTPUT.print("\033[" + lines + "A");
    }

    @Override
    public void left0(int columns) {
        OUTPUT.print("\033[" + columns + "D");
    }

    @Override
    public void right0(int columns) {
        OUTPUT.print("\033[" + columns + "C");
    }

    @Override
    public void clearLine0() {
        OUTPUT.print("\033[2K\r");
    }

    @Override
    public void print0(Object object, Object... args) {
        OUTPUT.print(LoggerColoring.replaceColoring(String.valueOf(object).formatted(args)));
    }

    @Override
    public void println0(Object object, Object... args) {
        OUTPUT.print((LoggerColoring.replaceColoring(String.valueOf(object).formatted(args))));
        println0();
    }

    @Override
    public void print0(LoggerLevel level, Object object, Object... args) {
        this.print0(processMessage(level, object), args);
    }

    @Override
    public void println0(LoggerLevel level, Object object, Object... args) {
        this.println0(processMessage(level, object), args);
    }

    @Override
    public void println0() {
        OUTPUT.println();
        if(isPrintingCaret()) {
            OUTPUT.print(">");
        }
    }

    @Override
    public void println0(LoggerLevel level) {
        OUTPUT.print(LoggerColoring.replaceColoring(level.getConsoleText()));
        println();
    }

    @Override
    public void eprint0(Object object, Object... args) {
        ERROR.print(LoggerColoring.replaceColoring(String.valueOf(object).formatted(args)));
    }

    @Override
    public void eprintln0(Object object, Object... args) {
        ERROR.print((LoggerColoring.replaceColoring(String.valueOf(object).formatted(args))));
        eprintln0();
    }

    @Override
    public void eprint0(LoggerLevel level, Object object, Object... args) {
        eprint0(processMessage(level, object), args);
    }

    @Override
    public void eprintln0(LoggerLevel level, Object object, Object... args) {
        eprintln0(processMessage(level, object), args);
    }

    @Override
    public void eprintln0() {
        ERROR.println();
        if(isPrintingCaret()) {
            ERROR.print(">");
        }
    }

    @Override
    public void eprintln0(LoggerLevel level) {
        ERROR.print(LoggerColoring.replaceColoring(level.getConsoleText()));
        eprintln0();
    }

    public void info0(Object object, Object... args) {
        this.println0(LoggerLevel.INFO, object, args);
    }

    public void ok0(Object object, Object... args) {
        this.println0(LoggerLevel.OK, object, args);
    }

    public void debug0(Object object, Object... args) {
        this.println0(LoggerLevel.DEBUG, object, args);
    }

    public void warning0(Object object, Object... args) {
        this.println0(LoggerLevel.WARNING, object, args);
    }

    public void error0(Object object, Object... args) {
        ERROR.print(processMessageAndColor(LoggerLevel.ERROR, object).formatted(args));
        eprintln0();
    }

    public void error0(Throwable throwable) {
        ERROR.print(processMessageAndColor(LoggerLevel.ERROR, throwable.getMessage()));
        throwable.printStackTrace(ERROR);

        eprintln0();
    }

    @Override
    public void errorWithSuppressed0(Throwable throwable) {
        this.error0(throwable);

        this.printSuppressed(throwable, 0);
    }

    @Override
    public void reportError0(Object object, Object... args) {
        var msg = processMessageAndColor(LoggerLevel.ERROR, object).formatted(args);
        Bukkit.getLogger().severe(msg);
        ERROR.print(msg);
        eprintln0();

        Helix.errors().reportError(msg, ErrorType.ERROR);
    }

    @Override
    public void reportError0(Throwable throwable) {
        var msg = processMessageAndColor(LoggerLevel.ERROR, throwable.getMessage());
        ERROR.print(msg);
        throwable.printStackTrace(ERROR);
        eprintln0();

        Helix.errors().reportError(msg, ErrorType.ERROR);
    }

    private String processMessageAndColor(LoggerLevel level, Object object) {
        return LoggerColoring.replaceColoring(level.getConsoleText() + object);
    }

    private String processMessage(LoggerLevel level, Object object) {
        return level.getConsoleText() + object;
    }

    private static PrintStream newPrintStream(FileDescriptor fileDescriptor) {
        return newPrintStream(new FileOutputStream(fileDescriptor), System.getProperties().getProperty("sun.stdout.encoding"));
    }

    private void printSuppressed(Throwable throwable, int indent) {
        var space = "   ".repeat(indent) + " - ";

        this.printSuppressedLine(space, throwable);

        for (var suppressed : throwable.getSuppressed()) {
            if (suppressed.getSuppressed().length > 0) {
                this.printSuppressed(suppressed, indent + 1);
                continue;
            }

            this.printSuppressedLine(space, suppressed);
        }
    }

    private void printSuppressedLine(String space, Throwable throwable) {
        var trace = throwable.fillInStackTrace().getStackTrace();
        var file = trace.length == 0 ? "Unknown" : trace[trace.length - 1].getClassName();
        var line = trace.length == 0 ? "-1" : trace[trace.length - 1].getLineNumber();

        this.error0("%s%s [%s:%s]", space, throwable.getMessage(), file, line);
    }

    /**
     * Taken from {@link java.lang.System#newPrintStream(java.io.FileOutputStream, java.lang.String)}
     * */
    private static PrintStream newPrintStream(FileOutputStream fos, String enc) {
        if (enc != null) {
            try {
                return new PrintStream(new BufferedOutputStream(fos, 128), true, enc);
            } catch (UnsupportedEncodingException ignored) {}
        }
        return new PrintStream(new BufferedOutputStream(fos, 128), true);
    }

}
