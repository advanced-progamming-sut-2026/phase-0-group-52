package view.gui;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

public class Popup extends Table {
    private static final float MARGIN = 48f;

    protected final UiKit ui;
    private final Table body = new Table();
    private final Table footer = new Table();
    private final Table frame;
    private final Table header = new Table();
    private final Table headerSlots = new Table();
    private final ScrollPane scroll;

    public Popup(UiKit ui, String title, float width, float height) {
        this.ui = ui;
        this.frame = ui.panel();

        setFillParent(true);
        setTouchable(Touchable.enabled);
        setBackground(ui.drawable("scrim"));
        center();

        Label heading = new Label(title, ui.skin(), "hugeOnDark");
        heading.setAlignment(Align.center);

        header.add(headerSlots).left().uniformX();
        header.add(heading).expandX().center();
        header.add(closeButton()).right().size(52f).uniformX();
        frame.add(header).growX().padBottom(Theme.PAD).row();

        body.top();
        scroll = new ScrollPane(body, ui.skin());
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);
        scroll.setOverscroll(false, false);
        UiKit.focusOnHover(scroll);
        frame.add(scroll).grow().row();

        footer.bottom();
        frame.add(footer).growX().padTop(Theme.PAD_SMALL).row();

        add(frame)
                .width(Math.min(width, Theme.WORLD_WIDTH - MARGIN))
                .height(Math.min(height, Theme.WORLD_HEIGHT - MARGIN));
        Animations.enter(frame);
    }

    private com.badlogic.gdx.scenes.scene2d.Actor closeButton() {
        return ui.iconButton(Icons.CLOSE_POPUP, "X", Theme.RED, new Runnable() {
            @Override
            public void run() {
                close();
            }
        });
    }

    protected Table body() {
        return body;
    }

    protected Table footer() {
        return footer;
    }

    protected Popup addHeaderIcon(Icons.Icon icon, String fallbackText, Runnable action) {
        headerSlots.add(ui.iconButton(icon, fallbackText, Theme.BLUE, action))
                .size(52f).padRight(Theme.PAD_SMALL);
        return this;
    }

    protected ScrollPane scroller() {
        return scroll;
    }

    public void close() {
        Animations.exit(this, new Runnable() {
            @Override
            public void run() {
                remove();
            }
        });
    }

    public void showOn(Stage stage) {
        stage.addActor(this);
        stage.setScrollFocus(scroll);
    }
}
