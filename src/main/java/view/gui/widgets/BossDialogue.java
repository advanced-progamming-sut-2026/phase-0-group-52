package view.gui.widgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import view.gui.Assets;
import view.gui.GameContext;
import view.gui.Theme;
import view.gui.UiKit;

import java.util.List;

public final class BossDialogue extends Table {

    public static final float HOLD = 2.6f;

    private static final float PORTRAIT = 148f;
    private static final float BUBBLE_WIDTH = 620f;
    private static final float BUBBLE_PAD = 18f;
    private static final float FADE = 0.25f;
    private static final float LIFT = 96f;
    private static final int CORNER = 22;

    private final UiKit ui;
    private final Label line;
    private final List<String> lines;
    private final Runnable done;

    private int at;
    private float clock;

    public BossDialogue(GameContext context, String speaker, List<String> lines,
            Runnable done) {
        this.ui = context.ui();
        this.lines = lines;
        this.done = done;

        Table bubble = new Table();
        bubble.setBackground(ui.primitives().rounded(CORNER,
                Theme.alpha(Theme.PANEL, 0.96f), Theme.OUTLINE, 4));
        bubble.pad(BUBBLE_PAD);

        Label who = new Label(speaker, ui.skin(), "titleOnDark");
        who.setColor(Theme.RED_LIGHT);
        bubble.add(who).left().row();

        line = new Label("", ui.skin(), "onDark");
        line.setWrap(true);
        line.setAlignment(Align.left);
        bubble.add(line).width(BUBBLE_WIDTH).left().padTop(6f);

        Table row = new Table();
        Actor face = portrait(context.assets());
        if (face != null) {
            row.add(face).size(PORTRAIT).padRight(Theme.PAD_SMALL).bottom();
        }
        row.add(bubble).bottom();

        setFillParent(true);
        bottom();
        add(row).padBottom(LIFT);
        getColor().a = 0f;
        addAction(Actions.fadeIn(FADE));
        setTouchable(Touchable.enabled);
        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                advance();
            }
        });
        show();
    }

    private Actor portrait(Assets assets) {
        if (assets == null) {
            return null;
        }
        com.badlogic.gdx.graphics.g2d.TextureRegion art =
                assets.region("IMAGE_UI_QUESTS_QUESTICONS_ZOMBOSS");
        if (art == null) {
            return null;
        }
        Image face = new Image(art);
        face.setScaling(Scaling.fit);
        face.setTouchable(Touchable.disabled);
        return face;
    }

    private void show() {
        line.setText(lines.get(at));
        clock = 0f;
    }

    private void advance() {
        at++;
        if (at >= lines.size()) {
            remove();
            if (done != null) {
                done.run();
            }
            return;
        }
        show();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        clock += delta;
        if (clock >= HOLD) {
            advance();
        }
    }

    public static boolean play(Stage stage, GameContext context, String speaker,
            List<String> lines, Runnable done) {
        if (lines == null || lines.isEmpty()) {
            return false;
        }
        stage.addActor(new BossDialogue(context, speaker, lines, done));
        return true;
    }
}
