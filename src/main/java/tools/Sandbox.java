package tools;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;
import controller.menu.LevelController;
import model.App;
import model.ChapterType;
import model.Game;
import model.LevelBuilder;
import model.Vec2;
import model.entities.plants.Plant;
import model.entities.plants.PlantFactory;
import model.entities.plants.Plants;
import model.entities.zombies.Zombie;
import model.entities.zombies.ZombieFactory;
import model.entities.zombies.Zombies;
import view.gui.Assets;
import view.gui.EntityTuning;
import view.gui.LawnGeometry;
import view.gui.Theme;
import view.gui.UiKit;
import view.gui.widgets.LawnField;
import view.gui.widgets.LawnView;

public final class Sandbox extends ApplicationAdapter {

    private static final int START_SUN = 9999;

    private Assets assets;
    private UiKit ui;
    private Stage stage;
    private App app;
    private LevelController controller;
    private LawnField field;
    private Label readout;

    private int plantIndex;
    private int zombieIndex;
    private int row = 2;
    private int column = 2;
    private boolean running = true;
    private String blocked;

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("PvZ2 sandbox");
        config.setWindowedMode(Theme.WORLD_WIDTH, Theme.WORLD_HEIGHT);
        config.setForegroundFPS(60);
        new Lwjgl3Application(new Sandbox(), config);
    }

    @Override
    public void create() {
        assets = new Assets();
        ui = new UiKit(assets);
        stage = new Stage(new FitViewport(Theme.WORLD_WIDTH, Theme.WORLD_HEIGHT), assets.batch());
        Gdx.input.setInputProcessor(stage);
        readout = new Label("", ui.skin(), "onDark");

        app = App.getInstance();
        app.setSelectedChapter(ChapterType.ANCIENT_EGYPT);
        app.setSelectedLevel(1);
        controller = new LevelController(app);
        newGame();
        build();
    }

    private void newGame() {
        Game game = LevelBuilder.build(app, ChapterType.ANCIENT_EGYPT, 1);
        game.setApp(app);
        game.getWaves().clear();
        game.getZombies().clear();
        game.setSunAmount(START_SUN);
        game.setEndless(true);
        app.setGame(game);
    }

    private void build() {
        stage.clear();
        EntityTuning.applyGrid(ChapterType.ANCIENT_EGYPT.name());
        LawnView lawn = new LawnView(assets, ChapterType.ANCIENT_EGYPT);
        lawn.setCamera(0f);
        stage.addActor(lawn);

        field = new LawnField(ui, assets, controller);
        field.setFillParent(true);
        field.setShowGrid(true);
        stage.addActor(field);

        Table hud = new Table();
        hud.setFillParent(true);
        hud.top().left();
        hud.add(readout).left().pad(10f);
        stage.addActor(hud);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        input();
        if (running) {
            controller.tick(Gdx.graphics.getDeltaTime());
        }
        readout.setText(caption());
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    private String caption() {
        Game game = app.getGame();
        return "PLANT  " + Plants.values()[plantIndex].getName()
                + "      ZOMBIE  " + Zombies.values()[zombieIndex].name()
                + "\ncell (" + (column + 1) + ", " + (row + 1) + ")"
                + "   sun " + (game == null ? 0 : game.getSunAmount())
                + "   plants " + (game == null ? 0 : game.getPlants().size())
                + "   zombies " + (game == null ? 0 : game.getZombies().size())
                + "   " + (running ? "RUNNING" : "PAUSED")
                + "\n[/] plant   [;'] zombie   [WASD] cell"
                + "\n[P] plant   [Z] zombie   [F] feed cell   [B] boost all   [X] clear"
                + "\n[SPACE] pause   [C] clear cell   [R] reset lawn"
                + (blocked == null ? "" : "\n" + blocked);
    }

    private void input() {
        int plants = Plants.values().length;
        int zombies = Zombies.values().length;
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT_BRACKET)) {
            plantIndex = Math.floorMod(plantIndex - 1, plants);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT_BRACKET)) {
            plantIndex = Math.floorMod(plantIndex + 1, plants);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SEMICOLON)) {
            zombieIndex = Math.floorMod(zombieIndex - 1, zombies);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.APOSTROPHE)) {
            zombieIndex = Math.floorMod(zombieIndex + 1, zombies);
        }
        cell();
        actions();
    }

    private void cell() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            row = Math.floorMod(row - 1, LawnGeometry.ROWS);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            row = Math.floorMod(row + 1, LawnGeometry.ROWS);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            column = Math.floorMod(column - 1, LawnGeometry.COLUMNS);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            column = Math.floorMod(column + 1, LawnGeometry.COLUMNS);
        }
    }

    private void actions() {
        Game game = app.getGame();
        if (game == null) {
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            model.entities.Cell cell = game.getField().getCell(column, row);
            if (cell != null && cell.isPlantable()) {
                Plant plant = PlantFactory.create(Plants.values()[plantIndex],
                        new Vec2(column, row));
                cell.getPlants().add(plant);
                game.getPlants().add(plant);
                plant.onPlanted(game);
                blocked = null;
            } else {
                blocked = "That cell will not take another plant.";
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
            model.entities.Cell cell = game.getField().getCell(column, row);
            if (cell != null) {
                for (Plant plant : new java.util.ArrayList<Plant>(cell.getPlants())) {
                    model.entities.plants.PlantCombat.removePlant(game, plant);
                }
            }
        }
        spawnKeys(game);
    }

    private void spawnKeys(Game game) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
            Zombie zombie = ZombieFactory.create(Zombies.values()[zombieIndex], row,
                    LawnGeometry.COLUMNS, ChapterType.ANCIENT_EGYPT);
            game.getZombies().add(zombie);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            game.setPlantFoodCount(Game.MAX_PLANT_FOOD);
            model.entities.Cell cell = game.getField().getCell(column, row);
            if (cell == null || cell.getPlants().isEmpty()) {
                blocked = "No plant in that cell to feed.";
            } else {
                for (Plant plant : new java.util.ArrayList<Plant>(cell.getPlants())) {
                    plant.onPlantFood(game);
                }
                blocked = null;
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.B)) {
            for (Plant plant : new java.util.ArrayList<Plant>(game.getPlants())) {
                plant.boost();
            }
            blocked = "Every plant on the lawn is boosted.";
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
            game.getZombies().clear();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            running = !running;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            newGame();
            build();
        }
    }

    @Override
    public void dispose() {
        if (stage != null) {
            stage.dispose();
        }
        if (assets != null) {
            assets.dispose();
        }
    }
}
