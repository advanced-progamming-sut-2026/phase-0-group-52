package view.gui.widgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import view.gui.GameContext;
import view.gui.Popup;
import view.gui.Theme;

public final class MinigameOutcomePopup extends Popup {

    private static final float WIDTH = 620f;
    private static final float HEIGHT = 420f;
    private static final float HEADLINE = 1.5f;
    private static final float SCORE_SCALE = 1.25f;

    public MinigameOutcomePopup(GameContext context, boolean won, String message,
            int score, int best, final Runnable onReplay, final Runnable onLeave) {
        super(context.ui(), won ? "Nicely Done!" : "So Close!", WIDTH, HEIGHT);

        Label headline = new Label(message == null ? "" : message, ui.skin(), "onDark");
        headline.setWrap(true);
        headline.setAlignment(Align.center);
        headline.setFontScale(HEADLINE);
        headline.setColor(won ? Theme.SUN : Theme.RED_LIGHT);
        body().add(headline).growX().center().pad(Theme.PAD).row();

        Label tally = new Label("Score  " + score + "        Best  " + Math.max(best, score),
                ui.skin(), "titleOnDark");
        tally.setAlignment(Align.center);
        tally.setFontScale(SCORE_SCALE);
        body().add(tally).growX().center().padBottom(Theme.PAD).row();

        Table row = new Table();
        row.add(ui.faceButton("Play again", "secondary", new Runnable() {
            @Override
            public void run() {
                close();
                if (onReplay != null) {
                    onReplay.run();
                }
            }
        })).pad(Theme.PAD_SMALL);
        row.add(ui.faceButton("Back to the menu", "primary", new Runnable() {
            @Override
            public void run() {
                close();
                if (onLeave != null) {
                    onLeave.run();
                }
            }
        })).pad(Theme.PAD_SMALL);
        footer().add(row).center();
        sealClose();
    }

    private void sealClose() {
        for (Actor actor : getChildren()) {
            hideCloseButton(actor);
        }
    }

    private void hideCloseButton(Actor actor) {
        if (!(actor instanceof Table)) {
            return;
        }
        for (Actor child : ((Table) actor).getChildren()) {
            if (Theme.RED.equals(child.getColor())) {
                child.setVisible(false);
                child.setTouchable(Touchable.disabled);
            }
            hideCloseButton(child);
        }
    }
}
