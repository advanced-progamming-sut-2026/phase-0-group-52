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
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Builds a chunky bitmap font at runtime from a system typeface.
 *
 * <p>The stock LibGDX font is a thin 15px face that looks weak once scaled up, and
 * the project has no font files to load. This rasterises an installed heavy
 * typeface into a texture instead, which keeps the "no assets" rule while getting
 * much closer to the bold, rounded lettering the game uses.
 *
 * <p>Glyphs are drawn white so they can be tinted per label. If anything goes wrong
 * — a headless environment, a missing typeface — the caller falls back to the
 * built-in font rather than failing to start.
 */
final class FontFactory {

    /** Preferred faces, heaviest first; the first one installed wins. */
    private static final String[] PREFERRED = {
            "Arial Rounded MT Bold",
            "Segoe UI Black",
            "Arial Black",
            "Verdana",
            "Segoe UI",
            "SansSerif",
    };

    private static final char FIRST_CHAR = 32;
    private static final char LAST_CHAR = 126;
    /** Space around each glyph so neighbours do not bleed in when filtering. */
    private static final int PADDING = 2;

    private FontFactory() {
    }

    /**
     * Rasterises a font at the given pixel size, or returns {@code null} if the
     * platform cannot do it.
     */
    static BitmapFont create(int size) {
        try {
            Font awtFont = pickFont(size);
            if (awtFont == null) {
                return null;
            }
            return rasterise(awtFont);
        } catch (RuntimeException e) {
            Log.warn("gui", "Falling back to the built-in font: " + e.getMessage());
            return null;
        } catch (LinkageError e) {
            // AWT is unavailable on some minimal runtimes.
            Log.warn("gui", "AWT unavailable; using the built-in font");
            return null;
        }
    }

    private static Font pickFont(int size) {
        String[] installed = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames();
        for (String wanted : PREFERRED) {
            for (String candidate : installed) {
                if (candidate.equalsIgnoreCase(wanted)) {
                    return new Font(candidate, Font.BOLD, size);
                }
            }
        }
        return new Font(Font.SANS_SERIF, Font.BOLD, size);
    }

    /** Draws every printable ASCII glyph into one texture and describes it to LibGDX. */
    private static BitmapFont rasterise(Font awtFont) {
        // Measure first so the atlas is only as large as it needs to be.
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

        // A space glyph is required for wrapping to work.
        BitmapFont.Glyph space = data.getGlyph(' ');
        if (space != null) {
            data.spaceXadvance = space.xadvance;
        }

        Texture texture = new Texture(toPixmap(atlas), true);
        texture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);

        // Not flipped: the stage uses a y-up viewport.
        BitmapFont font = new BitmapFont(data, new TextureRegion(texture), false);
        font.setUseIntegerPositions(false);
        font.setOwnsTexture(true);
        return font;
    }

    /** Line metrics shared by every glyph. */
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

    /** Draws each printable character into its cell and records where it landed. */
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
            // Each glyph box starts at the top of the line, so its BMFont y-offset
            // is zero. For an unflipped font LibGDX stores that as -(height + 0),
            // which is what its own loader computes.
            glyph.yoffset = -glyph.height;
            glyph.xadvance = metrics.charWidth(c);
            data.setGlyph(c, glyph);
        }
    }

    /** Copies an AWT image into a LibGDX pixmap, converting ARGB to RGBA. */
    private static Pixmap toPixmap(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = image.getRGB(x, y);
                int alpha = (argb >>> 24) & 0xFF;
                int red = (argb >> 16) & 0xFF;
                int green = (argb >> 8) & 0xFF;
                int blue = argb & 0xFF;
                pixmap.drawPixel(x, y, (red << 24) | (green << 16) | (blue << 8) | alpha);
            }
        }
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
