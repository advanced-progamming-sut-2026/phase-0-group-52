package view.gui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public final class XpBar extends Widget {

    private final Drawable track;
    private final Drawable fill;
    private final float ratio;
    private final float inset;

    public XpBar(Drawable track, Drawable fill, float ratio, float inset) {
        this.track = track;
        this.fill = fill;
        this.ratio = Math.max(0f, Math.min(1f, ratio));
        this.inset = inset;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Color tint = getColor();
        batch.setColor(tint.r, tint.g, tint.b, tint.a * parentAlpha);
        track.draw(batch, getX(), getY(), getWidth(), getHeight());
        float span = (getWidth() - inset * 2f) * ratio;
        if (span > 1f) {
            fill.draw(batch, getX() + inset, getY() + inset, span, getHeight() - inset * 2f);
        }
    }
}
