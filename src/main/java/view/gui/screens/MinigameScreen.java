package view.gui.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import controller.menu.MinigameRunController;
import minigame.MinigameType;
import model.Result;
import model.entities.plants.Plants;
import model.entities.zombies.Zombies;
import view.gui.Assets;
import view.gui.GameContext;
import view.gui.LawnGeometry;
import view.gui.Navigator;
import view.gui.Theme;
import view.gui.UiKit;
import view.gui.widgets.LawnField;
import view.gui.widgets.LawnView;
import view.gui.widgets.LevelNotice;
import view.gui.widgets.LevelOutcomePopup;
import view.gui.widgets.MinigameHud;

public final class MinigameScreen implements Screen, Navigator.Hosted {

    private final GameContext context;
    private final UiKit ui;
    private final Stage stage;
    private final MinigameRunController controller;

    private LawnView lawn;
    private LawnField field;
    private MinigameHud hud;
    private LevelNotice notice;
    private Label banner;
    private Plants armed;
    private Zombies armedZombie;
    private boolean shown;
    private boolean started;

    public MinigameScreen(GameContext context) {
        this.context = context;
        this.ui = context.ui();
        this.stage = context.assets().newStage();
        this.controller = new MinigameRunController(context.app());
    }

    @Override
    public Stage uiStage() {
        return stage;
    }

    private Assets assets() {
        return context.assets();
    }

    private void build() {
        stage.clear();
        lawn = new LawnView(assets(), controller.chapter());
        lawn.setCamera(0f);
        stage.addActor(lawn);

        field = new LawnField(ui, assets(), controller);
        field.setFillParent(true);
        field.setShowGrid(true);
        field.setMinigame(controller);
        field.onPlant(new LawnField.Sink() {
            @Override
            public void picked(int column, int row) {
                act(column, row);
            }

            @Override
            public void missed() {
                clearCarry();
            }
        });
        stage.addActor(field);

        hud = buildHud();
        stage.addActor(hud);
        stage.addActor(buildChrome());
        stage.addActor(context.toasts());
        notice.announce(controller.objective());
    }

    private MinigameHud buildHud() {
        return new MinigameHud(context, controller, new MinigameHud.Sink() {
            @Override
            public void armedPlant(Plants plant) {
                armed = plant;
                armedZombie = null;
            }

            @Override
            public void armedZombie(Zombies zombie) {
                armedZombie = zombie;
                armed = null;
            }

            @Override
            public void quit() {
                controller.leave();
            }
        });
    }

    private Table buildChrome() {
        notice = new LevelNotice(ui);
        stage.addActor(notice);
        banner = new Label("", ui.skin(), "titleOnDark");
        banner.setAlignment(Align.center);
        banner.setColor(Theme.SUN);
        Table bannerRow = new Table();
        bannerRow.setFillParent(true);
        bannerRow.top().padTop(Theme.PAD_LARGE * 2.6f);
        bannerRow.add(banner);
        return bannerRow;
    }

    private void act(int column, int row) {
        if (controller.isVasebreaker()) {
            report(controller.smash(column, row));
            return;
        }
        if (controller.isBowling()) {
            report(controller.roll(column, row));
            return;
        }
        if (controller.isIZombie()) {
            if (armedZombie == null) {
                context.toasts().error("Pick a zombie first.");
                return;
            }
            report(controller.buy(armedZombie, row));
            return;
        }
        if (armed == null) {
            return;
        }
        Result planted = controller.plant(armed, column, row);
        if (!planted.isSuccess()) {
            context.toasts().error(planted.getMessage());
            return;
        }
        clearCarry();
    }

    private void report(Result result) {
        if (!result.isSuccess()) {
            context.toasts().error(result.getMessage());
            return;
        }
        if (result.getMessage() != null && !result.getMessage().isEmpty()) {
            context.toasts().info(result.getMessage());
        }
    }

    private void clearCarry() {
        armed = null;
        armedZombie = null;
        if (hud != null) {
            hud.clearArmed();
        }
    }

    private void finish() {
        if (shown || controller.outcome() == null) {
            return;
        }
        shown = true;
        controller.setPaused(true);
        new LevelOutcomePopup(context, controller, controller.hasWon(),
                controller.outcome() + "\n\nScore " + controller.score()
                        + "   Best " + controller.best(), new Runnable() {
                            @Override
                            public void run() {
                                replay();
                            }
                        }).showOn(stage);
    }

    private void replay() {
        Result again = controller.restart();
        if (!again.isSuccess()) {
            context.toasts().error(again.getMessage());
            controller.leave();
            return;
        }
        shown = false;
        clearCarry();
        controller.setPaused(false);
        build();
    }

    @Override
    public void show() {
        shown = false;
        if (controller.game() == null || controller.outcome() != null) {
            Result opened = controller.start();
            if (!opened.isSuccess()) {
                context.toasts().error(opened.getMessage());
                controller.leave();
                return;
            }
        }
        started = true;
        build();
        Gdx.input.setInputProcessor(stage);
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    clearCarry();
                    return true;
                }
                return false;
            }
        });
    }

    private String bannerText() {
        MinigameType kind = controller.kind();
        if (kind == null) {
            return "";
        }
        if (controller.isVasebreaker()) {
            Plants held = controller.held();
            return held == null
                    ? "Vases left  " + controller.vasesLeft()
                    : "Place your " + held.getName() + "!";
        }
        if (controller.isIZombie()) {
            return "Brains left  " + controller.brainsLeft();
        }
        if (controller.isScoreAttack()) {
            return "Score  " + controller.score() + " / " + controller.scoreTarget();
        }
        if (controller.isBowling()) {
            return "Nuts ready  " + controller.bowlingBelt().size();
        }
        return "Score  " + controller.score();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if (!started) {
            return;
        }
        float speed = Math.max(1, context.settings().getGameSpeed());
        controller.setSpeed(speed);
        view.gui.widgets.PamActor.setWorldRate(speed);
        controller.tick(delta);
        view.gui.widgets.PamActor.freezeAll(controller.isPaused());
        banner.setText(bannerText());
        hud.refresh();
        finish();
        field.setArmed(armed);
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
        context.toasts().remove();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    public float lawnWidth() {
        return lawn == null ? LawnGeometry.scaled(0f) : lawn.lawnWidth();
    }

    public Actor hudActor() {
        return hud;
    }
}
