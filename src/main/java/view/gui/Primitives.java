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

public final class Primitives implements Disposable {
    private final Map<String, Texture> cache = new HashMap<String, Texture>();

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

    public Texture pixel() {
        return rect(1, 1, Color.WHITE);
    }

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

    public NinePatch roundedPatch(int radius, Color fill, Color border, int borderWidth) {
        int edge = Math.max(radius, (border == null) ? 0 : borderWidth);
        int size = edge * 2 + 3;
        String key = "patch:" + size + ":" + radius + ":" + fill + ":" + border + ":" + borderWidth;
        Texture cached = cache.get(key);
        if (cached == null) {
            Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
            pixmap.setBlending(Pixmap.Blending.None);
            pixmap.setColor(0f, 0f, 0f, 0f);
            pixmap.fill();

            if (border != null && borderWidth > 0) {
                drawRoundedRect(pixmap, 0, 0, size, size, radius, border);
                drawRoundedRect(pixmap, borderWidth, borderWidth,
                        size - borderWidth * 2, size - borderWidth * 2,
                        Math.max(0, radius - borderWidth), fill);
            } else {
                drawRoundedRect(pixmap, 0, 0, size, size, radius, fill);
            }
            cached = upload(pixmap);
            cache.put(key, cached);
        }

        return new NinePatch(new TextureRegion(cached), edge + 1, edge + 1, edge + 1, edge + 1);
    }

    public Drawable roundedTop(int radius, Color fill, Color border, int borderWidth) {
        int edge = Math.max(radius, (border == null) ? 0 : borderWidth);
        int size = edge * 2 + 3;
        String key = "topPatch:" + size + ":" + radius + ":" + fill + ":" + border
                + ":" + borderWidth;
        Texture cached = cache.get(key);
        if (cached == null) {
            Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
            pixmap.setBlending(Pixmap.Blending.None);
            pixmap.setColor(0f, 0f, 0f, 0f);
            pixmap.fill();
            if (border != null && borderWidth > 0) {
                drawRoundedRect(pixmap, 0, 0, size, size, radius, border);
                drawRoundedRect(pixmap, borderWidth, borderWidth,
                        size - borderWidth * 2, size - borderWidth * 2,
                        Math.max(0, radius - borderWidth), fill);
            } else {
                drawRoundedRect(pixmap, 0, 0, size, size, radius, fill);
            }
            int fillBits = Color.rgba8888(fill);
            int borderBits = (border == null) ? fillBits : Color.rgba8888(border);
            for (int y = size - edge - 1; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    boolean side = borderWidth > 0
                            && (x < borderWidth || x >= size - borderWidth);
                    pixmap.drawPixel(x, y, side ? borderBits : fillBits);
                }
            }
            cached = upload(pixmap);
            cache.put(key, cached);
        }
        return new NinePatchDrawable(
                new NinePatch(new TextureRegion(cached), edge + 1, edge + 1, edge + 1, 1));
    }

    public Drawable rounded(int radius, Color fill, Color border, int borderWidth) {
        return new NinePatchDrawable(roundedPatch(radius, fill, border, borderWidth));
    }

    public Drawable flat(Color fill) {
        return new TextureRegionDrawable(new TextureRegion(rect(4, 4, fill)));
    }

    public Drawable panel() {
        return rounded(Theme.RADIUS, Theme.PANEL, Theme.OUTLINE, Theme.BORDER);
    }

    public Drawable sunken() {
        return rounded(Theme.RADIUS - 2, Theme.PANEL_SUNKEN, Theme.OUTLINE_SOFT, 2);
    }

    public Texture entityToken(int diameter, Color body, Color rim) {
        return circle(diameter, body, rim, Math.max(2, diameter / 12));
    }

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

        pixmap.setColor(locked ? Theme.darken(tint, 0.45f) : tint);
        pixmap.fillRectangle(3, 3, width - 6, 6);

        pixmap.setColor(Theme.darken(Theme.PANEL_SUNKEN, locked ? 0.4f : 0.12f));
        pixmap.fillRectangle(3, height - 22, width - 6, 19);

        Texture texture = upload(pixmap);
        cache.put(key, texture);
        return texture;
    }

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

    public static final int WEDGE_STEPS = 90;
    private static final int WEDGE_TEXTURE = 72;

    public Texture wedge(int ignored, int step, Color fill) {
        int size = WEDGE_TEXTURE;
        String key = "wedge:" + step + ":" + fill;
        Texture cached = cache.get(key);
        if (cached != null) {
            return cached;
        }
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(0f, 0f, 0f, 0f);
        pixmap.fill();
        pixmap.setColor(fill);
        float swept = (float) (Math.PI * 2f * step / (float) WEDGE_STEPS);
        float centre = (size - 1) / 2f;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = x - centre;
                float dy = centre - y;
                float angle = (float) Math.atan2(dx, dy);
                if (angle < 0f) {
                    angle += (float) (Math.PI * 2f);
                }
                if (angle >= (float) (Math.PI * 2f) - swept) {
                    pixmap.drawPixel(x, y);
                }
            }
        }
        Texture texture = upload(pixmap);
        cache.put(key, texture);
        return texture;
    }

    public Texture radialGlow(int size, Color core) {
        String key = "glow:" + size + ":" + core;
        Texture cached = cache.get(key);
        if (cached != null) {
            return cached;
        }
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        float centre = (size - 1) / 2f;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = (x - centre) / centre;
                float dy = (y - centre) / centre;
                float fade = 1f - Math.min(1f, (float) Math.sqrt(dx * dx + dy * dy));
                pixmap.setColor(core.r, core.g, core.b, core.a * fade * fade);
                pixmap.drawPixel(x, y);
            }
        }
        Texture texture = upload(pixmap);
        cache.put(key, texture);
        return texture;
    }

    public Texture verticalGradient(int width, int height, Color top, Color bottom) {
        String key = "grad:" + width + "x" + height + ":" + top + ":" + bottom;
        Texture cached = cache.get(key);
        if (cached != null) {
            return cached;
        }
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        for (int y = 0; y < height; y++) {
            float t = y / (float) Math.max(1, height - 1);
            pixmap.setColor(
                    top.r + (bottom.r - top.r) * t,
                    top.g + (bottom.g - top.g) * t,
                    top.b + (bottom.b - top.b) * t,
                    top.a + (bottom.a - top.a) * t);
            pixmap.drawLine(0, y, width, y);
        }
        Texture texture = upload(pixmap);
        cache.put(key, texture);
        return texture;
    }

    private void drawRoundedRect(Pixmap pixmap, int x, int y, int width, int height,
            int radius, Color color) {
        if (width <= 0 || height <= 0) {
            return;
        }
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
