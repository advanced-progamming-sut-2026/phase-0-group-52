package view.gui.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import view.gui.Pam;

public final class PamActor extends Widget {
    private final Pam pam;
    private final String path;
    private final boolean ready;
    private final Rectangle extent;

    private String clipName;
    private boolean looping = true;
    private boolean fit;
    private float duration;
    private float stateTime;
    private float coverage = 1f;
    private Runnable onFinished;

    public PamActor(Pam pam, String path, String clipName) {
        this.pam = pam;
        this.path = path;
        this.ready = pam != null && pam.load(path);
        this.extent = this.ready ? pam.player().bounds(path, clipName) : null;
        this.clipName = clipName;
        this.duration = this.ready ? pam.player().clipDurationSeconds(path, clipName) : 0f;
        setTouchable(Touchable.disabled);
    }

    public PamActor setCoverage(float value) {
        this.coverage = value;
        return this;
    }

    public PamActor setFit(boolean value) {
        this.fit = value;
        return this;
    }

    public boolean isReady() {
        return ready;
    }

    public void play(String clip, boolean loop, Runnable finished) {
        if (!ready) {
            if (finished != null) {
                finished.run();
            }
            return;
        }
        clipName = clip;
        looping = loop;
        stateTime = 0f;
        onFinished = finished;
        duration = pam.player().clipDurationSeconds(path, clip);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (!ready || extent.width <= 0f || extent.height <= 0f) {
            return;
        }
        pam.update();
        advance();

        float byWidth = getWidth() / extent.width;
        float byHeight = getHeight() / extent.height;
        float scale = (fit ? Math.min(byWidth, byHeight) : Math.max(byWidth, byHeight)) * coverage;
        float x = getX() + getWidth() / 2f - (extent.x + extent.width / 2f) * scale;
        float y = getY() + getHeight() / 2f - (extent.y + extent.height / 2f) * scale;

        if (fit) {
            pam.player().draw(batch, path, clipName, stateTime, x, y, scale, scale, looping);
            return;
        }
        batch.flush();
        if (!clipBegin(getX(), getY(), getWidth(), getHeight())) {
            return;
        }
        pam.player().draw(batch, path, clipName, stateTime, x, y, scale, scale, looping);
        batch.flush();
        clipEnd();
    }

    private void advance() {
        stateTime += Gdx.graphics.getDeltaTime();
        if (looping || duration <= 0f || stateTime < duration) {
            return;
        }
        stateTime = duration;
        final Runnable done = onFinished;
        onFinished = null;
        if (done != null) {
            Gdx.app.postRunnable(done);
        }
    }
}
