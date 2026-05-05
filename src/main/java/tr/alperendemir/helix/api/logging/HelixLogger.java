package tr.alperendemir.helix.api.logging;

public abstract class HelixLogger {

    private static HelixLogger logger;

    public HelixLogger() {
        if (logger != null) {
            throw new IllegalStateException("Cannot create two HelixLogger instances!");
        }

        logger = this;
    }

    public abstract void printCaret0(boolean print);

    public abstract boolean isPrintingCaret0();

    public abstract void syncLog0(Runnable runnable);

    public abstract void down0(int lines);
    public abstract void up0(int lines);

    public abstract void left0(int columns);
    public abstract void right0(int columns);

    public abstract void clearLine0();

    public abstract void print0(Object object, Object... args);
    public abstract void println0(Object object, Object... args);

    public abstract void print0(LoggerLevel level, Object object, Object... args);
    public abstract void println0(LoggerLevel level, Object object, Object... args);

    public abstract void println0();
    public abstract void println0(LoggerLevel level);

    public abstract void eprint0(Object object, Object... args);
    public abstract void eprintln0(Object object, Object... args);

    public abstract void eprint0(LoggerLevel level, Object object, Object... args);
    public abstract void eprintln0(LoggerLevel level, Object object, Object... args);

    public abstract void eprintln0();
    public abstract void eprintln0(LoggerLevel level);

    public abstract void info0(Object object, Object... args);

    public abstract void ok0(Object object, Object... args);

    public abstract void debug0(Object object, Object... args);

    public abstract void warning0(Object object, Object... args);

    public abstract void error0(Object object, Object... args);
    public abstract void error0(Throwable throwable);

    public abstract void errorWithSuppressed0(Throwable throwable);

    public abstract void reportError0(Object object, Object... args);
    public abstract void reportError0(Throwable throwable);

    public static void printCaret(boolean print) {
        logger.printCaret0(print);
    }

    public static boolean isPrintingCaret() {
        return logger.isPrintingCaret0();
    }

    public static void syncLog(Runnable runnable) {
        logger.syncLog0(runnable);
    }

    public static void down(int lines) {
        logger.down0(lines);
    }

    public static void up(int lines) {
        logger.up0(lines);
    }

    public static void left(int columns) {
        logger.left0(columns);
    }

    public static void right(int columns) {
        logger.right0(columns);
    }

    public static void clearLine() {
        logger.clearLine0();
    }

    public static void print(Object object, Object... args) {
        logger.print0(object, args);
    }

    public static void println(Object object, Object... args) {
        logger.println0(object, args);
    }

    public static void print(LoggerLevel level, Object object, Object... args) {
        logger.print0(level, object, args);
    }

    public static void println(LoggerLevel level, Object object, Object... args) {
        logger.println0(level, object, args);
    }

    public static void eprint(Object object, Object... args) {
        logger.eprint0(object, args);
    }

    public static void eprintln(Object object, Object... args) {
        logger.eprintln0(object, args);
    }

    public static void eprint(LoggerLevel level, Object object, Object... args) {
        logger.eprint0(level, object, args);
    }

    public static void eprintln(LoggerLevel level, Object object, Object... args) {
        logger.eprintln0(level, object, args);
    }

    public static void eprintln() {
        logger.eprintln0();
    }

    public static void eprintln(LoggerLevel level) {
        logger.eprintln0(level);
    }

    public static void println() {
        logger.println0();
    }

    public static void println(LoggerLevel level) {
        logger.println0(level);
    }

    public static void info(Object object, Object... args) {
        logger.info0(object, args);
    }

    public static void ok(Object object, Object... args) {
        logger.ok0(object, args);
    }

    public static void debug(Object object, Object... args) {
        logger.debug0(object, args);
    }

    public static void warning(Object object, Object... args) {
        logger.warning0(object, args);
    }

    public static void error(Object object, Object... args) {
        logger.error0(object, args);
    }

    public static void error(Throwable throwable) {
        logger.error0(throwable);
    }

    public static void errorWithSuppressed(Throwable throwable) {
        logger.errorWithSuppressed0(throwable);
    }

    public static void reportError(Object object, Object... args) {
        logger.reportError0(object, args);
    }

    public static void reportError(Throwable throwable) {
        logger.reportError0(throwable);
    }
}
