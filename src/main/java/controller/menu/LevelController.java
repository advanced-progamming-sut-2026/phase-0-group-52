package controller.menu;

import model.App;
import model.ChapterType;
import model.Game;
import model.GameField;
import model.LevelBuilder;
import model.Result;
import model.Vec2;
import model.entities.Cell;
import model.entities.Sun;
import model.entities.plants.Plant;
import model.entities.plants.PlantData;
import model.entities.plants.PlantFactory;
import model.entities.plants.Plants;
import model.enums.MenuType;

import java.util.ArrayList;
import java.util.List;

public class LevelController {

    public static final int SUN_VALUE = 25;

    private final App app;


    private static final int MAX_CATCHUP = 8;

    private model.GameLoop loop;
    private double threatAtStart;
    private float carry;
    private float speed = 1f;
    private boolean paused;
    private boolean won;
    private String outcome;
    private String special;

    public LevelController(App app) {
        this.app = app;
    }

    public Result start() {
        if (resumeIfSuspended()) {
            return new Result(true, "Level resumed.", null);
        }
        ChapterType chapter = app.getSelectedChapter();
        if (chapter == null) {
            return new Result(false, "Enter a chapter first.", null);
        }
        if (app.getPlantSelection().isEmpty() && !suppliesOwnPlants()) {
            return new Result(false, "Choose at least one plant.", null);
        }
        reset();
        special = app.getPendingSpecial();
        Game game = special == null
                ? LevelBuilder.build(app, chapter, app.getSelectedLevel())
                : LevelBuilder.buildSpecial(app, chapter, app.getSelectedLevel(), special);
        if (game == null) {
            special = null;
            game = LevelBuilder.build(app, chapter, app.getSelectedLevel());
        }
        game.setApp(app);
        app.setGame(game);
        app.setPendingSpecial(null);
        resetMowers(game);
        app.setCurrentmenu(MenuType.GAME_MENU);
        return new Result(true, "Level started.", null);
    }

    private boolean suppliesOwnPlants() {
        model.level.SpecialLevel kind =
                model.level.SpecialLevel.byKey(app.getPendingSpecial());
        return kind == model.level.SpecialLevel.CONVEYOR
                || kind == model.level.SpecialLevel.PLANT_WHAT_YOU_GET;
    }

    public boolean isConveyor() {
        Game game = game();
        return game != null && game.getLevel() instanceof model.level.ConveyorBeltLevel;
    }

    public List<Plants> belt() {
        Game game = game();
        if (game == null || !(game.getLevel() instanceof model.level.ConveyorBeltLevel)) {
            return new ArrayList<Plants>();
        }
        return new ArrayList<Plants>(
                ((model.level.ConveyorBeltLevel) game.getLevel()).getBelt());
    }

    public int deadlineColumn() {
        Game game = game();
        return game != null && game.getLevel() instanceof model.level.DeadLine
                ? ((model.level.DeadLine) game.getLevel()).getDeadlineCol() : -1;
    }

    public List<Plant> guarded() {
        Game game = game();
        if (game == null || !(game.getLevel() instanceof model.level.SaveOurSeeds)) {
            return new ArrayList<Plant>();
        }
        return ((model.level.SaveOurSeeds) game.getLevel()).getProtectedPlants();
    }

    public double secondsLeft() {
        Game game = game();
        if (game == null || !(game.getLevel() instanceof model.level.TimedWar)) {
            return -1d;
        }
        return ((model.level.TimedWar) game.getLevel()).getRemainingTime()
                / Game.TICKS_PER_SECOND;
    }

    public int plantsLost() {
        Game game = game();
        return game != null && game.getLevel() instanceof model.level.LoveYourPlants
                ? ((model.level.LoveYourPlants) game.getLevel()).getLostPlants() : -1;
    }

    public int plantsAllowedToLose() {
        Game game = game();
        return game != null && game.getLevel() instanceof model.level.LoveYourPlants
                ? ((model.level.LoveYourPlants) game.getLevel()).getMaxLostPlants() : -1;
    }

    public boolean wavesHeld() {
        Game game = game();
        return game != null && game.getLevel() != null && game.getLevel().areWavesHeld();
    }

    public Result releaseWaves() {
        Game game = game();
        if (game == null || !(game.getLevel() instanceof model.level.PlantWhatYouGet)) {
            return new Result(false, "Nothing to start.", null);
        }
        ((model.level.PlantWhatYouGet) game.getLevel()).startWaves(game.getCurrentTick());
        return new Result(true, "Here they come!", null);
    }

    public Game game() {
        return app.getGame();
    }

    public ChapterType chapter() {
        Game game = game();
        if (game != null && game.getField() != null
                && game.getField().getChapter() != null) {
            return game.getField().getChapter();
        }
        return app.getSelectedChapter();
    }

    public int levelNumber() {
        return app.getSelectedLevel();
    }

    public int sun() {
        Game game = game();
        return game == null ? 0 : game.getSunAmount();
    }

    public List<Plants> bank() {
        Game game = game();
        return game == null ? new ArrayList<Plants>() : game.getChosenPlants();
    }

    public String objective() {
        Game game = game();
        return game == null || game.getLevel() == null
                ? "Do not let the zombies reach your house."
                : game.getLevel().objective();
    }

    public String objectiveTag() {
        Game game = game();
        return game == null || game.getLevel() == null
                ? "DEFEND THE HOUSE" : game.getLevel().objectiveTag();
    }

    public Result feed(int column, int row) {
        Game game = game();
        if (game == null) {
            return new Result(false, "No level running.", null);
        }
        if (game.getPlantFoodCount() <= 0) {
            return new Result(false, "No plant food.", null);
        }
        for (Plant plant : game.getPlants()) {
            if ((int) plant.getPosition().x == column && (int) plant.getPosition().y == row) {
                game.setPlantFoodCount(game.getPlantFoodCount() - 1);
                plant.onPlantFood(game);
                return new Result(true, plant.getType().getName() + " is supercharged!", plant);
            }
        }
        return new Result(false, "No plant there.", null);
    }

    public java.util.List<Integer> takeChilledRows() {
        Game game = game();
        if (game == null || game.getGusts().isEmpty()) {
            return new java.util.ArrayList<Integer>();
        }
        java.util.List<Integer> rows =
                new java.util.ArrayList<Integer>(game.getGusts());
        game.getGusts().clear();
        return rows;
    }

    public java.util.List<model.entities.Projectile> projectiles() {
        Game game = game();
        return game == null
                ? new java.util.ArrayList<model.entities.Projectile>()
                : game.getProjectiles();
    }

    public String takeDrop() {
        Game game = game();
        return game == null ? null : game.takeDrop();
    }

    public boolean consumeStorm() {
        Game game = game();
        if (game == null || !game.isStormPending()) {
            return false;
        }
        game.setStormPending(false);
        return true;
    }

    public int plantFood() {
        Game game = game();
        model.User user = app == null ? null : app.getLoggedInUser();
        if (user != null && game != null) {
            user.setPlantFood(game.getPlantFoodCount());
        }
        return game == null ? 0 : game.getPlantFoodCount();
    }

    public int plantFoodSlots() {
        return Game.MAX_PLANT_FOOD;
    }

    public void grantPlantFood(int amount) {
        Game game = game();
        if (game != null) {
            game.setPlantFoodCount(game.getPlantFoodCount() + amount);
        }
    }

    public void grantSun(int amount) {
        Game game = game();
        if (game != null) {
            game.setSunAmount(game.getSunAmount() + amount);
        }
    }

    public int waveCount() {
        Game game = game();
        return game == null || game.getWaves() == null ? 0 : game.getWaves().size();
    }

    public int currentWave() {
        Game game = game();
        return game == null ? 0 : game.getCurrentWaveIndex();
    }

    public double threatRemaining() {
        Game game = game();
        if (game == null) {
            return 0d;
        }
        double total = 0d;
        for (model.entities.zombies.Zombie zombie : game.getZombies()) {
            total += Math.max(0d, zombie.getHp());
        }
        java.util.List<model.Wave> waves = game.getWaves();
        if (waves != null) {
            for (int i = Math.max(0, game.getCurrentWaveIndex() + 1); i < waves.size(); i++) {
                for (model.entities.zombies.Zombie zombie : waves.get(i).getZombies()) {
                    total += Math.max(0d, zombie.getHp());
                }
            }
        }
        return total;
    }

    public double threatTotal() {
        if (threatAtStart <= 0d) {
            threatAtStart = threatRemaining();
        }
        return threatAtStart;
    }

    public float threatProgress() {
        double total = threatTotal();
        if (total <= 0d) {
            return 0f;
        }
        return Math.max(0f, Math.min(1f, (float) (1d - threatRemaining() / total)));
    }

    public float waveProgress() {
        int total = waveCount();
        return total <= 0 ? 0f : Math.min(1f, currentWave() / (float) total);
    }

    public java.util.List<model.entities.zombies.Zombie> zombies() {
        Game game = game();
        return game == null ? new java.util.ArrayList<model.entities.zombies.Zombie>()
                : game.getZombies();
    }

    public int nuke() {
        Game game = game();
        if (game == null) {
            return 0;
        }
        int killed = game.getZombies().size();
        for (model.entities.zombies.Zombie zombie : game.getZombies()) {
            zombie.setHp(0);
        }
        model.entities.plants.PlantCombat.removeDeadZombies(game);
        return killed;
    }

    public List<Sun> loose() {
        Game game = game();
        return game == null ? new ArrayList<Sun>() : game.getSuns();
    }

    public List<Plant> planted() {
        Game game = game();
        return game == null ? new ArrayList<Plant>() : game.getPlants();
    }

    public int rows() {
        Game game = game();
        return game == null ? GameField.ROWS : game.getField().getRows();
    }

    public int columns() {
        Game game = game();
        return game == null ? GameField.COLS : game.getField().getCols();
    }

    public boolean isOnCooldown(Plants type) {
        Game game = game();
        return game != null && game.isOnCooldown(type);
    }

    public double cooldownLeft(Plants type) {
        Game game = game();
        return game == null ? 0d : game.getRemainingCooldown(type);
    }

    public boolean canAfford(Plants type) {
        return sun() >= type.getCost();
    }

    private String whyBlocked(int column, int row) {
        Game game = game();
        model.entities.Cell cell = game == null || game.getField() == null
                ? null : game.getField().getCell(column, row);
        if (cell == null) {
            return "That square is off the lawn.";
        }
        if (cell.getType() == model.entities.CellType.TOMBSTONE) {
            return "A tombstone blocks that square - use a Grave Buster.";
        }
        if (cell.getType().isWater()) {
            return "That square is water.";
        }
        if (!cell.isEmpty()) {
            return "There is already a plant there.";
        }
        return "You cannot plant on that square.";
    }

    public boolean isFree(int column, int row) {
        Game game = game();
        if (game == null) {
            return false;
        }
        Cell cell = game.getField().getCell(column, row);
        return cell != null && cell.isPlantable() && cell.isEmpty();
    }

    public Result plant(Plants type, int column, int row) {
        Game game = game();
        if (game == null || type == null) {
            return new Result(false, "No level is running.", null);
        }
        if (!isFree(column, row)) {
            return new Result(false, whyBlocked(column, row), null);
        }
        if (game.getLevel() != null && !game.getLevel().isPlantAllowed(type)) {
            return new Result(false, type.getName() + " is locked in this level.", null);
        }
        boolean fromBelt = game.getLevel() instanceof model.level.ConveyorBeltLevel;
        if (fromBelt && !((model.level.ConveyorBeltLevel) game.getLevel()).hasOnBelt(type)) {
            return new Result(false, "That plant is not on the belt.", null);
        }
        if (!fromBelt && game.isOnCooldown(type)) {
            return new Result(false, type.getName() + " is recharging.", null);
        }
        if (!fromBelt && !game.spendSun(type.getCost())) {
            return new Result(false, "Not enough sun.", null);
        }
        model.entities.Cell target = game.getField().getCell(column, row);
        if (target != null && !target.accepts(type)) {
            return new Result(false, target.getType().isWater()
                    ? "Water needs a Lily Pad first." : "That square is taken.", null);
        }
        Plant plant = PlantFactory.create(type, new Vec2(column, row));
        game.getField().getCell(column, row).getPlants().add(plant);
        game.getPlants().add(plant);
        plant.onPlanted(game);
        game.getStats().recordPlantPlanted(type, column, row);
        applyStoredBoost(game, plant, type);
        if (fromBelt) {
            ((model.level.ConveyorBeltLevel) game.getLevel()).takeFromBelt(type);
        } else {
            game.startCooldown(type);
        }
        award(type);
        return new Result(true, type.getName() + " planted.", type);
    }

    private void applyStoredBoost(Game game, Plant made, Plants type) {
        model.User user = app == null ? null : app.getLoggedInUser();
        if (user == null || !user.getStoredBoosts().remove(type)) {
            return;
        }
        made.boost();
        made.onPlantFood(game);
        new controller.SaveService().persist(user);
    }

    private void resetMowers(Game game) {
        if (game == null || game.getField() == null) {
            return;
        }
        for (model.entities.Lawnmower mower : game.getField().getLawnmowers()) {
            mower.reset();
        }
    }

    public boolean isBoosted(Plants type) {
        model.User user = app == null ? null : app.getLoggedInUser();
        return user != null && user.getStoredBoosts().contains(type);
    }

    public Result collect(Sun sun) {
        Game game = game();
        if (game == null || sun == null || !game.getSuns().remove(sun)) {
            return new Result(false, "That sun is gone.", null);
        }
        game.addSun(sun.getAmount());
        game.getStats().addSunCollected(sun.getAmount());
        return new Result(true, "+" + sun.getAmount() + " sun.", null);
    }

    public float speed() {
        return speed;
    }

    public void tick(float delta) {
        Game game = game();
        if (game == null || paused || outcome != null) {
            return;
        }
        if (loop == null) {
            loop = new model.GameLoop(game);
        }
        carry += delta * speed;
        int budget = MAX_CATCHUP;
        while (carry >= Game.SECONDS_PER_TICK && budget-- > 0) {
            carry -= Game.SECONDS_PER_TICK;
            String ended = loop.step(game);
            if (ended != null) {
                outcome = ended;
                won = game.isWon();
                new controller.LevelEndService().finish(app, game);
                return;
            }
        }
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean value) {
        paused = value;
    }

    public String outcome() {
        return outcome;
    }

    public boolean hasWon() {
        return won;
    }

    public void setSpeed(float value) {
        speed = Math.max(0.25f, value);
    }

    public Result restart() {
        java.util.List<Plants> deck = new ArrayList<Plants>(
                app.getGame() == null ? app.getPlantSelection()
                        : app.getGame().getChosenPlants());
        String again = special;
        app.setSuspendedGame(null);
        app.setGame(null);
        reset();
        app.setPendingSpecial(again);
        if (app.getPlantSelection().isEmpty() && !deck.isEmpty()) {
            app.getPlantSelection().addAll(deck);
        }
        return start();
    }

    public model.level.SpecialLevel special() {
        return model.level.SpecialLevel.byKey(special);
    }

    protected void resetRun() {
        reset();
    }

    private void reset() {
        loop = null;
        special = null;
        threatAtStart = 0d;
        carry = 0f;
        outcome = null;
        won = false;
        paused = false;
    }

    public Result dig(int column, int row) {
        Game game = game();
        if (game == null) {
            return new Result(false, "No level running.", null);
        }
        for (model.entities.plants.Plant plant
                : new java.util.ArrayList<model.entities.plants.Plant>(game.getPlants())) {
            if ((int) plant.getPosition().x == column && (int) plant.getPosition().y == row) {
                model.entities.plants.PlantCombat.removePlant(game, plant);
                return new Result(true, "Dug up " + plant.getType().getName() + ".", null);
            }
        }
        return new Result(false, "Nothing planted there.", null);
    }

    public Result suspend() {
        Game game = game();
        if (game != null) {
            app.setSuspendedGame(game);
        }
        app.setCurrentmenu(MenuType.CHAPTER_MENU);
        return new Result(true, "Level saved.", null);
    }

    public boolean resumeIfSuspended() {
        Game saved = app.getSuspendedGame();
        if (saved == null) {
            return false;
        }
        app.setSuspendedGame(null);
        app.setGame(saved);
        loop = null;
        carry = 0f;
        outcome = null;
        won = false;
        paused = false;
        return true;
    }

    public Result leave() {
        app.setSuspendedGame(null);
        app.setGame(null);
        new controller.LevelEndService().clearDeck(app);
        reset();
        app.setCurrentmenu(MenuType.CHAPTER_MENU);
        return new Result(true, "Left the level.", null);
    }

    private void award(Plants type) {
        model.User user = app.getCurrentuser();
        if (user == null) {
            return;
        }
        user.getPlants().addXp(type, 1);
        PlantData.record(type);
    }
}
