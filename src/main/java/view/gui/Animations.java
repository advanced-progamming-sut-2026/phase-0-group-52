package view.gui;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

/**
 * Small motion helpers shared by every screen.
 *
 * <p>The specification lists interface animation — menu transitions and button
 * feedback — under its optional "beauty" tier, so these are deliberately short and
 * cheap: a press dip, a hover lift, and a fade/slide for screen changes. Anything
 * longer gets in the way of a game that is mostly clicking.
 */
public final class Animations {

    private Animations() {
    }

    /**
     * Gives a button a springy press: it scales down while held and pops back on
     * release, and lifts very slightly on hover.
     */
    public static void attachPress(final Actor actor) {
        actor.setOrigin(com.badlogic.gdx.utils.Align.center);
        actor.addListener(new ClickListener() {

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1) {
                    actor.clearActions();
                    actor.addAction(Actions.scaleTo(1.04f, 1.04f, 0.09f, Interpolation.pow2Out));
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1) {
                    actor.clearActions();
                    actor.addAction(Actions.scaleTo(1f, 1f, 0.09f, Interpolation.pow2Out));
                }
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
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

    /** Fades and slides an actor in from below; used when a screen opens. */
    public static void enter(Actor actor) {
        actor.getColor().a = 0f;
        actor.addAction(Actions.parallel(
                Actions.fadeIn(Theme.TRANSITION_TIME, Interpolation.pow2Out),
                Actions.sequence(
                        Actions.moveBy(0f, -18f),
                        Actions.moveBy(0f, 18f, Theme.TRANSITION_TIME, Interpolation.pow3Out))));
    }

    /** Fades an actor out and runs {@code after} once it is gone. */
    public static void exit(Actor actor, Runnable after) {
        actor.addAction(Actions.sequence(
                Actions.fadeOut(Theme.TRANSITION_TIME * 0.7f, Interpolation.pow2In),
                Actions.run(after)));
    }

    /** A short attention pulse, used when a value changes (coins, sun, progress). */
    public static void pulse(Actor actor) {
        actor.setOrigin(com.badlogic.gdx.utils.Align.center);
        actor.clearActions();
        actor.addAction(Actions.sequence(
                Actions.scaleTo(1.18f, 1.18f, 0.08f, Interpolation.pow2Out),
                Actions.scaleTo(1f, 1f, 0.16f, Interpolation.swingOut)));
    }

    /** A left-right shake, used to reject invalid input. */
    public static void shake(Actor actor) {
        actor.clearActions();
        actor.addAction(Actions.sequence(
                Actions.moveBy(-7f, 0f, 0.04f),
                Actions.moveBy(14f, 0f, 0.08f),
                Actions.moveBy(-14f, 0f, 0.08f),
                Actions.moveBy(7f, 0f, 0.04f)));
    }
}
