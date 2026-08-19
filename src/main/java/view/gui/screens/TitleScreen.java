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
import view.gui.GameContext;
import view.gui.PlayerListPopup;
import view.gui.Popup;
import view.gui.SettingsPopup;
import view.gui.Theme;
import view.gui.UiKit;

public final class TitleScreen implements Screen {
    private final GameContext context;
    private final UiKit ui;
    private final Stage stage;
    private final Runnable onPlay;

    private Label playerName;

    public TitleScreen(GameContext context, Runnable onPlay) {
        this.context = context;
        this.ui = context.ui();
        this.onPlay = onPlay;
        this.stage = new Stage(new FitViewport(Theme.WORLD_WIDTH, Theme.WORLD_HEIGHT));
        build();
    }

    private void build() {
        Image backdrop = new Image(new TextureRegion(ui.primitives().verticalGradient(
                64, 256, Theme.BACKDROP_ALT, Theme.BACKDROP)));
        backdrop.setScaling(Scaling.stretch);
        backdrop.setFillParent(true);
        stage.addActor(backdrop);

        Table root = new Table();
        root.setFillParent(true);
        root.pad(Theme.PAD_LARGE);

        Table centre = new Table();
        centre.add(titleBlock()).padBottom(Theme.PAD_LARGE).row();
        centre.add(playerBadge()).width(430f).height(52f).padBottom(Theme.PAD).row();
        centre.add(playButton()).width(260f).height(74f).row();

        root.add(centre).expand().center().row();
        root.add(bottomRow()).growX();

        stage.addActor(root);
        Animations.enter(root);
    }

    private Table titleBlock() {
        Table block = new Table();

        Label plants = new Label("PLANTS", ui.skin(), "hugeOnDark");
        plants.setColor(Theme.GREEN_LIGHT);
        Label versus = new Label("vs.", ui.skin(), "titleOnDark");
        versus.setColor(Theme.SUN);
        Label zombies = new Label("ZOMBIES", ui.skin(), "hugeOnDark");

        Table line = new Table();
        line.add(plants).padRight(Theme.PAD_SMALL);
        line.add(versus).padRight(Theme.PAD_SMALL);
        line.add(zombies);
        line.add(new Label("2", ui.skin(), "hugeOnDark")).padLeft(Theme.PAD_SMALL);

        block.add(line).row();

        Label tagline = new Label("IT'S ABOUT TIME", ui.skin(), "onDark");
        tagline.setColor(Theme.TEXT_ON_DARK);
        tagline.setAlignment(Align.center);
        block.add(tagline).padTop(Theme.PAD_SMALL);
        return block;
    }

    private Table playerBadge() {
        Table badge = new Table();
        badge.setBackground(ui.primitives().rounded(Theme.RADIUS + 6,
                Theme.lighten(Theme.PANEL, 0.35f), Theme.OUTLINE, Theme.BORDER));
        playerName = new Label("", ui.skin(), "title");
        playerName.setAlignment(Align.center);
        badge.add(playerName).expandX().center().padLeft(34f);
        badge.add(ui.token(26, Theme.SUN_DEEP)).size(26f).right().padRight(Theme.PAD);

        Animations.attachPress(badge);
        UiKit.onClick(badge, new Runnable() {
            @Override
            public void run() {
                openPlayers();
            }
        });
        return badge;
    }

    private Table playButton() {
        Table button = new Table();
        button.setBackground(ui.primitives().rounded(Theme.RADIUS + 6,
                Theme.plantFamily("MODIFIER"), Theme.darken(Theme.plantFamily("MODIFIER"), 0.35f),
                Theme.BORDER + 1));
        Label label = new Label("Play", ui.skin(), "hugeOnDark");
        label.setAlignment(Align.center);
        button.add(label).expand().center();

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
        row.add(iconButton("X", Theme.RED, new Runnable() {
            @Override
            public void run() {
                Gdx.app.exit();
            }
        })).size(58f).left();

        row.add(new Table()).expandX();

        row.add(iconButton("Players", Theme.BLUE, new Runnable() {
            @Override
            public void run() {
                openPlayers();
            }
        })).height(58f).width(120f).padRight(Theme.PAD_SMALL);

        row.add(iconButton("Settings", Theme.plantFamily("WALL_NUT"), new Runnable() {
            @Override
            public void run() {
                openSettings();
            }
        })).height(58f).width(120f);
        return row;
    }

    private Table iconButton(String text, com.badlogic.gdx.graphics.Color face, Runnable action) {
        Table button = new Table();
        button.setBackground(ui.primitives().rounded(Theme.RADIUS,
                face, Theme.darken(face, 0.35f), Theme.BORDER));
        Label label = new Label(text, ui.skin(), "onDark");
        label.setAlignment(Align.center);
        button.add(label).expand().center();
        Animations.attachPress(button);
        UiKit.onClick(button, action);
        return button;
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
        Gdx.input.setInputProcessor(stage);
        refreshName();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(Theme.BACKDROP.r, Theme.BACKDROP.g, Theme.BACKDROP.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        refreshName();
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
