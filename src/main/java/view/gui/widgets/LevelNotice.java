package view.gui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import view.gui.Theme;
import view.gui.UiKit;

public final class LevelNotice extends Table {

    private static final float FADE_IN = 0.35f;
    private static final float HOLD = 1.9f;
    private static final float FADE_OUT = 0.6f;
    private static final float SCALE = 1.9f;
    private static final Color ALERT = new Color(0.94f, 0.11f, 0.11f, 1f);

    private final Label line;

    public LevelNotice(UiKit ui) {
        setFillParent(true);
        setTouchable(Touchable.disabled);
        center();
        Label.LabelStyle base = ui.skin().get("titleOnDark", Label.LabelStyle.class);
        line = new Label("", new Label.LabelStyle(base.font, ALERT));
        line.setAlignment(Align.center);
        line.setFontScale(SCALE);
        line.getColor().a = 0f;
        add(line).center().padBottom(Theme.PAD_LARGE);
    }

    public void announce(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        line.setText(text);
        line.clearActions();
        line.getColor().a = 0f;
        line.addAction(Actions.sequence(
                Actions.fadeIn(FADE_IN),
                Actions.delay(HOLD),
                Actions.fadeOut(FADE_OUT)));
        toFront();
    }
}
