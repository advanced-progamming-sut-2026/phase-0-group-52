package model;

import model.entities.Cell;
import model.entities.CellType;
import model.entities.plants.Plant;
import model.entities.plants.PlantCombat;
import model.entities.plants.PlantFactory;
import model.entities.plants.Plants;
import model.entities.zombies.Zombie;
import model.entities.zombies.ZombieFactory;
import model.entities.zombies.Zombies;
import model.level.ConveyorBeltLevel;
import model.level.DeadLine;
import model.level.Level;
import model.level.LockedPlantsLevel;
import model.level.LoveYourPlants;
import model.level.NightOps;
import model.level.PlantWhatYouGet;
import model.level.SaveOurSeeds;
import model.level.TimedWar;

import java.util.ArrayList;
import java.util.Arrays;

public final class LevelBuilder {

    public static final int SPAWN_COLUMN = GameField.COLS + 2;

    private static int difficultyOf(App app) {
        if (app == null || app.getCurrentuser() == null) {
            return model.level.WavePlan.NORMAL_DIFFICULTY;
        }
        return app.getCurrentuser().getDifficultyLevel();
    }

    private static model.level.SpecialLevel specialOf(App app) {
        return app == null ? null : model.level.SpecialLevel.byKey(app.getPendingSpecial());
    }


    private LevelBuilder() {}

    private static void carryPlantFood(model.App app, Game game) {
        if (app != null && app.getLoggedInUser() != null) {
            game.setPlantFoodCount(app.getLoggedInUser().getPlantFood());
        }
    }

    public static Game build(App app, ChapterType chapter, int levelNumber) {
        GameField field = new GameField(chapter);
        applyTerrain(field, chapter);
        Game game = new Game(app, null, null, field, 50, new ArrayList<Plant>(), buildWaves(chapter, levelNumber,
                        difficultyOf(app), specialOf(app)));
        carryPlantFood(app, game);
        if (app != null) game.getChosenPlants().addAll(app.getPlantSelection());
        game.setLevel(levelNumber >= ChapterType.LEVELS_PER_CHAPTER
                ? new model.level.ZombossLevel(levelNumber, chapter)
                : new model.level.HomeDefense(levelNumber, chapter, null, null));
        return game;
    }

    public static String specialTypes() {
        return model.level.SpecialLevel.keys();
    }

    public static Game buildSpecial(App app, ChapterType chapter, int levelNumber, String special) {
        GameField field = new GameField(chapter);
        applyTerrain(field, chapter);
        int startSun = 150;
        Game game = new Game(app, null, null, field, startSun, new ArrayList<Plant>(), buildWaves(chapter, levelNumber,
                        difficultyOf(app), specialOf(app)));
        carryPlantFood(app, game);
        if (app != null) game.getChosenPlants().addAll(app.getPlantSelection());
        ArrayList<Plants> pool = new ArrayList<Plants>(Arrays.asList(
                Plants.PEASHOOTER, Plants.SUNFLOWER, Plants.WALL_NUT, Plants.SNOW_PEA, Plants.CHERRY_BOMB));
        String key = special.toLowerCase().replace("-", "").replace("_", "");
        Level level;
        switch (key) {
            case "conveyor": case "conveyorbelt":
                level = new ConveyorBeltLevel(levelNumber, chapter, pool, null);break;
            case "plantwhatyouget": case "pwyg":
                ArrayList<Plants> pwygPool = new ArrayList<Plants>(Arrays.asList(
                        Plants.PEASHOOTER, Plants.SUNFLOWER, Plants.WALL_NUT, Plants.SNOW_PEA,
                        Plants.CHERRY_BOMB, Plants.REPEATER, Plants.SQUASH, Plants.POTATO_MINE));
                game.setSunAmount(0);
                level = new PlantWhatYouGet(levelNumber, chapter, pwygPool, null);break;
            case "lockedplants": case "locked":
                LockedPlantsLevel locked = new LockedPlantsLevel(levelNumber, chapter, null, null);
                if (app != null && !app.getLockedPlants().isEmpty())
                    for (Plants lp : app.getLockedPlants()) locked.lockPlant(lp);
                else { locked.lockPlant(Plants.CHERRY_BOMB);
                    locked.lockPlant(Plants.JALAPENO); locked.lockPlant(Plants.SQUASH);}
                level = locked;break;
            case "saveourseeds": case "sos":
                SaveOurSeeds sos = new SaveOurSeeds(levelNumber, chapter, null, null);
                for (int r = 0; r < field.getRows(); r++) {
                    Plant guard = PlantFactory.create(Plants.SUNFLOWER, new Vec2(0, r));
                    Cell cell = field.getCell(0, r);
                    if (cell != null) cell.getPlants().add(guard);
                    game.getPlants().add(guard);
                    sos.protectPlant(guard);}
                level = sos;break;
            case "deadline":
                level = new DeadLine(levelNumber, chapter, null, null);break;
            case "loveyourplants": case "love":
                level = new LoveYourPlants(levelNumber, chapter, null, null);break;
            case "nightops": case "night":
                level = new NightOps(levelNumber, chapter, null, null);break;
            case "timedwar": case "timed":
                TimedWar tw = new TimedWar(levelNumber, chapter, null, null);
                tw.setDuration(60 * model.Game.TICKS_PER_SECOND);
                tw.setTargetKills(5 + 3 * levelNumber);
                level = tw;break;
            default: return null;}
        game.setLevel(level);return game;
    }

    public static final int MINIGAME_SUN = 150;
    public static final int IZOMBIE_WAVES = 0;
    public static final int SCORE_TARGET_BASE = 400;
    public static final int SCORE_TARGET_STEP = 250;

    public static Game buildMinigame(App app, minigame.MinigameType kind, int levelNumber) {
        if (kind == null || !kind.isLawnBased()) {
            return null;
        }
        ChapterType chapter = chapterFor(kind);
        GameField field = new GameField(chapter);
        applyTerrain(field, chapter);
        ArrayList<Wave> waves = kind == minigame.MinigameType.I_ZOMBIE
                || kind == minigame.MinigameType.VASE_BREAKER
                ? new ArrayList<Wave>()
                : buildWaves(chapter, levelNumber, difficultyOf(app), null);
        Game game = new Game(app, null, null, field,
                startSunFor(kind), new ArrayList<Plant>(), waves);
        game.setApp(app);
        carryPlantFood(app, game);
        game.setLevel(levelFor(kind, chapter, levelNumber));
        stockDeck(app, game, kind);
        return game;
    }

    private static ChapterType chapterFor(minigame.MinigameType kind) {
        switch (kind) {
            case WALLNUT_BOWLING: return ChapterType.BIG_WAVE_BEACH;
            case I_ZOMBIE:        return ChapterType.DARK_AGES;
            case VASE_BREAKER:    return ChapterType.ANCIENT_EGYPT;
            case SCORE:           return ChapterType.FROSTBITE_CAVES;
            default:              return ChapterType.ANCIENT_EGYPT;
        }
    }

    private static int startSunFor(minigame.MinigameType kind) {
        if (kind == minigame.MinigameType.I_ZOMBIE) {
            return model.level.IZombieLevel.START_BRAINS;
        }
        return kind == minigame.MinigameType.WALLNUT_BOWLING
                || kind == minigame.MinigameType.VASE_BREAKER ? 0 : MINIGAME_SUN;
    }

    private static model.level.Level levelFor(minigame.MinigameType kind,
            ChapterType chapter, int levelNumber) {
        switch (kind) {
            case ZOMBOTANY:
                return new model.level.ZombotanyLevel(levelNumber, chapter, null);
            case WALLNUT_BOWLING:
                return new model.level.WallNutBowlingLevel(levelNumber, chapter, null);
            case I_ZOMBIE:
                return new model.level.IZombieLevel(levelNumber, chapter, null);
            case VASE_BREAKER:
                return new model.level.VasebreakerLevel(levelNumber, chapter, null);
            case SCORE:
                return new model.level.ScoreAttackLevel(levelNumber, chapter, null,
                        SCORE_TARGET_BASE + SCORE_TARGET_STEP * Math.max(0, levelNumber - 1));
            default:
                return new model.level.HomeDefense(levelNumber, chapter, null, null);
        }
    }

    private static void stockDeck(App app, Game game, minigame.MinigameType kind) {
        if (kind == minigame.MinigameType.I_ZOMBIE
                || kind == minigame.MinigameType.WALLNUT_BOWLING
                || kind == minigame.MinigameType.VASE_BREAKER) {
            return;
        }
        if (app != null && !app.getPlantSelection().isEmpty()) {
            game.getChosenPlants().addAll(app.getPlantSelection());
            return;
        }
        game.getChosenPlants().addAll(Arrays.asList(Plants.SUNFLOWER, Plants.PEASHOOTER,
                Plants.WALL_NUT, Plants.SNOW_PEA, Plants.CHERRY_BOMB, Plants.POTATO_MINE));
    }

    private static void applyTerrain(GameField field, ChapterType chapter) {
        switch (chapter) {
            case ANCIENT_EGYPT:
            case DARK_AGES:
                break;
            case BIG_WAVE_BEACH:
                for (int r = 0; r < field.getRows(); r++) {
                    Cell cell = field.getCell(field.getCols() - 1, r);
                    if (cell != null) cell.setType(CellType.WATER);
                }
                break;
            case FROSTBITE_CAVES:
                seedIce(field);
                break;
            default:
                break;
        }
    }

    private static void seedIce(model.GameField field) {
        int patches = 2 + PlantCombat.RANDOM.nextInt(3);
        for (int i = 0; i < patches; i++) {
            int row = PlantCombat.RANDOM.nextInt(field.getRows());
            int column = 2 + PlantCombat.RANDOM.nextInt(Math.max(1, field.getCols() - 3));
            Cell cell = field.getCell(column, row);
            if (cell == null || cell.getType() != CellType.NORMAL) {
                continue;
            }
            boolean canRise = row > 0;
            boolean canFall = row < field.getRows() - 1;
            if (!canRise && !canFall) {
                continue;
            }
            boolean rising = canRise && (!canFall || PlantCombat.RANDOM.nextBoolean());
            cell.setType(rising ? CellType.SLIPPERY_UP : CellType.SLIPPERY_DOWN);
        }
    }

    public static java.util.List<Zombies> firstWave(ChapterType chapter, int levelNumber) {
        java.util.List<Zombies> preview = new ArrayList<Zombies>();
        Zombies[] pool = (levelNumber <= 1)
                ? new Zombies[]{ Zombies.ZOMBIE_DEFAULT }
                : poolFor(chapter);
        java.util.Random pick = new java.util.Random(
                (chapter == null ? 0 : chapter.ordinal()) * 131L + levelNumber);
        int count = 1 + levelNumber / 2;
        for (int i = 0; i < count; i++) {
            preview.add(pool[pick.nextInt(pool.length)]);
        }
        return preview;
    }

    private static ArrayList<Wave> buildWaves(ChapterType chapter, int levelNumber,
            int difficulty, model.level.SpecialLevel special) {
        ArrayList<Wave> waves = new ArrayList<Wave>();
        int count = model.level.WavePlan.waveCount(levelNumber, special);
        java.util.List<Zombies> roster = model.level.WavePlan.restrict(
                model.level.WavePlan.roster(chapter, levelNumber), special);
        java.util.Random random = new java.util.Random(
                (chapter == null ? 0 : chapter.ordinal()) * 977L + levelNumber * 31L + difficulty);
        for (int w = 0; w < count; w++) {
            double budget = model.level.WavePlan.budget(levelNumber, difficulty, w,
                    count, special);
            ArrayList<Zombie> zs = new ArrayList<Zombie>();
            for (Zombies type : model.level.WavePlan.compose(roster, budget, random)) {
                int row = random.nextInt(GameField.ROWS);
                zs.add(ZombieFactory.create(type, row, SPAWN_COLUMN, chapter));
            }
            markFoodCarrier(zs, w, random);
            waves.add(new Wave(zs, w + 1, 0));
        }
        return waves;
    }

    public static final int FIRST_FOOD_WAVE = 1;

    private static void markFoodCarrier(ArrayList<Zombie> wave, int waveIndex,
            java.util.Random random) {
        if (waveIndex < FIRST_FOOD_WAVE || wave.isEmpty()) {
            return;
        }
        wave.get(random.nextInt(wave.size())).setCarriesPlantFood(true);
    }

    private static Zombies[] poolFor(ChapterType chapter) {
        switch (chapter) {
            case ANCIENT_EGYPT:
                return new Zombies[]{ Zombies.ZOMBIE_DEFAULT, Zombies.ZOMBIE_ARMOR1,
                        Zombies.ZOMBIE_RA, Zombies.ZOMBIE_EXPLORER, Zombies.ZOMBIE_TOMB_RAISER };
            case FROSTBITE_CAVES:
                return new Zombies[]{ Zombies.ZOMBIE_DEFAULT, Zombies.ZOMBIE_ICE_AGE_DODO,
                        Zombies.ZOMBIE_ICE_AGE_HUNTER, Zombies.ZOMBIE_ICE_AGE_TROGLOBITE };
            case BIG_WAVE_BEACH:
                return new Zombies[]{ Zombies.ZOMBIE_DEFAULT, Zombies.ZOMBIE_BEACH_FISHERMAN,
                        Zombies.ZOMBIE_BEACH_OCTOPUS, Zombies.ZOMBIE_BEACH_SNORKEL };
            case DARK_AGES:
                return new Zombies[]{ Zombies.ZOMBIE_DARK_ARMOR3, Zombies.ZOMBIE_DARK_JUGGLER,
                        Zombies.ZOMBIE_WIZARD, Zombies.ZOMBIE_DARK_IMP_DRAGON };
            default:
                return new Zombies[]{ Zombies.ZOMBIE_DEFAULT, Zombies.ZOMBIE_ARMOR1, Zombies.ZOMBIE_ARMOR2 };
        }
    }
}
