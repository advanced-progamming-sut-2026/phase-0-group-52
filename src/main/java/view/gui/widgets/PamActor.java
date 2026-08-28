package view.gui.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import view.gui.Assets;

public final class PamActor extends Widget {
    private final Assets pam;
    private final String path;
    private final boolean ready;
    private Rectangle extent;

    private String clipName;
    private boolean looping = true;
    private boolean fit;
    private boolean frozen;
    private boolean clipped;
    private float duration;
    private float stateTime;
    private float coverage = 1f;
    private Runnable onFinished;
    private java.util.Map<String, Boolean> parts;

    public PamActor(Assets pam, String path, String clipName) {
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

    public PamActor setExtent(float x, float y, float width, float height) {
        if (ready) {
            this.extent = new Rectangle(x, y, width, height);
        }
        return this;
    }

    public PamActor cycle(final String[] clips) {
        if (ready && clips != null && clips.length > 0) {
            playRandom(clips);
        }
        return this;
    }

    private void playRandom(final String[] clips) {
        String next = clips[com.badlogic.gdx.math.MathUtils.random(clips.length - 1)];
        play(next, false, new Runnable() {
            @Override
            public void run() {
                playRandom(clips);
            }
        });
    }

    public PamActor freeze() {
        looping = false;
        frozen = true;
        stateTime = 0f;
        return this;
    }

    public PamActor setParts(java.util.Map<String, Boolean> value) {
        parts = value;
        return this;
    }

    public PamActor setClipped(boolean value) {
        this.clipped = value;
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
        frozen = false;
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

        if (fit && !clipped) {
            pam.player().draw(batch, path, clipName, stateTime, x, y, scale, scale,
                    looping, parts);
            return;
        }
        batch.flush();
        if (!clipBegin(getX(), getY(), getWidth(), getHeight())) {
            return;
        }
        pam.player().draw(batch, path, clipName, stateTime, x, y, scale, scale,
                looping, parts);
        batch.flush();
        clipEnd();
    }

    private void advance() {
        if (frozen) {
            return;
        }
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
