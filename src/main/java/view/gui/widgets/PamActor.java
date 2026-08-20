package view.gui.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import view.gui.Pam;

public final class PamActor extends Widget {
    private final Pam pam;
    private final String path;
    private final String clipName;
    private final boolean ready;
    private final Rectangle extent;

    private float stateTime;
    private float coverage = 1f;

    public PamActor(Pam pam, String path, String clipName) {
        this.pam = pam;
        this.path = path;
        this.clipName = clipName;
        this.ready = pam != null && pam.load(path);
        this.extent = this.ready ? pam.player().bounds(path, clipName) : null;
        setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
    }

    public PamActor setCoverage(float coverage) {
        this.coverage = coverage;
        return this;
    }

    public boolean isReady() {
        return ready;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (!ready || extent.width <= 0f || extent.height <= 0f) {
            return;
        }
        pam.update();
        stateTime += Gdx.graphics.getDeltaTime();

        float scale = Math.max(getWidth() / extent.width, getHeight() / extent.height) * coverage;
        float x = getX() + getWidth() / 2f - (extent.x + extent.width / 2f) * scale;
        float y = getY() + getHeight() / 2f - (extent.y + extent.height / 2f) * scale;

        batch.flush();
        if (!clipBegin(getX(), getY(), getWidth(), getHeight())) {
            return;
        }
        pam.player().draw(batch, path, clipName, stateTime, x, y, scale, scale, true);
        batch.flush();
        clipEnd();
    }
}
