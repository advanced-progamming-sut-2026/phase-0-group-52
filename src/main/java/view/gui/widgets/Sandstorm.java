package view.gui.widgets;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import view.gui.Assets;
import view.gui.LawnGeometry;
import view.gui.Theme;

public final class Sandstorm extends Group {

    public static final String REAR = "768/INITIAL/EFFECTS/SANDSTORM_REAR/SANDSTORM_REAR.PAM";
    public static final String TOP = "768/INITIAL/EFFECTS/SANDSTORM_TOP/SANDSTORM_TOP.PAM";

    private static final float CANVAS = 320f;
    private static final float SWEEP = 2.6f;
    private static final float HOLD = 1.4f;
    private static final float WIDTH = 1.9f;

    private final PamActor rear;
    private final PamActor top;

    private float clock;
    private boolean blowing;

    public Sandstorm(Assets assets) {
        setTouchable(Touchable.disabled);
        rear = PlantStage.anchored(assets, REAR, "intro", CANVAS, CANVAS);
        top = PlantStage.anchored(assets, TOP, "intro", CANVAS, CANVAS);
        if (rear.isReady()) {
            addActor(rear);
        }
        if (top.isReady()) {
            addActor(top);
        }
        setVisible(false);
    }

    public boolean isReady() {
        return rear.isReady() || top.isReady();
    }

    public boolean isBlowing() {
        return blowing;
    }

    public void blow() {
        if (!isReady()) {
            return;
        }
        blowing = true;
        clock = 0f;
        setVisible(true);
        play("intro");
    }

    private void play(String clip) {
        if (rear.isReady()) {
            rear.play(clip, "loop".equals(clip), null);
        }
        if (top.isReady()) {
            top.play(clip, "loop".equals(clip), null);
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!blowing) {
            return;
        }
        float before = clock;
        clock += delta;
        if (before < HOLD && clock >= HOLD) {
            play("loop");
        }
        if (before < SWEEP && clock >= SWEEP) {
            play("outro");
        }
        if (clock >= SWEEP + HOLD) {
            blowing = false;
            setVisible(false);
        }
        layoutStorm();
    }

    private void layoutStorm() {
        float height = LawnGeometry.areaHeight() * 1.35f;
        float width = height * WIDTH;
        float travel = Theme.WORLD_WIDTH + width;
        float progress = Math.min(1f, clock / (SWEEP + HOLD));
        float x = Theme.WORLD_WIDTH - travel * progress;
        float y = LawnGeometry.areaY() - height * 0.15f;
        if (rear.isReady()) {
            rear.setBounds(x, y, width, height);
        }
        if (top.isReady()) {
            top.setBounds(x, y, width, height);
        }
    }
}
