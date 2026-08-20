package view.gui;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public final class Animations {
    private Animations() {
    }

    private static final com.badlogic.gdx.graphics.Color RESTING =
            new com.badlogic.gdx.graphics.Color(0.87f, 0.87f, 0.87f, 1f);

    public static void attachPress(final Actor actor) {
        actor.setColor(RESTING);
        actor.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1) {
                    actor.setOrigin(com.badlogic.gdx.utils.Align.center);
                    actor.clearActions();
                    actor.addAction(Actions.parallel(
                            Actions.scaleTo(1.06f, 1.06f, 0.09f, Interpolation.pow2Out),
                            Actions.color(com.badlogic.gdx.graphics.Color.WHITE, 0.09f)));
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1) {
                    actor.clearActions();
                    actor.addAction(Actions.parallel(
                            Actions.scaleTo(1f, 1f, 0.09f, Interpolation.pow2Out),
                            Actions.color(RESTING, 0.12f)));
                }
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                actor.setOrigin(com.badlogic.gdx.utils.Align.center);
                actor.clearActions();
                actor.addAction(Actions.scaleTo(0.95f, 0.95f, 0.05f, Interpolation.pow2Out));
                return false;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                actor.clearActions();
                actor.addAction(Actions.scaleTo(1f, 1f, 0.12f, Interpolation.swingOut));
            }
        });
    }

    public static void enter(Actor actor) {
        actor.getColor().a = 0f;
        actor.addAction(Actions.fadeIn(Theme.TRANSITION_TIME, Interpolation.pow2Out));
    }

    public static void exit(Actor actor, Runnable after) {
        actor.addAction(Actions.sequence(
                Actions.fadeOut(Theme.TRANSITION_TIME * 0.7f, Interpolation.pow2In),
                Actions.run(after)));
    }

    public static void pulse(Actor actor) {
        actor.setOrigin(com.badlogic.gdx.utils.Align.center);
        actor.clearActions();
        actor.addAction(Actions.sequence(
                Actions.scaleTo(1.18f, 1.18f, 0.08f, Interpolation.pow2Out),
                Actions.scaleTo(1f, 1f, 0.16f, Interpolation.swingOut)));
    }

    public static void shake(Actor actor) {
        actor.clearActions();
        actor.addAction(Actions.sequence(
                Actions.color(Theme.RED_LIGHT, 0.06f),
                Actions.color(com.badlogic.gdx.graphics.Color.WHITE, 0.28f)));
    }
}
