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
import model.level.*;

import java.util.ArrayList;
import java.util.Arrays;

public final class LevelBuilder {

    private LevelBuilder() {
    }

    public static Game build(App app, ChapterType chapter, int levelNumber) {
        GameField field = new GameField(chapter);
        applyTerrain(field, chapter);
        Game game = new Game(app, null, null, field, 50, new ArrayList<Plant>(), buildWaves(chapter, levelNumber));
        if (app != null) game.getChosenPlants().addAll(app.getPlantSelection());
        return game;
    }

    public static String specialTypes() {
        return "conveyor, plant_what_you_get, locked_plants, save_our_seeds, deadline, love_your_plants, night_ops, timed_war";
    }

    public static Game buildSpecial(App app, ChapterType chapter, int levelNumber, String special) {
        GameField field = new GameField(chapter);
        applyTerrain(field, chapter);
        int startSun = 150;
        Game game = new Game(app, null, null, field, startSun, new ArrayList<Plant>(), buildWaves(chapter, levelNumber));
        if (app != null) game.getChosenPlants().addAll(app.getPlantSelection());
        ArrayList<Plants> pool = new ArrayList<Plants>(Arrays.asList(
                Plants.PEASHOOTER, Plants.SUNFLOWER, Plants.WALL_NUT, Plants.SNOW_PEA, Plants.CHERRY_BOMB));
        String key = special.toLowerCase().replace("-", "").replace("_", "");
        Level level;
        switch (key) {
            case "conveyor":
            case "conveyorbelt":
                level = new ConveyorBeltLevel(levelNumber, chapter, pool, null);
                break;
            case "plantwhatyouget":
            case "pwyg":
                PlantWhatYouGet pwyg = new PlantWhatYouGet(levelNumber, chapter, null, null);
                game.setSunAmount(pwyg.getStartingSun());
                level = pwyg;
                break;
            case "lockedplants":
            case "locked":
                LockedPlantsLevel locked = new LockedPlantsLevel(levelNumber, chapter, null, null);
                locked.lockPlant(Plants.CHERRY_BOMB);
                locked.lockPlant(Plants.JALAPENO);
                locked.lockPlant(Plants.SQUASH);
                level = locked;
                break;
            case "saveourseeds":
            case "sos":
                SaveOurSeeds sos = new SaveOurSeeds(levelNumber, chapter, null, null);
                for (int r = 0; r < field.getRows(); r++) {
                    Plant guard = PlantFactory.create(Plants.SUNFLOWER, new Vec2(0, r));
                    Cell cell = field.getCell(0, r);
                    if (cell != null) cell.getPlants().add(guard);
                    game.getPlants().add(guard);
                    sos.protectPlant(guard);
                }
                level = sos;
                break;
            case "deadline":
                level = new DeadLine(levelNumber, chapter, null, null);
                break;
            case "loveyourplants":
            case "love":
                level = new LoveYourPlants(levelNumber, chapter, null, null);
                break;
            case "nightops":
            case "night":
                level = new NightOps(levelNumber, chapter, null, null);
                break;
            case "timedwar":
            case "timed":
                TimedWar tw = new TimedWar(levelNumber, chapter, null, null);
                tw.setDuration(60);
                tw.setTargetKills(5 + 3 * levelNumber);
                level = tw;
                break;
            default:
                return null;
        }
        game.setLevel(level);
        return game;
    }

    private static void applyTerrain(GameField field, ChapterType chapter) {
        switch (chapter) {
            case ANCIENT_EGYPT:
            case DARK_AGES:
                for (int i = 0; i < 3; i++) {
                    int r = PlantCombat.RANDOM.nextInt(field.getRows());
                    int c = 4 + PlantCombat.RANDOM.nextInt(field.getCols() - 4);
                    Cell cell = field.getCell(c, r);
                    if (cell != null) cell.setType(CellType.TOMBSTONE);
                }
                break;
            case BIG_WAVE_BEACH:
                for (int r = 0; r < field.getRows(); r++) {
                    Cell cell = field.getCell(field.getCols() - 1, r);
                    if (cell != null) cell.setType(CellType.WATER);
                }
                break;
            case FROSTBITE_CAVES:
                int rr = PlantCombat.RANDOM.nextInt(field.getRows());
                Cell cell = field.getCell(field.getCols() / 2, rr);
                if (cell != null) cell.setType(CellType.SLIPPERY_DOWN);
                break;
            default:
                break;
        }
    }

    private static ArrayList<Wave> buildWaves(ChapterType chapter, int levelNumber) {
        ArrayList<Wave> waves = new ArrayList<Wave>();
        int waveCount = 3 + levelNumber;
        Zombies[] pool = (levelNumber <= 1)
                ? new Zombies[]{Zombies.ZOMBIE_DEFAULT}
                : poolFor(chapter);
        for (int w = 0; w < waveCount; w++) {
            ArrayList<Zombie> zs = new ArrayList<Zombie>();
            int n = 1 + (w / 2) + levelNumber / 2;
            for (int i = 0; i < n; i++) {
                Zombies data = pool[PlantCombat.RANDOM.nextInt(pool.length)];
                int row = PlantCombat.RANDOM.nextInt(GameField.ROWS);
                zs.add(ZombieFactory.create(data, row, GameField.COLS, chapter));
            }
            waves.add(new Wave(zs, w + 1, 0));
        }
        return waves;
    }

    private static Zombies[] poolFor(ChapterType chapter) {
        switch (chapter) {
            case ANCIENT_EGYPT:
                return new Zombies[]{Zombies.ZOMBIE_DEFAULT, Zombies.ZOMBIE_ARMOR1,
                        Zombies.ZOMBIE_RA, Zombies.ZOMBIE_EXPLORER, Zombies.ZOMBIE_TOMB_RAISER};
            case FROSTBITE_CAVES:
                return new Zombies[]{Zombies.ZOMBIE_DEFAULT, Zombies.ZOMBIE_ICE_AGE_DODO,
                        Zombies.ZOMBIE_ICE_AGE_HUNTER, Zombies.ZOMBIE_ICE_AGE_TROGLOBITE};
            case BIG_WAVE_BEACH:
                return new Zombies[]{Zombies.ZOMBIE_DEFAULT, Zombies.ZOMBIE_BEACH_FISHERMAN,
                        Zombies.ZOMBIE_BEACH_OCTOPUS, Zombies.ZOMBIE_BEACH_SNORKEL};
            case DARK_AGES:
                return new Zombies[]{Zombies.ZOMBIE_DARK_ARMOR3, Zombies.ZOMBIE_DARK_JUGGLER,
                        Zombies.ZOMBIE_WIZARD, Zombies.ZOMBIE_DARK_IMP_DRAGON};
            default:
                return new Zombies[]{Zombies.ZOMBIE_DEFAULT, Zombies.ZOMBIE_ARMOR1, Zombies.ZOMBIE_ARMOR2};
        }
    }
}
