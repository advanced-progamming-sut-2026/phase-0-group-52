package model;

import model.entities.Cell;
import model.entities.CellType;
import model.entities.Lawnmower;
import model.entities.Sun;
import model.entities.SunType;
import model.entities.plants.PlantTag;
import model.entities.plants.Explosive;
import model.entities.plants.Homing;
import model.entities.plants.Lobber;
import model.entities.plants.Melee;
import model.entities.plants.Mint;
import model.entities.plants.Modifier;
import model.entities.plants.Plant;
import model.entities.plants.Plants;
import model.entities.plants.Shooter;
import model.entities.plants.StrikeThrough;
import model.entities.plants.SunProducer;
import model.entities.plants.Wallnut;
import model.entities.zombies.Zombie;
import model.entities.zombies.ZombieFactory;
import model.entities.zombies.Zombies;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * قلب شبیه‌سازی بازی. کلِ حلقه‌ی گذر زمان، خورشید، موج، نبرد، برد/باخت و تقلب‌ها اینجاست.
 * موتور «محتوا-آگاه» نیست: در هر تیک فقط قلاب‌های polymorphic (onTick) را روی گیاه/زامبی صدا می‌زند.
 */
public class GameEngine {

    private final Game game;
    private final List<Zombies> zombiePool;   // زامبی‌های مجاز این مرحله
    private final double baseWaveBudget;       // بودجه‌ی موج اول
    private final int waveCount;               // تعداد کل موج‌ها
    private final ChapterType chapter;
    private final Random rng = new Random();

    private double lastWaveInitialHp = 0;      // برای شرط «۷۵٪ جانِ موج قبلی از بین رفته»

    public GameEngine(Game game, List<Zombies> zombiePool, double baseWaveBudget,
                      int waveCount, ChapterType chapter) {
        this.game = game;
        this.zombiePool = zombiePool;
        this.baseWaveBudget = baseWaveBudget;
        this.waveCount = waveCount;
        this.chapter = chapter;
    }

    /** آماده‌سازی اولیه؛ اولین سقوط خورشید را زمان‌بندی می‌کند. */
    public void start() {
        game.setNextSunDropTick(sunDropInterval(0));
    }

    // ======================================================================
    //  گذر زمان
    // ======================================================================

    public void advance(int ticks) {
        for (int i = 0; i < ticks && !game.isGameOver(); i++) {
            tick();
        }
        if (!game.isGameOver()) checkWin();
    }

    private void tick() {
        int t = game.getCurrentTick();

        decreaseCooldowns();
        updateFallingSuns();
        autoDropSun(t);

        for (Plant p : new ArrayList<>(game.getPlants())) p.onTick(game);
        maybeStartNextWave();
        for (Zombie z : new ArrayList<>(game.getZombies())) z.onTick(game);

        removeDeadPlants();
        removeDeadZombies();
        handleLawnmowersAndLoss();

        game.setCurrentTick(t + 1);
    }

    // ======================================================================
    //  خورشید
    // ======================================================================

    /** فاصله‌ی سقوط خورشید (تیک). فرمول داک برحسب ثانیه: max(6+0.05·t, 12)؛ چون ۱۰ تیک=۱ث تبدیل می‌شود. */
    private int sunDropInterval(int currentTick) {
        double tSec = Game.ticksToSeconds(currentTick);
        double xSec = Math.max(6 + 0.05 * tSec, 12);
        return Game.secondsToTicks(xSec);
    }

    private void autoDropSun(int t) {
        if (t < game.getNextSunDropTick()) return;

        int col = rng.nextInt(game.getField().getCols());
        int row = rng.nextInt(game.getField().getRows());
        SunType type = SunType.pickRandom(rng.nextDouble());
        game.getSuns().add(new Sun(type, new Vec2(col, row)));

        System.out.println("New " + type.name().toLowerCase()
            + " sun is dropping at position (" + col + ", " + row + ")");

        game.setNextSunDropTick(t + sunDropInterval(t));
    }

    private void updateFallingSuns() {
        for (Sun sun : game.getSuns()) {
            if (!sun.isFalling()) continue;
            sun.tickFall();
            if (!sun.isFalling()) {
                System.out.println("Sun reached the ground at position ("
                    + sun.getCol() + ", " + sun.getRow() + ")");
                // TODO: اگر رادیواکتیو بود، به خورشید معمولی تبدیل شود.
            }
        }
    }

    /** برداشت خورشید از یک خانه (دستور collect sun). */
    public boolean collectSun(int col, int row) {
        for (int i = 0; i < game.getSuns().size(); i++) {
            Sun sun = game.getSuns().get(i);
            if (sun.getCol() == col && sun.getRow() == row && !sun.isFalling()) {
                game.addSun(sun.getAmount());
                game.getSuns().remove(i);
                return true;
            }
        }
        return false;
    }

    // ======================================================================
    //  موج‌ها (weighted-random + بودجه)
    // ======================================================================

    private void maybeStartNextWave() {
        boolean firstWave = game.getCurrentWaveIndex() < 0;
        boolean prevMostlyDead = !firstWave && aliveZombieHp() <= 0.25 * lastWaveInitialHp;

        if ((firstWave || prevMostlyDead) && game.getCurrentWaveIndex() < waveCount - 1) {
            spawnWave(game.getCurrentWaveIndex() + 1);
        }
    }

    /** بودجه‌ی موج i: هر موج ۱.۲۵× قبلی؛ موج آخر (flag) دو برابر مرحله‌ی قبل. */
    private double waveBudget(int waveIndex) {
        if (waveIndex == waveCount - 1) {
            return baseWaveBudget * Math.pow(1.25, waveIndex - 1) * 2;
        }
        return baseWaveBudget * Math.pow(1.25, waveIndex);
    }

    private void spawnWave(int waveIndex) {
        game.setCurrentWaveIndex(waveIndex);
        int waveNumber = waveIndex + 1;

        if (waveIndex == waveCount - 1) System.out.println("The final wave has come.");
        else System.out.println("Wave " + waveNumber + " started.");

        double budget = waveBudget(waveIndex);
        double spawnedHp = 0;
        ArrayList<Zombie> waveZombies = new ArrayList<>();

        while (true) {
            Zombies pick = pickWeightedZombie(budget);
            if (pick == null) break;

            int[] spawn = pickSpawn();          // {col, lane} — لبه‌ی راست یا ساحلِ پست
            int spawnCol = spawn[0];
            int lane = spawn[1];
            Zombie z = createZombie(pick, lane, spawnCol);

            game.getZombies().add(z);
            waveZombies.add(z);
            budget -= pick.getWaveCost();
            spawnedHp += pick.getHp();

            System.out.println("Zombie " + pick.getName() + " spawned at wave " + waveNumber
                + " in lane " + lane + " which costed " + pick.getWaveCost() + ".");
        }

        lastWaveInitialHp = spawnedHp;
        game.getWaves().add(new Wave(waveZombies, waveNumber, 0));
    }

    /**
     * موقعیتِ ظهور: پیش‌فرض لبه‌ی راست؛ در ساحل با احتمال ۳۰٪ از یک خانه‌ی ساحلِ پست ظاهر می‌شود.
     * خروجی: {col, row}.
     */
    private int[] pickSpawn() {
        if (chapter == ChapterType.BIG_WAVE_BEACH) {
            List<int[]> low = game.getField().getLowGroundCells();
            if (!low.isEmpty() && rng.nextDouble() < 0.3) {
                int[] cell = low.get(rng.nextInt(low.size()));
                System.out.println("A zombie emerged from the low beach at ("
                    + cell[0] + ", " + cell[1] + ").");
                return cell;
            }
        }
        return new int[]{ game.getField().getCols(), rng.nextInt(game.getField().getRows()) };
    }

    /** انتخاب وزن‌دار فقط از بین زامبی‌های قابل‌خرید با بودجه‌ی باقی‌مانده. */
    private Zombies pickWeightedZombie(double budget) {
        if (zombiePool == null || zombiePool.isEmpty()) return null;
        int total = 0;
        for (Zombies z : zombiePool)
            if (z.getWaveCost() <= budget) total += z.getWeight();
        if (total <= 0) return null;
        int roll = rng.nextInt(total);
        for (Zombies z : zombiePool) {
            if (z.getWaveCost() > budget) continue;
            roll -= z.getWeight();
            if (roll < 0) return z;
        }
        return null;
    }

    /** ساختِ زامبی از طریق factory (زیرکلاس درست + مقداردهیِ زره). */
    private Zombie createZombie(Zombies data, int lane, int spawnCol) {
        return ZombieFactory.create(data, lane, spawnCol, chapter);
    }

    private double aliveZombieHp() {
        double sum = 0;
        for (Zombie z : game.getZombies()) sum += z.getHp();
        return sum;
    }

    // ======================================================================
    //  حذف مرده‌ها، چمن‌زن و باخت
    // ======================================================================

    private void removeDeadPlants() {
        game.getPlants().removeIf(p -> {
            if (p.isDead()) {
                Cell c = game.getField().getCell(p.getCol(), p.getRow());
                if (c != null) c.getPlants().remove(p);
                System.out.println("Plant " + p.getType().getName()
                    + " at (" + p.getCol() + ", " + p.getRow() + ") is destroyed.");
                return true;
            }
            return false;
        });
    }

    private void removeDeadZombies() {
        game.getZombies().removeIf(z -> {
            if (z.isDead()) {
                System.out.println("Zombie of type " + z.getClass().getSimpleName()
                    + " is dead at (" + z.getCol() + ", " + z.getRow() + ")");
                // TODO: drop سکه/الماس/گلدان و غذای گیاه (زامبی درخشان).
                return true;
            }
            return false;
        });
    }

    /** زامبی‌ای که از ستون ۰ رد شد: بارِ اول چمن‌زن، بارِ دوم باخت. */
    private void handleLawnmowersAndLoss() {
        for (Zombie z : new ArrayList<>(game.getZombies())) {
            if (z.getPosition().x >= 0) continue;

            Lawnmower mower = game.getField().getLawnmower(z.getRow());
            if (mower != null && mower.isIsactive()) {
                System.out.println("The lawn mower in the row " + z.getRow()
                    + " is triggered and killed these zombies:");
                final int lane = z.getRow();
                game.getZombies().removeIf(zz -> zz.getRow() == lane);
                mower.setIsactive(false);
                // TODO: زامبی‌های رئیس نباید کشته شوند.
            } else {
                System.out.println("The zombie ate your brain; LOSER!!!");
                game.setWon(false);
                game.setGameOver(true);
                return;
            }
        }
    }

    private void checkWin() {
        boolean allWavesSpawned = game.getCurrentWaveIndex() >= waveCount - 1;
        if (allWavesSpawned && game.getZombies().isEmpty()) {
            System.out.println("Dear humanz, zis is not done yet; we will come back to eat your brainz, humanz.");
            game.setWon(true);
            game.setGameOver(true);
        }
    }

    // ======================================================================
    //  کاشت / برداشت / غذا
    // ======================================================================

    /** کاشت گیاه با بررسی خانه (آب/خشکی)، cooldown و خورشید. */
    public String plantPlant(Plants type, int col, int row) {
        Cell cell = game.getField().getCell(col, row);
        if (cell == null) return "Invalid tile.";

        if (cell.getType() == CellType.WATER) {
            // روی آب فقط گیاهِ آبی/برگ؛ گیاهِ معمولی نیاز به برگِ زیرش دارد
            boolean aquatic = type.getTags().contains(PlantTag.WATER);
            boolean isLilyPad = (type == Plants.LILY_PAD);
            boolean hasLilyPad = cell.getPlants().stream().anyMatch(p -> p.getType() == Plants.LILY_PAD);
            if (!aquatic && !isLilyPad && !hasLilyPad) {
                return "This plant needs a lily pad on water.";
            }
            if (cell.getPlants().size() >= 2) return "You can't plant here.";
        } else if (!cell.isPlantable()) {
            return "You can't plant here.";
        }

        if (game.isOnCooldown(type)) return "Plant is on cooldown.";
        if (game.getSunAmount() < type.getCost()) return "Not enough sun.";

        Plant plant = createPlant(type, new Vec2(col, row));
        game.spendSun(type.getCost());
        game.getPlants().add(plant);
        cell.getPlants().add(plant);
        game.startCooldown(type);
        plant.onPlanted(game);
        if (game.getBoostedTypes().contains(type)) plant.onPlantFood(game);
        return "Planted " + type.getName() + " at (" + col + ", " + row + ").";
    }

    /** برداشتن گیاه از یک خانه. */
    public String pluckPlant(int col, int row) {
        Cell cell = game.getField().getCell(col, row);
        if (cell == null || cell.getPlants().isEmpty()) return "No plant here.";
        Plant p = cell.getPlants().remove(cell.getPlants().size() - 1);
        game.getPlants().remove(p);
        return "Plucked.";
    }

    /** استفاده از غذای گیاه روی خانه. */
    public String feedPlant(int col, int row) {
        Cell cell = game.getField().getCell(col, row);
        if (cell == null || cell.getPlants().isEmpty()) return "No plant here.";
        if (game.getPlantFoodCount() <= 0) return "No plant food.";
        Plant p = cell.getPlants().get(cell.getPlants().size() - 1);
        game.setPlantFoodCount(game.getPlantFoodCount() - 1);
        p.onPlantFood(game);
        return "Fed plant.";
    }

    /** ساختِ گیاه بر اساس دسته‌بندی. (تا وقتی PlantFactory کامل نشده.) */
    private Plant createPlant(Plants type, Vec2 pos) {
        switch (type.getCategory()) {
            case SUN_PRODUCER:
                return new SunProducer(type, pos);
            case LOBBER:
                return new Lobber(type, pos);
            case MELEE:
                return new Melee(type, pos);
            case HOMING:
                return new Homing(type, pos);
            case SHOOTER:
                return new Shooter(type, pos);
            case EXPLOSIVE:
                return new Explosive(type, pos);
            case WALL_NUT:
                return new Wallnut(type, pos);
            case MODIFIER:
                return new Modifier(type, pos);
            case STRIKE_THROUGH:
                return new StrikeThrough(type, pos);
            case MINT:
                return new Mint(type, pos);
            default:
                throw new IllegalArgumentException("Unknown category: " + type.getCategory());
        }
    }

    // ======================================================================
    //  تقلب‌ها
    // ======================================================================

    public void cheatAddSun(int n)    { game.addSun(n); }
    public void cheatRemoveCooldown() { game.clearAllCooldowns(); }
    public void cheatAddPlantFood()   { game.setPlantFoodCount(game.getPlantFoodCount() + 1); }

    /** دستور release the nuke: کشتن همه‌ی زامبی‌های نقشه. */
    public void releaseNuke() {
        for (Zombie z : game.getZombies()) {
            System.out.println("Zombie of type " + z.getClass().getSimpleName()
                + " is dead at (" + z.getCol() + ", " + z.getRow() + ")");
        }
        game.getZombies().clear();
    }

    private void decreaseCooldowns() {
        game.getCooldowns().replaceAll((type, remaining) -> Math.max(0, remaining - 1));
    }
}
