package view.gui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public final class Layered extends BaseDrawable {
    private final Drawable under;
    private final Drawable over;
    private final float inset;

    public Layered(Drawable under, Drawable over, float inset) {
        this.under = under;
        this.over = over;
        this.inset = inset;
        setLeftWidth(over.getLeftWidth());
        setRightWidth(over.getRightWidth());
        setTopHeight(over.getTopHeight());
        setBottomHeight(over.getBottomHeight());
        setMinWidth(over.getMinWidth());
        setMinHeight(over.getMinHeight());
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height) {
        under.draw(batch, x + inset, y + inset,
                Math.max(0f, width - inset * 2f), Math.max(0f, height - inset * 2f));
        over.draw(batch, x, y, width, height);
    }
}
