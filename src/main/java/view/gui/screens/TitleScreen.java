package view.gui.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;
import model.User;
import view.gui.Animations;
import view.gui.Backdrops;
import view.gui.GameContext;
import view.gui.Icons;
import view.gui.PlayerListPopup;
import view.gui.Popup;
import view.gui.SettingsPopup;
import view.gui.Theme;
import view.gui.Toasts;
import view.gui.UiKit;

public final class TitleScreen implements Screen {
    private final GameContext context;
    private final UiKit ui;
    private final Stage stage;
    private final Runnable onPlay;
    private final Backdrops backdrops = new Backdrops();

    private Label playerName;

    public TitleScreen(GameContext context, Runnable onPlay) {
        this.context = context;
        this.ui = context.ui();
        this.onPlay = onPlay;
        this.stage = new Stage(new FitViewport(Theme.WORLD_WIDTH, Theme.WORLD_HEIGHT));
        build();
    }

    private void build() {
        stage.addActor(backdrop());

        Table root = new Table();
        root.setFillParent(true);
        root.bottom();

        Table centre = new Table();
        centre.add(playerBadge()).width(444f).height(66f).padBottom(Theme.PAD).row();
        centre.add(playButton()).width(260f).height(78f);

        root.add(centre).padBottom(Theme.PAD_LARGE).row();
        root.add(bottomRow()).growX();

        stage.addActor(root);
        Animations.enter(root);
    }

    private Image backdrop() {
        TextureRegion region = backdrops.random();
        Image image = (region == null)
                ? new Image(new TextureRegion(ui.primitives().verticalGradient(
                        64, 256, Theme.BACKDROP_ALT, Theme.BACKDROP)))
                : new Image(region);
        image.setScaling(Scaling.fill);
        image.setFillParent(true);
        return image;
    }

    private Table playerBadge() {
        Table badge = new Table();
        badge.setBackground(ui.drawable("nameField"));
        playerName = new Label("", ui.skin(), "title");
        playerName.setAlignment(Align.center);
        badge.add(playerName).expandX().center().padLeft(40f);
        badge.add(ui.iconButton(Icons.PLAYERS, "Edit", Theme.BLUE, new Runnable() {
            @Override
            public void run() {
                openPlayers();
            }
        })).size(38f).right().padRight(Theme.PAD);

        Table frame = new Table();
        frame.setBackground(ui.primitives().rounded(Theme.RADIUS + 10,
                Theme.darken(Theme.OUTLINE, 0.15f), Theme.darken(Theme.OUTLINE, 0.6f), 3));
        frame.add(badge).grow().pad(5f);
        return frame;
    }

    private Table playButton() {
        Table button = new Table();
        button.setBackground(ui.buttonFace("green", Theme.plantFamily("MODIFIER")));
        Label label = new Label("Play", ui.skin(), "hugeOnDark");
        label.setAlignment(Align.center);
        button.add(label).expand().center().padBottom(UiKit.opticalPad(label));

        Animations.attachPress(button);
        UiKit.onClick(button, new Runnable() {
            @Override
            public void run() {
                play();
            }
        });
        return button;
    }

    private Table bottomRow() {
        Table row = new Table();
        row.bottom();
        row.pad(0f, Theme.PAD, 0f, Theme.PAD);
        row.add(ui.iconButton(Icons.QUIT_GAME, "Quit", Theme.RED, new Runnable() {
            @Override
            public void run() {
                Gdx.app.exit();
            }
        })).size(66f).left().bottom();

        row.add(new Table()).expandX();

        row.add(ui.iconButton(Icons.SETTINGS, "Settings",
                Theme.plantFamily("WALL_NUT"), new Runnable() {
                    @Override
                    public void run() {
                        openSettings();
                    }
                })).size(60f).right().bottom().padBottom(Theme.PAD);
        return row;
    }

    private void openPlayers() {
        Popup popup = new PlayerListPopup(context);
        popup.showOn(stage);
    }

    private void openSettings() {
        Popup popup = new SettingsPopup(context);
        popup.showOn(stage);
    }

    private void play() {
        if (context.user() == null) {
            context.toasts().error("Choose a player before starting.");
            return;
        }
        onPlay.run();
    }

    private void refreshName() {
        User user = context.user();
        playerName.setText(user == null ? "No player selected" : user.getNickname());
    }

    public Stage stageForCapture() {
        return stage;
    }

    @Override
    public void show() {
        Toasts toasts = context.toasts();
        toasts.setSize(stage.getViewport().getWorldWidth() - Theme.PAD_LARGE * 2, 240f);
        toasts.setPosition(Theme.PAD_LARGE, Theme.PAD_LARGE);
        stage.addActor(toasts);

        Gdx.input.setInputProcessor(stage);
        refreshName();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        refreshName();
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        context.toasts().setSize(
                stage.getViewport().getWorldWidth() - Theme.PAD_LARGE * 2, 240f);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        context.toasts().remove();
    }

    @Override
    public void dispose() {
        backdrops.dispose();
        stage.dispose();
    }
}
