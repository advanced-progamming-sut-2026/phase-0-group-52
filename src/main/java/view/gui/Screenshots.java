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

public final class Screenshots {
    private static final SimpleDateFormat STAMP = new SimpleDateFormat("yyyyMMdd-HHmmss");

    private Screenshots() {
    }

    public static FileHandle capture() {
        return capture("screenshots/pvz-" + STAMP.format(new Date()) + ".png");
    }

    public static FileHandle capture(String path) {
        int width = Gdx.graphics.getBackBufferWidth();
        int height = Gdx.graphics.getBackBufferHeight();

        byte[] pixels = ScreenUtils.getFrameBufferPixels(0, 0, width, height, true);

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
