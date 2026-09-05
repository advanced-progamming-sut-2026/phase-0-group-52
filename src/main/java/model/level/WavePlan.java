package model.level;

import model.ChapterType;
import model.entities.zombies.Zombies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class WavePlan {

    public static final int NORMAL_DIFFICULTY = 3;
    public static final int LATE_LEVEL = 3;
    public static final int MIN_WAVES = 4;
    public static final int MAX_WAVES = 10;
    public static final double BUDGET_BASE = 230d;
    public static final double BUDGET_PER_LEVEL = 90d;
    public static final double BUDGET_PER_WAVE = 0.22d;
    public static final double DIFFICULTY_STEP = 0.2d;
    public static final double FLAG_MULTIPLIER = 1.6d;
    public static final double TIMED_PRESSURE = 1.35d;
    public static final double GENTLE_PRESSURE = 0.75d;
    public static final double BELT_PRESSURE = 0.85d;
    public static final double LOCKED_PRESSURE = 0.9d;

    private WavePlan() {}

    public static int waveCount(int levelNumber) {
        return Math.max(MIN_WAVES, Math.min(MAX_WAVES, 3 + levelNumber));
    }

    public static boolean isFlagWave(int waveIndex, int waveCount) {
        return waveIndex == waveCount - 1 || (waveIndex + 1) % 5 == 0;
    }

    public static double budget(int levelNumber, int difficulty, int waveIndex, int waveCount) {
        return budget(levelNumber, difficulty, waveIndex, waveCount, null);
    }

    public static double budget(int levelNumber, int difficulty, int waveIndex,
            int waveCount, SpecialLevel special) {
        double scale = 1d + (difficulty - NORMAL_DIFFICULTY) * DIFFICULTY_STEP;
        double growth = 1d + waveIndex * BUDGET_PER_WAVE;
        double total = (BUDGET_BASE + BUDGET_PER_LEVEL * (levelNumber - 1)) * scale * growth;
        if (isFlagWave(waveIndex, waveCount)) {
            total *= FLAG_MULTIPLIER;
        }
        return total * pressure(special);
    }

    public static double pressure(SpecialLevel special) {
        if (special == null) {
            return 1d;
        }
        switch (special) {
            case TIMED_WAR:      return TIMED_PRESSURE;
            case SAVE_OUR_SEEDS: return GENTLE_PRESSURE;
            case LOVE_YOUR_PLANTS: return GENTLE_PRESSURE;
            case CONVEYOR:
            case PLANT_WHAT_YOU_GET: return BELT_PRESSURE;
            case LOCKED_PLANTS:  return LOCKED_PRESSURE;
            case DEADLINE:       return GENTLE_PRESSURE;
            default:             return 1d;
        }
    }

    public static int waveCount(int levelNumber, SpecialLevel special) {
        int base = waveCount(levelNumber);
        return special == SpecialLevel.TIMED_WAR ? Math.min(MAX_WAVES, base + 2) : base;
    }

    public static List<Zombies> roster(ChapterType chapter, int levelNumber) {
        List<Zombies> pool = new ArrayList<Zombies>();
        pool.add(Zombies.ZOMBIE_DEFAULT);
        if (chapter == null) {
            return pool;
        }
        switch (chapter) {
            case ANCIENT_EGYPT:   egypt(pool, levelNumber); break;
            case FROSTBITE_CAVES: frostbite(pool, levelNumber); break;
            case DARK_AGES:       dark(pool, levelNumber); break;
            case BIG_WAVE_BEACH:  beach(pool, levelNumber); break;
            default: break;
        }
        return pool;
    }

    private static void egypt(List<Zombies> pool, int level) {
        pool.add(Zombies.ZOMBIE_ARMOR1);
        pool.add(Zombies.ZOMBIE_RA);
        pool.add(Zombies.ZOMBIE_EXPLORER);
        pool.add(Zombies.ZOMBIE_TOMB_RAISER);
        if (level >= 2) {
            pool.add(Zombies.ZOMBIE_ARMOR2);
            pool.add(Zombies.ZOMBIE_IMP);
        }
        if (level >= LATE_LEVEL) {
            pool.add(Zombies.ZOMBIE_ARMOR4);
            pool.add(Zombies.ZOMBIE_GARGANTUAR);
        }
    }

    private static void frostbite(List<Zombies> pool, int level) {
        pool.add(Zombies.ZOMBIE_ARMOR1);
        pool.add(Zombies.ZOMBIE_ICE_AGE_HUNTER);
        pool.add(Zombies.ZOMBIE_ICE_AGE_DODO);
        if (level >= 2) {
            pool.add(Zombies.ZOMBIE_ARMOR2);
            pool.add(Zombies.ZOMBIE_ICE_AGE_TROGLOBITE);
        }
        if (level >= 3) {
            pool.add(Zombies.ZOMBIE_ARMOR4);
            pool.add(Zombies.ZOMBIE_GARGANTUAR);
        }
    }

    private static void dark(List<Zombies> pool, int level) {
        pool.add(Zombies.ZOMBIE_DEFAULT);
        pool.add(Zombies.ZOMBIE_DARK_ARMOR3);
        pool.add(Zombies.ZOMBIE_DARK_JUGGLER);
        pool.add(Zombies.ZOMBIE_WIZARD);
        if (level >= 2) {
            pool.add(Zombies.ZOMBIE_DARK_IMP_DRAGON);
        }
        if (level >= 3) {
            pool.add(Zombies.ZOMBIE_DARK_KING);
        }
    }

    private static void beach(List<Zombies> pool, int level) {
        pool.add(Zombies.ZOMBIE_ARMOR1);
        pool.add(Zombies.ZOMBIE_BEACH_FISHERMAN);
        pool.add(Zombies.ZOMBIE_BEACH_SNORKEL);
        if (level >= 2) {
            pool.add(Zombies.ZOMBIE_BEACH_OCTOPUS);
            pool.add(Zombies.ZOMBIE_IMP);
        }
    }

    public static List<Zombies> restrict(List<Zombies> roster, SpecialLevel special) {
        if (special == null) {
            return roster;
        }
        List<Zombies> out = new ArrayList<Zombies>(roster);
        if (special == SpecialLevel.SAVE_OUR_SEEDS || special == SpecialLevel.LOCKED_PLANTS) {
            out.remove(Zombies.ZOMBIE_TOMB_RAISER);
        }
        if (special == SpecialLevel.LOVE_YOUR_PLANTS
                || special == SpecialLevel.SAVE_OUR_SEEDS) {
            out.remove(Zombies.ZOMBIE_GARGANTUAR);
            out.remove(Zombies.ZOMBIE_DARK_KING);
        }
        if (special == SpecialLevel.CONVEYOR
                || special == SpecialLevel.PLANT_WHAT_YOU_GET) {
            out.remove(Zombies.ZOMBIE_IMP);
        }
        if (out.isEmpty()) {
            out.add(Zombies.ZOMBIE_DEFAULT);
        }
        return out;
    }

    public static List<Zombies> compose(List<Zombies> roster, double budget, Random random) {
        List<Zombies> picked = new ArrayList<Zombies>();
        if (roster.isEmpty()) {
            return picked;
        }
        List<Zombies> affordable = new ArrayList<Zombies>();
        double spent = 0d;
        while (spent < budget) {
            affordable.clear();
            for (Zombies type : roster) {
                if (spent + cost(type) <= budget) {
                    affordable.add(type);
                }
            }
            if (affordable.isEmpty()) {
                break;
            }
            Zombies pick = affordable.get(random.nextInt(affordable.size()));
            picked.add(pick);
            spent += cost(pick);
        }
        if (picked.isEmpty()) {
            picked.add(roster.get(0));
        }
        Collections.shuffle(picked, random);
        return picked;
    }

    private static double cost(Zombies type) {
        model.entities.zombies.ZombieRecord record =
                model.entities.zombies.ZombieData.of(type);
        double weight = record == null ? 100d : record.getWaveCost();
        return weight <= 0d ? 100d : weight;
    }
}
