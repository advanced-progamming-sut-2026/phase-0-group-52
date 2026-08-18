package util;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Central log sink for the whole application.
 *
 * <p>The project started as a console game where every layer wrote straight to
 * {@code System.out}. Rather than rewrite those ~350 call sites (which would mean
 * touching models and controllers the graphics phase is supposed to leave alone),
 * {@link #installConsoleCapture()} tees {@code System.out} and {@code System.err}:
 * text still reaches the terminal exactly as before, and a copy lands here where
 * the GUI can pick it up for toasts and the debug overlay.
 *
 * <p>New code should call {@link #info}, {@link #warn} and friends directly, which
 * records a proper source and level instead of a guessed one.
 */
public final class Log {

    /** How many entries are retained before the oldest are dropped. */
    public static final int CAPACITY = 2000;

    public enum Level {
        DEBUG, INFO, WARN, ERROR
    }

    /** A single recorded line. Immutable. */
    public static final class Entry {
        private final long time;
        private final Level level;
        private final String source;
        private final String message;

        Entry(long time, Level level, String source, String message) {
            this.time = time;
            this.level = level;
            this.source = source;
            this.message = message;
        }

        public long getTime() { return time; }
        public Level getLevel() { return level; }
        public String getSource() { return source; }
        public String getMessage() { return message; }

        public String getClockText() {
            return CLOCK.format(new Date(time));
        }

        @Override
        public String toString() {
            return "[" + getClockText() + "] " + level + " " + source + ": " + message;
        }
    }

    /** Notified on every new entry. Implementations must not block. */
    public interface Listener {
        void onEntry(Entry entry);
    }

    private static final SimpleDateFormat CLOCK = new SimpleDateFormat("HH:mm:ss");

    private static final ArrayList<Entry> ENTRIES = new ArrayList<Entry>();
    private static final ArrayList<Listener> LISTENERS = new ArrayList<Listener>();

    private static Level minLevel = Level.DEBUG;
    private static PrintStream originalOut;
    private static PrintStream originalErr;
    private static boolean captureInstalled;

    private Log() {
    }

    // ---------------------------------------------------------------- writing

    public static void debug(String source, String message) {
        add(Level.DEBUG, source, message);
    }

    public static void info(String source, String message) {
        add(Level.INFO, source, message);
    }

    public static void warn(String source, String message) {
        add(Level.WARN, source, message);
    }

    public static void error(String source, String message) {
        add(Level.ERROR, source, message);
    }

    public static void error(String source, String message, Throwable thrown) {
        add(Level.ERROR, source, message + " (" + thrown.getClass().getSimpleName()
                + ": " + thrown.getMessage() + ")");
    }

    /**
     * Records an entry and echoes it to the real console. Entries that arrive via
     * the {@code System.out} tee use {@link #addCaptured} instead, so they are not
     * printed twice.
     */
    private static void add(Level level, String source, String message) {
        if (level.ordinal() < minLevel.ordinal()) {
            return;
        }
        Entry entry = record(level, source, message);
        PrintStream target = (originalOut != null) ? originalOut : System.out;
        target.println(entry.toString());
        publish(entry);
    }

    /** Records a line that has already been written to the terminal. */
    private static void addCaptured(Level level, String source, String message) {
        Entry entry = record(level, source, message);
        publish(entry);
    }

    private static Entry record(Level level, String source, String message) {
        Entry entry = new Entry(System.currentTimeMillis(), level, source, message);
        synchronized (ENTRIES) {
            ENTRIES.add(entry);
            while (ENTRIES.size() > CAPACITY) {
                ENTRIES.remove(0);
            }
        }
        return entry;
    }

    private static void publish(Entry entry) {
        List<Listener> snapshot;
        synchronized (LISTENERS) {
            if (LISTENERS.isEmpty()) {
                return;
            }
            snapshot = new ArrayList<Listener>(LISTENERS);
        }
        for (Listener listener : snapshot) {
            try {
                listener.onEntry(entry);
            } catch (RuntimeException e) {
                // A broken listener must never take down the thing being logged.
                if (originalErr != null) {
                    originalErr.println("Log listener failed: " + e);
                }
            }
        }
    }

    // ---------------------------------------------------------------- reading

    /** All retained entries, oldest first. */
    public static List<Entry> all() {
        synchronized (ENTRIES) {
            return new ArrayList<Entry>(ENTRIES);
        }
    }

    /** The most recent {@code count} entries, oldest first. */
    public static List<Entry> recent(int count) {
        synchronized (ENTRIES) {
            int from = Math.max(0, ENTRIES.size() - count);
            return new ArrayList<Entry>(ENTRIES.subList(from, ENTRIES.size()));
        }
    }

    public static int size() {
        synchronized (ENTRIES) {
            return ENTRIES.size();
        }
    }

    public static void clear() {
        synchronized (ENTRIES) {
            ENTRIES.clear();
        }
    }

    // ------------------------------------------------------------- listeners

    public static void addListener(Listener listener) {
        synchronized (LISTENERS) {
            LISTENERS.add(listener);
        }
    }

    public static void removeListener(Listener listener) {
        synchronized (LISTENERS) {
            LISTENERS.remove(listener);
        }
    }

    // --------------------------------------------------------------- config

    public static Level getMinLevel() {
        return minLevel;
    }

    public static void setMinLevel(Level level) {
        minLevel = (level == null) ? Level.DEBUG : level;
    }

    // -------------------------------------------------------------- capture

    /**
     * Tees {@code System.out} and {@code System.err} into the log. Console output is
     * unchanged; this only adds a copy. Safe to call more than once.
     */
    public static void installConsoleCapture() {
        if (captureInstalled) {
            return;
        }
        originalOut = System.out;
        originalErr = System.err;
        try {
            System.setOut(new PrintStream(new LineTap(originalOut, Level.INFO), true, "UTF-8"));
            System.setErr(new PrintStream(new LineTap(originalErr, Level.ERROR), true, "UTF-8"));
            captureInstalled = true;
        } catch (UnsupportedEncodingException e) {
            // UTF-8 is guaranteed by the platform; if it somehow is not, leave the
            // console alone rather than breaking output entirely.
            System.setOut(originalOut);
            System.setErr(originalErr);
            originalOut.println("Log: console capture unavailable (" + e.getMessage() + ")");
        }
    }

    /** Restores the untouched console streams. */
    public static void uninstallConsoleCapture() {
        if (!captureInstalled) {
            return;
        }
        System.setOut(originalOut);
        System.setErr(originalErr);
        captureInstalled = false;
    }

    public static boolean isConsoleCaptureInstalled() {
        return captureInstalled;
    }

    /** Returns the untouched stdout, bypassing the tee. */
    public static PrintStream console() {
        return (originalOut != null) ? originalOut : System.out;
    }

    /**
     * Forwards bytes to the real stream and, on each completed line, records a
     * copy in the log.
     */
    private static final class LineTap extends OutputStream {

        private final PrintStream mirror;
        private final Level level;
        private final ByteArrayOutputStream pending = new ByteArrayOutputStream(160);

        LineTap(PrintStream mirror, Level level) {
            this.mirror = mirror;
            this.level = level;
        }

        @Override
        public void write(int b) {
            mirror.write(b);
            if (b == '\n') {
                flushLine();
            } else if (b != '\r') {
                pending.write(b);
            }
        }

        @Override
        public void write(byte[] bytes, int offset, int length) {
            for (int i = 0; i < length; i++) {
                write(bytes[offset + i]);
            }
        }

        @Override
        public void flush() {
            mirror.flush();
        }

        private void flushLine() {
            mirror.flush();
            if (pending.size() == 0) {
                return;
            }
            String line;
            try {
                line = pending.toString("UTF-8");
            } catch (UnsupportedEncodingException e) {
                line = pending.toString();
            }
            pending.reset();
            if (line.trim().isEmpty()) {
                return;
            }
            addCaptured(classify(line), "console", line);
        }

        /**
         * The console layer has no level of its own, so infer one from the wording
         * the existing views already use ("Error: ...", "Usage: ...").
         */
        private Level classify(String line) {
            if (level == Level.ERROR) {
                return Level.ERROR;
            }
            String lower = line.toLowerCase();
            if (lower.startsWith("error") || lower.contains("failed") || lower.contains("invalid")) {
                return Level.WARN;
            }
            return Level.INFO;
        }
    }

    /** Read-only view used by tests. */
    public static List<Entry> unmodifiable() {
        synchronized (ENTRIES) {
            return Collections.unmodifiableList(new ArrayList<Entry>(ENTRIES));
        }
    }
}
