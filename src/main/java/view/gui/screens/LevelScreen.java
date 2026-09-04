package view.gui.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import controller.menu.LevelController;
import model.entities.plants.Plants;
import view.gui.Assets;
import view.gui.GameContext;
import view.gui.LawnGeometry;
import view.gui.Navigator;
import view.gui.Theme;
import view.gui.UiKit;
import view.gui.widgets.LawnField;
import view.gui.widgets.LawnView;
import view.gui.widgets.LevelHud;
import view.gui.widgets.LevelNotice;
import view.gui.widgets.LevelOutcomePopup;
import view.gui.widgets.LevelPausePopup;


public final class LevelScreen implements Screen, Navigator.Hosted {

    private static final float GHOST_ALPHA = 0.85f;
    private static final float START_BUTTON = 320f;
    private static final float START_LIFT = 150f;
    private static final float GHOST_ICON = 72f;
    private static final float GHOST_SCALE = 1.7f;


    private final GameContext context;
    private final UiKit ui;
    private final Stage stage;
    private final LevelController controller;

    private LawnView lawn;
    private LawnField field;
    private LevelHud hud;
    private Plants armed;
    private Actor carried;
    private boolean feeding;
    private LevelNotice notice;
    private int lastWave = -1;
    private final com.badlogic.gdx.math.Vector2 cursor = new com.badlogic.gdx.math.Vector2();
    private boolean shovelling;
    private boolean shown;

    public LevelScreen(GameContext context) {
        this.context = context;
        this.ui = context.ui();
        this.stage = context.assets().newStage();
        this.controller = new LevelController(context.app());
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
        field.setShowGrid(context.settings().isShowGrid());
        field.onPlant(new LawnField.Sink() {
            @Override
            public void picked(int column, int row) {
                place(column, row);
            }

            @Override
            public void missed() {
                clearCarry();
            }
        });
        if (controller.chapter() == model.ChapterType.DARK_AGES) {
            view.gui.widgets.NightVeil veil = new view.gui.widgets.NightVeil();
            veil.setFillParent(true);
            stage.addActor(veil);
        }
        stage.addActor(field);

        hud = buildHud();
        stage.addActor(hud);
        notice = new LevelNotice(ui);
        stage.addActor(notice);
        stage.addActor(context.toasts());
        notice.announce(controller.objective());
        lastWave = controller.currentWave();
        hud.rebuildPackets();
        buildStartButton();
    }

    private LevelHud buildHud() {
        return new LevelHud(context, controller, new LevelHud.Sink() {
            @Override
            public void armed(Plants plant) {
                armed = plant;
                shovelling = false;
                hud.setArmed(plant);
                carry(plant == null ? null : plantGhost(plant));
            }

            @Override
            public void feeding(boolean on) {
                feeding = on;
                armed = null;
                shovelling = false;
                hud.setArmed(null);
                carry(on ? iconGhost("IMAGE_UI_HUD_INGAME_PLANTFOOD_BUTTON") : null);
            }

            @Override
            public void shovel(boolean on) {
                shovelling = on;
                feeding = false;
                armed = null;
                hud.setArmed(null);
                carry(on ? iconGhost("IMAGE_UI_HUD_INGAME_SHOVEL_ICON") : null);
            }

            @Override
            public void paused() {
                openPause();
            }

            @Override
            public void nuked() {
                context.toasts().info("The lawn is clear.");
            }
        });
    }

    private void carry(Actor ghost) {
        if (carried != null) {
            carried.remove();
        }
        carried = ghost;
        if (carried != null) {
            carried.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
            stage.addActor(carried);
        }
    }

    private Actor plantGhost(Plants plant) {
        model.entities.plants.PlantRecord record = model.entities.plants.PlantData.record(plant);
        if (record == null || assets() == null || !record.getAnimations().hasPlant()) {
            return null;
        }
        view.gui.widgets.PamActor ghost = view.gui.widgets.PlantStage.anchored(assets(),
                record.getAnimations().getPlant(),
                view.gui.widgets.PlantStage.clipOf(record, "idle"),
                record.getAnimations().getCanvasWidth(),
                record.getAnimations().getCanvasHeight());
        if (!ghost.isReady()) {
            return null;
        }
        ghost.freeze();
        ghost.getColor().a = GHOST_ALPHA;
        ghost.setSize(LawnGeometry.cellWidth() * GHOST_SCALE,
                LawnGeometry.cellHeight() * GHOST_SCALE);
        return ghost;
    }

    private Actor iconGhost(String id) {
        com.badlogic.gdx.graphics.g2d.TextureRegion art =
                assets() == null ? null : assets().region(id);
        if (art == null) {
            return null;
        }
        com.badlogic.gdx.scenes.scene2d.ui.Image ghost =
                new com.badlogic.gdx.scenes.scene2d.ui.Image(art);
        ghost.setScaling(com.badlogic.gdx.utils.Scaling.fit);
        float ratio = art.getRegionWidth() / (float) art.getRegionHeight();
        ghost.setSize(GHOST_ICON * ratio, GHOST_ICON);
        return ghost;
    }

    private void followMouse() {
        if (carried == null) {
            return;
        }
        cursor.set(Gdx.input.getX(), Gdx.input.getY());
        stage.getViewport().unproject(cursor);
        carried.setPosition(cursor.x - carried.getWidth() / 2f,
                cursor.y - carried.getHeight() / 2f);
        carried.toFront();
    }

    private void openPause() {
        controller.setPaused(true);
        new LevelPausePopup(context, controller, new Runnable() {
            @Override
            public void run() {
                controller.setPaused(false);
            }
        }).showOn(stage);
    }

    private void finish() {
        if (shown || controller.outcome() == null) {
            return;
        }
        shown = true;
        controller.setPaused(true);
        new LevelOutcomePopup(context, controller,
                controller.hasWon(), controller.outcome(), new Runnable() {
                    @Override
                    public void run() {
                        replay();
                    }
                }).showOn(stage);
    }

    private void buildStartButton() {
        if (!controller.wavesHeld()) {
            return;
        }
        final com.badlogic.gdx.scenes.scene2d.ui.Table bar =
                new com.badlogic.gdx.scenes.scene2d.ui.Table();
        bar.setFillParent(true);
        bar.bottom().padBottom(START_LIFT);
        bar.add(ui.faceButton("Start the waves", "epic", new Runnable() {
            @Override
            public void run() {
                model.Result go = controller.releaseWaves();
                if (go.isSuccess()) {
                    bar.remove();
                    notice.announce(go.getMessage());
                }
            }
        })).width(START_BUTTON);
        stage.addActor(bar);
    }

    private void replay() {
        model.Result again = controller.restart();
        if (!again.isSuccess()) {
            context.toasts().error(again.getMessage());
            controller.leave();
            return;
        }
        shown = false;
        armed = null;
        feeding = false;
        shovelling = false;
        carried = null;
        controller.setPaused(false);
        build();
        notice.announce(controller.objective());
    }

    private void announceWaves() {
        int wave = controller.currentWave();
        if (notice != null && wave > lastWave && wave > 0) {
            notice.announce(wave >= controller.waveCount()
                    ? "A huge wave of zombies is approaching!"
                    : "Wave " + wave + " incoming!");
        }
        lastWave = wave;
    }

    private void clearCarry() {
        armed = null;
        shovelling = false;
        feeding = false;
        hud.setArmed(null);
        carry(null);
    }

    private void place(int column, int row) {
        if (feeding) {
            model.Result fed = controller.feed(column, row);
            if (!fed.isSuccess()) {
                context.toasts().error(fed.getMessage());
            }
            clearCarry();
            return;
        }
        if (shovelling) {
            controller.dig(column, row);
            return;
        }
        if (armed == null) {
            return;
        }
        model.Result result = controller.plant(armed, column, row);
        if (!result.isSuccess()) {
            context.toasts().error(result.getMessage());
            return;
        }
        clearCarry();
    }

    @Override
    public void show() {
        shown = false;
        if (controller.game() == null || controller.outcome() != null) {
            controller.restart();
        }
        build();
        Gdx.input.setInputProcessor(stage);
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    armed = null;
                    shovelling = false;
                    hud.setArmed(null);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        float speed = Math.max(1, context.settings().getGameSpeed());
        controller.setSpeed(speed);
        view.gui.widgets.PamActor.setWorldRate(speed);
        controller.tick(delta);
        view.gui.widgets.PamActor.freezeAll(controller.isPaused());
        announceWaves();
        String drop = controller.takeDrop();
        if (drop != null) {
            context.toasts().info(drop);
        }
        if (controller.consumeStorm() && notice != null) {
            notice.announce("Sandstorms are carrying zombies in!");
        }
        hud.refresh();
        finish();
        field.setArmed(armed);
        field.setTool(shovelling ? view.gui.widgets.LawnField.Tool.SHOVEL
                : feeding ? view.gui.widgets.LawnField.Tool.FOOD
                : view.gui.widgets.LawnField.Tool.NONE);
        followMouse();
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

    public void setGrid(boolean value) {
        if (field != null) {
            field.setShowGrid(value);
        }
    }

    public void arm(Plants plant) {
        armed = plant;
        if (hud != null) {
            hud.setArmed(plant);
        }
    }
}
