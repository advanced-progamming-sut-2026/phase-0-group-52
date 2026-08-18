package view.gui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;

import java.util.HashMap;
import java.util.Map;

/**
 * Builds every image the interface uses at runtime, from code.
 *
 * <p>Nothing here loads a file. Panels, buttons, seed packets and entity stand-ins
 * are drawn into {@link Pixmap}s and uploaded as textures, which keeps the whole
 * shell working before any art exists. When real art arrives, screens keep calling
 * the same methods and only the bodies change, so no layout code has to move.
 *
 * <p>Results are cached by shape and colour, so repeated calls for the same button
 * background share one texture.
 */
public final class Primitives implements Disposable {

    private final Map<String, Texture> cache = new HashMap<String, Texture>();

    // ----------------------------------------------------------- primitives

    /** A filled rectangle. */
    public Texture rect(int width, int height, Color fill) {
        String key = "rect:" + width + "x" + height + ":" + fill;
        Texture cached = cache.get(key);
        if (cached != null) {
            return cached;
        }
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(fill);
        pixmap.fill();
        Texture texture = upload(pixmap);
        cache.put(key, texture);
        return texture;
    }

    /** A one-pixel white texture, tinted by the caller. */
    public Texture pixel() {
        return rect(1, 1, Color.WHITE);
    }

    /** A filled circle, used for suns, coins and entity stand-ins. */
    public Texture circle(int diameter, Color fill, Color border, int borderWidth) {
        String key = "circle:" + diameter + ":" + fill + ":" + border + ":" + borderWidth;
        Texture cached = cache.get(key);
        if (cached != null) {
            return cached;
        }
        Pixmap pixmap = new Pixmap(diameter, diameter, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(0f, 0f, 0f, 0f);
        pixmap.fill();
        int radius = diameter / 2;
        if (border != null && borderWidth > 0) {
            pixmap.setColor(border);
            pixmap.fillCircle(radius, radius, radius - 1);
            pixmap.setColor(fill);
            pixmap.fillCircle(radius, radius, Math.max(1, radius - 1 - borderWidth));
        } else {
            pixmap.setColor(fill);
            pixmap.fillCircle(radius, radius, radius - 1);
        }
        Texture texture = upload(pixmap);
        cache.put(key, texture);
        return texture;
    }

    /**
     * A rounded rectangle with an optional border, returned as a nine-patch so it
     * stretches cleanly to any widget size.
     */
    public NinePatch roundedPatch(int radius, Color fill, Color border, int borderWidth) {
        String key = "patch:" + radius + ":" + fill + ":" + border + ":" + borderWidth;
        Texture cached = cache.get(key);
        int size = radius * 2 + 3;
        if (cached == null) {
            Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
            pixmap.setBlending(Pixmap.Blending.None);
            pixmap.setColor(0f, 0f, 0f, 0f);
            pixmap.fill();

            if (border != null && borderWidth > 0) {
                drawRoundedRect(pixmap, 0, 0, size, size, radius, border);
                drawRoundedRect(pixmap, borderWidth, borderWidth,
                        size - borderWidth * 2, size - borderWidth * 2,
                        Math.max(1, radius - borderWidth), fill);
            } else {
                drawRoundedRect(pixmap, 0, 0, size, size, radius, fill);
            }
            cached = upload(pixmap);
            cache.put(key, cached);
        }
        // The centre row/column is what stretches; corners stay crisp.
        return new NinePatch(new TextureRegion(cached), radius + 1, radius + 1, radius + 1, radius + 1);
    }

    /** Convenience wrapper returning a ready-to-use drawable. */
    public Drawable rounded(int radius, Color fill, Color border, int borderWidth) {
        return new NinePatchDrawable(roundedPatch(radius, fill, border, borderWidth));
    }

    /** A flat colour drawable that stretches to any size. */
    public Drawable flat(Color fill) {
        return new TextureRegionDrawable(new TextureRegion(rect(4, 4, fill)));
    }

    /**
     * The standard panel look: parchment face, thick brown outline.
     */
    public Drawable panel() {
        return rounded(Theme.RADIUS, Theme.PANEL, Theme.OUTLINE, Theme.BORDER);
    }

    /** A recessed area inside a panel, for lists and detail boxes. */
    public Drawable sunken() {
        return rounded(Theme.RADIUS - 2, Theme.PANEL_SUNKEN, Theme.OUTLINE_SOFT, 2);
    }

    // -------------------------------------------------------------- stand-ins

    /**
     * The placeholder for a plant or zombie: a coloured disc carrying the initials
     * of the entity, framed like a token so it reads against the lawn.
     *
     * <p>This is the single method to replace when sprite sheets arrive.
     */
    public Texture entityToken(int diameter, Color body, Color rim) {
        return circle(diameter, body, rim, Math.max(2, diameter / 12));
    }

    /**
     * A seed-packet background: tan card, brown border, darker strip along the
     * bottom where the sun cost is printed.
     */
    public Texture packetFace(int width, int height, Color tint, boolean locked) {
        String key = "packet:" + width + "x" + height + ":" + tint + ":" + locked;
        Texture cached = cache.get(key);
        if (cached != null) {
            return cached;
        }
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(0f, 0f, 0f, 0f);
        pixmap.fill();

        Color face = locked ? Theme.darken(Theme.PANEL, 0.35f) : Theme.PANEL;
        drawRoundedRect(pixmap, 0, 0, width, height, 8, Theme.OUTLINE);
        drawRoundedRect(pixmap, 3, 3, width - 6, height - 6, 6, face);

        // Coloured band at the top hints at the plant family.
        pixmap.setColor(locked ? Theme.darken(tint, 0.45f) : tint);
        pixmap.fillRectangle(3, 3, width - 6, 6);

        // Cost strip along the bottom.
        pixmap.setColor(Theme.darken(Theme.PANEL_SUNKEN, locked ? 0.4f : 0.12f));
        pixmap.fillRectangle(3, height - 22, width - 6, 19);

        Texture texture = upload(pixmap);
        cache.put(key, texture);
        return texture;
    }

    /**
     * A horizontal progress bar background, used for seed-packet progress, quest
     * progress and the wave meter.
     */
    public Texture bar(int width, int height, Color fill, Color border) {
        String key = "bar:" + width + "x" + height + ":" + fill + ":" + border;
        Texture cached = cache.get(key);
        if (cached != null) {
            return cached;
        }
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(0f, 0f, 0f, 0f);
        pixmap.fill();
        int radius = Math.min(height / 2, 8);
        drawRoundedRect(pixmap, 0, 0, width, height, radius, border);
        drawRoundedRect(pixmap, 2, 2, width - 4, height - 4, Math.max(1, radius - 2), fill);
        Texture texture = upload(pixmap);
        cache.put(key, texture);
        return texture;
    }

    /**
     * A striped lawn background. Rows alternate shade the way the game's turf does.
     */
    public Texture lawn(int width, int height, int rows, Color light, Color dark) {
        String key = "lawn:" + width + "x" + height + ":" + rows + ":" + light + ":" + dark;
        Texture cached = cache.get(key);
        if (cached != null) {
            return cached;
        }
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        int rowHeight = Math.max(1, height / Math.max(1, rows));
        for (int r = 0; r < rows; r++) {
            pixmap.setColor((r % 2 == 0) ? light : dark);
            pixmap.fillRectangle(0, r * rowHeight, width, rowHeight);
        }
        Texture texture = upload(pixmap);
        cache.put(key, texture);
        return texture;
    }

    /** A soft vertical gradient, used behind menu screens. */
    public Texture verticalGradient(int width, int height, Color top, Color bottom) {
        String key = "grad:" + width + "x" + height + ":" + top + ":" + bottom;
        Texture cached = cache.get(key);
        if (cached != null) {
            return cached;
        }
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        for (int y = 0; y < height; y++) {
            float t = y / (float) Math.max(1, height - 1);
            pixmap.setColor(
                    top.r + (bottom.r - top.r) * t,
                    top.g + (bottom.g - top.g) * t,
                    top.b + (bottom.b - top.b) * t,
                    1f);
            pixmap.drawLine(0, y, width, y);
        }
        Texture texture = upload(pixmap);
        cache.put(key, texture);
        return texture;
    }

    // ------------------------------------------------------------- internals

    /**
     * Fills a rounded rectangle. {@link Pixmap} has no such primitive, so this is
     * a centre rectangle, two side rectangles and four corner discs.
     */
    private void drawRoundedRect(Pixmap pixmap, int x, int y, int width, int height,
            int radius, Color color) {
        int r = Math.max(0, Math.min(radius, Math.min(width, height) / 2));
        pixmap.setColor(color);
        if (r == 0) {
            pixmap.fillRectangle(x, y, width, height);
            return;
        }
        pixmap.fillRectangle(x + r, y, width - r * 2, height);
        pixmap.fillRectangle(x, y + r, r, height - r * 2);
        pixmap.fillRectangle(x + width - r, y + r, r, height - r * 2);
        pixmap.fillCircle(x + r, y + r, r);
        pixmap.fillCircle(x + width - r - 1, y + r, r);
        pixmap.fillCircle(x + r, y + height - r - 1, r);
        pixmap.fillCircle(x + width - r - 1, y + height - r - 1, r);
    }

    private Texture upload(Pixmap pixmap) {
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

    @Override
    public void dispose() {
        for (Texture texture : cache.values()) {
            texture.dispose();
        }
        cache.clear();
    }
}
