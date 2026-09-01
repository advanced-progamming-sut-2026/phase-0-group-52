package view.gui.widgets;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;

public final class LevelIntro {

    private static final float PAN_OUT = 1.9f;
    private static final float HOLD = 0.8f;
    private static final float PAN_BACK = 1.1f;

    private final LawnView lawn;
    private Runnable finished;
    private boolean running;

    public LevelIntro(LawnView lawn) {
        this.lawn = lawn;
    }

    public boolean isRunning() {
        return running;
    }

    public void play(Runnable whenDone) {
        finished = whenDone;
        running = true;
        lawn.clearActions();
        lawn.setCamera(0f);
        lawn.addAction(Actions.sequence(
                Actions.delay(0.25f),
                pan(lawn.maxCamera(), PAN_OUT, Interpolation.pow3),
                Actions.delay(HOLD),
                pan(0f, PAN_BACK, Interpolation.pow2),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        settle();
                    }
                })));
    }

    public void skip() {
        if (!running) {
            return;
        }
        lawn.clearActions();
        lawn.setCamera(0f);
        settle();
    }

    private void settle() {
        running = false;
        Runnable done = finished;
        finished = null;
        if (done != null) {
            done.run();
        }
    }

    private TemporalAction pan(float target, float seconds, Interpolation curve) {
        Pan action = new Pan(lawn, target);
        action.setDuration(seconds);
        action.setInterpolation(curve);
        return action;
    }

    private static final class Pan extends TemporalAction {
        private static final float MAX_STEP = 1f / 30f;

        private final LawnView lawn;
        private final float target;
        private float from;

        @Override
        public boolean act(float delta) {
            return super.act(Math.min(delta, MAX_STEP));
        }

        Pan(LawnView lawn, float target) {
            this.lawn = lawn;
            this.target = target;
        }

        @Override
        protected void begin() {
            from = lawn.camera();
        }

        @Override
        protected void update(float percent) {
            lawn.setCamera(from + (target - from) * percent);
        }
    }
}
