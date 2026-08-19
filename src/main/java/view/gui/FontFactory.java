package view.gui;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import util.Log;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

final class FontFactory {
    static final String FONT_FOLDER = "assets/fonts";

    static final String[] DISPLAY_PREFERRED = {
            "Burbank Big Condensed Bold",
            "Burbank Big Cd Bd",
            "BurbankBigCondensed-Bold",
            "Burbank Big Condensed",
            "Burbank",
            "Bahnschrift SemiBold Condensed",
            "Arial Narrow",
            "Haettenschweiler",
            "Impact",
            "Arial Black",
            "SansSerif",
    };

    static final String[] SUBTEXT_PREFERRED = {
            "Brianne's Hand",
            "Brianne's hand",
            "Briannes Hand",
            "BrianneTod",
            "Segoe UI",
            "Verdana",
            "SansSerif",
    };

    private static final char FIRST_CHAR = 32;
    private static final char LAST_CHAR = 126;

    private static final int PADDING = 2;

    private static Map<String, Font> fileFonts;
    private static final Map<String, Font> RESOLVED = new HashMap<String, Font>();

    private FontFactory() {
    }

    static BitmapFont create(int size, String[] preferred) {
        try {
            Font awtFont = pickFont(size, preferred);
            if (awtFont == null) {
                return null;
            }
            return rasterise(awtFont);
        } catch (RuntimeException e) {
            Log.warn("gui", "Falling back to the built-in font: " + e.getMessage());
            return null;
        } catch (LinkageError e) {
            Log.warn("gui", "AWT unavailable; using the built-in font");
            return null;
        }
    }

    private static synchronized Font pickFont(int size, String[] preferred) {
        String key = preferred[0];
        Font base = RESOLVED.get(key);
        if (base == null) {
            base = resolveBase(preferred);
            RESOLVED.put(key, base);
        }
        return (base == null) ? null : base.deriveFont((float) size);
    }

    private static Font resolveBase(String[] preferred) {
        Map<String, Font> supplied = suppliedFonts();
        for (String wanted : preferred) {
            Font match = supplied.get(wanted.toLowerCase());
            if (match != null) {
                Log.info("gui", "Using supplied font " + match.getFamily()
                        + " for " + preferred[0]);
                return match.deriveFont(Font.PLAIN, 16f);
            }
        }
        for (String wanted : preferred) {
            Font candidate = new Font(wanted, Font.BOLD, 16);
            if (candidate.getFamily().equalsIgnoreCase(wanted)) {
                Log.info("gui", "Using installed font " + candidate.getFamily()
                        + " for " + preferred[0]);
                return candidate;
            }
        }
        Log.warn("gui", "No match for " + preferred[0] + "; using the default sans face");
        return new Font(Font.SANS_SERIF, Font.BOLD, 16);
    }

    private static Map<String, Font> suppliedFonts() {
        if (fileFonts != null) {
            return fileFonts;
        }
        fileFonts = new HashMap<String, Font>();
        File folder = new File(FONT_FOLDER);
        if (!folder.isDirectory()) {
            return fileFonts;
        }
        File[] candidates = folder.listFiles();
        if (candidates == null) {
            return fileFonts;
        }
        java.util.Arrays.sort(candidates);
        for (File file : candidates) {
            String name = file.getName().toLowerCase();
            if (!name.endsWith(".ttf") && !name.endsWith(".otf")) {
                continue;
            }
            try {
                Font loaded = Font.createFont(Font.TRUETYPE_FONT, file);
                fileFonts.put(loaded.getFamily().toLowerCase(), loaded);
                fileFonts.put(loaded.getFontName().toLowerCase(), loaded);
                Log.debug("gui", "Found font file " + file.getName()
                        + " -> " + loaded.getFamily());
            } catch (java.awt.FontFormatException e) {
                Log.warn("gui", "Not a usable font file: " + file.getName());
            } catch (java.io.IOException e) {
                Log.warn("gui", "Could not read font file: " + file.getName());
            }
        }
        return fileFonts;
    }

    private static BitmapFont rasterise(Font awtFont) {
        BufferedImage probe = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D probeGraphics = probe.createGraphics();
        probeGraphics.setFont(awtFont);
        FontMetrics metrics = probeGraphics.getFontMetrics();

        int glyphHeight = metrics.getHeight();
        int columns = 16;
        int rows = (LAST_CHAR - FIRST_CHAR + columns) / columns;

        int cellWidth = 0;
        for (char c = FIRST_CHAR; c <= LAST_CHAR; c++) {
            cellWidth = Math.max(cellWidth, metrics.charWidth(c));
        }
        cellWidth += PADDING * 2;
        int cellHeight = glyphHeight + PADDING * 2;
        probeGraphics.dispose();

        int atlasWidth = nextPowerOfTwo(cellWidth * columns);
        int atlasHeight = nextPowerOfTwo(cellHeight * rows);

        BufferedImage atlas = new BufferedImage(atlasWidth, atlasHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = atlas.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setFont(awtFont);
        graphics.setColor(Color.WHITE);

        BitmapFont.BitmapFontData data = describe(metrics, glyphHeight);
        drawGlyphs(graphics, metrics, data, columns, cellWidth, cellHeight, glyphHeight);
        graphics.dispose();

        BitmapFont.Glyph space = data.getGlyph(' ');
        if (space != null) {
            data.spaceXadvance = space.xadvance;
        }

        Texture texture = new Texture(toPixmapFast(atlas), true);
        texture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);

        BitmapFont font = new BitmapFont(data, new TextureRegion(texture), false);
        font.setUseIntegerPositions(false);
        font.setOwnsTexture(true);
        return font;
    }

    private static BitmapFont.BitmapFontData describe(FontMetrics metrics, int glyphHeight) {
        BitmapFont.BitmapFontData data = new BitmapFont.BitmapFontData();
        data.setScale(1f);
        data.lineHeight = glyphHeight;
        data.capHeight = metrics.getAscent();
        data.ascent = 0f;
        data.descent = -metrics.getDescent();
        data.down = -data.lineHeight;
        data.blankLineScale = 1f;
        return data;
    }

    private static void drawGlyphs(Graphics2D graphics, FontMetrics metrics,
            BitmapFont.BitmapFontData data, int columns, int cellWidth, int cellHeight,
            int glyphHeight) {
        for (char c = FIRST_CHAR; c <= LAST_CHAR; c++) {
            int index = c - FIRST_CHAR;
            int x = (index % columns) * cellWidth + PADDING;
            int y = (index / columns) * cellHeight + PADDING;

            graphics.drawString(String.valueOf(c), x, y + metrics.getAscent());

            BitmapFont.Glyph glyph = new BitmapFont.Glyph();
            glyph.id = c;
            glyph.srcX = x;
            glyph.srcY = y;
            glyph.width = metrics.charWidth(c);
            glyph.height = glyphHeight;
            glyph.xoffset = 0;

            glyph.yoffset = -glyph.height;
            glyph.xadvance = metrics.charWidth(c);
            data.setGlyph(c, glyph);
        }
    }

    private static Pixmap toPixmapFast(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] argb = ((java.awt.image.DataBufferInt) image.getRaster().getDataBuffer()).getData();
        byte[] rgba = new byte[width * height * 4];
        for (int i = 0; i < argb.length; i++) {
            int pixel = argb[i];
            int at = i * 4;
            rgba[at] = (byte) ((pixel >> 16) & 0xFF);
            rgba[at + 1] = (byte) ((pixel >> 8) & 0xFF);
            rgba[at + 2] = (byte) (pixel & 0xFF);
            rgba[at + 3] = (byte) ((pixel >>> 24) & 0xFF);
        }
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        java.nio.ByteBuffer buffer = pixmap.getPixels();
        buffer.clear();
        buffer.put(rgba);
        buffer.position(0);
        return pixmap;
    }

    private static int nextPowerOfTwo(int value) {
        int result = 1;
        while (result < value) {
            result <<= 1;
        }
        return result;
    }
}
