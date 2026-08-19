package view.gui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

public final class ConfirmPopup extends Popup {
    public ConfirmPopup(UiKit ui, String title, String message, String confirmText,
            final Runnable onConfirm) {
        super(ui, title, 520f, 260f);

        Label text = new Label(message, ui.skin(), "default");
        text.setWrap(true);
        text.setAlignment(Align.center);
        body().add(text).width(430f).padTop(Theme.PAD).row();

        Table actions = new Table();
        actions.add(ui.dangerButton(confirmText, new Runnable() {
            @Override
            public void run() {
                close();
                onConfirm.run();
            }
        })).width(180f).height(46f).padRight(Theme.PAD);
        actions.add(ui.secondaryButton("Cancel", new Runnable() {
            @Override
            public void run() {
                close();
            }
        })).width(150f).height(46f);

        footer().add(actions);
    }
}
