package view.gui.widgets;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import view.gui.Assets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class HeadSwapActor extends Widget {

    private static final float HEAD_TARGET = 65f;

    private static final String[] HEAD_PARTS = {
        "zombie_skull", "zombie_jaw", "zombie_pupil",
    };

    private final Assets pam;
    private final String bodyPath;
    private final String headPath;
    private final Map<String, Boolean> hidden = new HashMap<String, Boolean>();

    private String clipName;
    private Rectangle extent;
    private float stateTime;
    private float headScale = 1f;
    private boolean ready;

    public HeadSwapActor(Assets pam, String bodyPath, String headPath, String clipName,
            List<String> hideParts) {
        this.pam = pam;
        this.bodyPath = bodyPath;
        this.headPath = headPath;
        this.clipName = clipName;
        setTouchable(Touchable.disabled);
        this.ready = pam != null && pam.load(bodyPath) && pam.load(headPath);
        if (!ready) {
            return;
        }
        for (String part : HEAD_PARTS) {
            hidden.put(part, Boolean.FALSE);
        }
        if (hideParts != null) {
            for (String part : hideParts) {
                hidden.put(part, Boolean.FALSE);
            }
        }
        this.extent = pam.player().bounds(bodyPath, clipName);
    }

    public HeadSwapActor setHeadScale(float value) {
        headScale = value;
        return this;
    }

    public boolean isReady() {
        return ready && extent != null && extent.width > 0f && extent.height > 0f;
    }

    public void play(String clip) {
        if (!ready) {
            return;
        }
        clipName = clip;
        stateTime = 0f;
        extent = pam.player().bounds(bodyPath, clipName);
    }

    @Override
    public float getPrefWidth() {
        return 0f;
    }

    @Override
    public float getPrefHeight() {
        return 0f;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (!isReady()) {
            return;
        }
        pam.update();
        stateTime += com.badlogic.gdx.Gdx.graphics.getDeltaTime();

        float scale = Math.min(getWidth() / extent.width, getHeight() / extent.height);
        float flippedY = -(extent.y + extent.height);
        float x = getX() + getWidth() / 2f - (extent.x + extent.width / 2f) * scale;
        float y = getY() + getHeight() / 2f - (flippedY + extent.height / 2f) * scale;

        pam.player().draw(batch, bodyPath, clipName, stateTime, x, y, scale, scale, true, hidden);

        Rectangle head = pam.player().partBounds(bodyPath, clipName, stateTime, HEAD_PARTS[0]);
        if (head == null) {
            return;
        }
        Rectangle crown = pam.player().bounds(headPath, headClip());
        if (crown == null || crown.width <= 0f) {
            return;
        }
        float neckX = x + (head.x + head.width / 2f) * scale;
        float neckY = y - (head.y + head.height / 2f) * scale;
        float headSize = scale * (HEAD_TARGET / crown.width) * headScale;
        pam.player().draw(batch, headPath, headClip(), stateTime, neckX, neckY,
                headSize, headSize, true);
    }

    private String headClip() {
        List<String> clips = pam.player().clips(headPath);
        if (clips == null || clips.isEmpty()) {
            return "idle";
        }
        for (String clip : clips) {
            if (clip.endsWith("idle")) {
                return clip;
            }
        }
        return clips.get(0);
    }
}
