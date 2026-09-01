package view.gui.widgets;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import controller.SaveService;
import controller.menu.LevelController;
import view.gui.GameContext;
import view.gui.SettingsPopup;
import view.gui.Theme;

public final class LevelPausePopup extends SettingsPopup {


    private final GameContext context;
    private final LevelController controller;
    private final Runnable onResume;

    public LevelPausePopup(GameContext context, LevelController controller, Runnable onResume) {
        super(context);
        this.context = context;
        this.controller = controller;
        this.onResume = onResume;
        addLevelButtons();
    }

    private void addLevelButtons() {
        Table row = new Table();
        row.add(ui.faceButton("Save and Exit", "primary", new Runnable() {
            @Override
            public void run() {
                new SaveService().persist(context.user());
                controller.suspend();
                close();
            }
        })).pad(Theme.PAD_SMALL);
        row.add(ui.faceButton("Quit", "danger", new Runnable() {
            @Override
            public void run() {
                leave();
            }
        })).pad(Theme.PAD_SMALL);
        row.add(ui.faceButton("Restart", "secondary", new Runnable() {
            @Override
            public void run() {
                controller.restart();
                close();
            }
        })).pad(Theme.PAD_SMALL);
        footer().row();
        footer().add(row).center();
    }

    private void leave() {
        controller.leave();
        close();
    }

    @Override
    public void close() {
        if (onResume != null) {
            onResume.run();
        }
        super.close();
    }
}
