package view.gui;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

public class Popup extends Table {
    protected final UiKit ui;
    private final Table body = new Table();
    private final Table frame = new Table();

    public Popup(UiKit ui, String title, float width, float height) {
        this.ui = ui;

        setFillParent(true);
        setTouchable(Touchable.enabled);
        setBackground(ui.drawable("scrim"));
        center();

        frame.setBackground(ui.primitives().rounded(Theme.RADIUS + 4,
                Theme.PANEL_FRAME, Theme.OUTLINE, Theme.BORDER + 1));
        frame.pad(Theme.PAD);

        Table header = new Table();
        Label heading = new Label(title, ui.skin(), "title");
        heading.setAlignment(Align.center);
        header.add(heading).expandX().center().padLeft(44f);
        header.add(closeButton()).right().size(38f);
        frame.add(header).growX().padBottom(Theme.PAD_SMALL).row();

        body.top();
        frame.add(body).grow().row();

        add(frame).width(width).height(height);
        Animations.enter(frame);
    }

    private Table closeButton() {
        Table button = new Table();
        button.setBackground(ui.primitives().rounded(14, Theme.RED,
                Theme.darken(Theme.RED, 0.35f), 2));
        Label cross = new Label("X", ui.skin(), "onDark");
        cross.setAlignment(Align.center);
        button.add(cross).expand().center();
        Animations.attachPress(button);
        UiKit.onClick(button, new Runnable() {
            @Override
            public void run() {
                close();
            }
        });
        return button;
    }

    protected Table body() {
        return body;
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
    }
}
