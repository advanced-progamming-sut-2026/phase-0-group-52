package view.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import util.Log;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Saves what is currently on screen to a PNG.
 *
 * <p>Grabbing the frame buffer beats capturing the desktop: it works while the
 * window is behind something else, and the image is exactly the game's pixels. Bound
 * to F12 so screenshots can be taken for reports and bug threads without extra
 * tooling.
 */
public final class Screenshots {

    private static final SimpleDateFormat STAMP = new SimpleDateFormat("yyyyMMdd-HHmmss");

    private Screenshots() {
    }

    /** Writes the current frame to {@code screenshots/} and returns the file. */
    public static FileHandle capture() {
        return capture("screenshots/pvz-" + STAMP.format(new Date()) + ".png");
    }

    /** Writes the current frame to an explicit path, relative to the working directory. */
    public static FileHandle capture(String path) {
        int width = Gdx.graphics.getBackBufferWidth();
        int height = Gdx.graphics.getBackBufferHeight();

        byte[] pixels = ScreenUtils.getFrameBufferPixels(0, 0, width, height, true);

        // The frame buffer has no meaningful alpha; force it opaque so the PNG is
        // not saved as a fully transparent image.
        for (int i = 3; i < pixels.length; i += 4) {
            pixels[i] = (byte) 255;
        }

        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        ByteBuffer buffer = pixmap.getPixels();
        BufferUtils.copy(pixels, 0, buffer, pixels.length);

        FileHandle file = Gdx.files.local(path);
        PixmapIO.writePNG(file, pixmap);
        pixmap.dispose();

        Log.info("gui", "Screenshot saved to " + file.path());
        return file;
    }
}
