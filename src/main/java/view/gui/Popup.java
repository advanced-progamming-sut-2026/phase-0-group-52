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
    private final Table frame = new Table();
    private final Table header = new Table();
    private final Table headerSlots = new Table();
    private final ScrollPane scroll;

    public Popup(UiKit ui, String title, float width, float height) {
        this.ui = ui;

        setFillParent(true);
        setTouchable(Touchable.enabled);
        setBackground(ui.drawable("scrim"));
        center();

        frame.setBackground(ui.primitives().rounded(Theme.RADIUS + 4,
                Theme.PANEL_FRAME, Theme.OUTLINE, Theme.BORDER + 1));
        frame.pad(Theme.PAD);

        Label heading = new Label(title, ui.skin(), "title");
        heading.setAlignment(Align.center);
        header.add(heading).expandX().center().padLeft(44f);
        header.add(headerSlots).right().padRight(Theme.PAD_SMALL);
        header.add(closeButton()).right().size(38f);
        frame.add(header).growX().padBottom(Theme.PAD_SMALL).row();

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

    protected Popup addHeaderButton(String text, com.badlogic.gdx.graphics.Color face,
            Runnable action) {
        Table button = new Table();
        button.setBackground(ui.primitives().rounded(Theme.RADIUS, face,
                Theme.darken(face, 0.4f), 2));
        Label label = new Label(text, ui.skin(), "smallOnDark");
        label.setAlignment(Align.center);
        button.add(label).pad(2f, Theme.PAD_SMALL, 2f, Theme.PAD_SMALL);
        Animations.attachPress(button);
        UiKit.onClick(button, action);
        headerSlots.add(button).height(34f).padRight(Theme.PAD_SMALL);
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
