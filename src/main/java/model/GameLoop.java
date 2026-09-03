package model;

import model.entities.Lawnmower;
import model.entities.Sun;
import model.entities.SunType;
import model.entities.plants.Plant;
import model.entities.plants.PlantCombat;
import model.entities.plants.Plants;
import model.entities.zombies.Zombie;
import model.mechanics.ChapterMechanics;

import java.util.ArrayList;
import java.util.Map;

public class GameLoop {

    public static final int WAVE_INTERVAL = 15 * Game.TICKS_PER_SECOND;
    public static final int MIN_WAVE_GAP = 14 * Game.TICKS_PER_SECOND;
    public static final int MAX_WAVE_GAP = 24 * Game.TICKS_PER_SECOND;
    public static final int CLEAR_THRESHOLD = 2;
    public static final int MIN_LEVEL_TICKS = 120 * Game.TICKS_PER_SECOND;

    private int lastWaveTick;

    private final ChapterMechanics mechanics;
    private int skySunTimer = 0;

    public GameLoop(Game game) {
        this.mechanics = (game.getField() != null)
                ? ChapterMechanics.forChapter(game.getField().getChapter()) : null;
    }

    public ChapterMechanics mechanics() {
        return mechanics;
    }

    public String step(Game game) {
        if (game.isGameOver()) return null;
        int tick = game.getCurrentTick() + 1;
        game.setCurrentTick(tick);
        if (skyEnabled(game)) {
            skySunTimer++;
            if (skySunTimer >= skyIntervalTicks(game)) {
                skySunTimer = 0;
                dropSkySun(game);
            }
        }
        for (Sun s : game.getSuns()) {
            boolean wasFalling = s.isFalling();
            s.tickFall();
            if (wasFalling && !s.isFalling()) {
                if (s.getType() == SunType.RADIOACTIVE) s.convertToNormal();
                System.out.println("Sun reached the ground at position ("
                        + (s.getCol() + 1) + ", " + (s.getRow() + 1) + ")");
            }
        }

        for (Plant p : new ArrayList<Plant>(game.getPlants())) {
            p.ageStatesTick();
            if (p.isFrozenSolid()) {
                continue;
            }
            p.onTick(game);
        }
        for (Zombie z : new ArrayList<Zombie>(game.getZombies())) {
            z.ageStatus();
            z.advanceFreeze();
            if (z.isFrozenSolid()) continue;
            z.onTick(game);
        }
        if (mechanics != null) mechanics.onTick(game);
        if (game.getLevel() != null) game.getLevel().onTick(game);

        if (game.getField() != null)
            for (Lawnmower lm : game.getField().getLawnmowers()) {
                lm.triggerIfZombieAtHouse(game);
                lm.advance(game);
            }

        coolDown(game);

        flyProjectiles(game);
        PlantCombat.removeDeadZombies(game);
        spawnWaves(game, tick);
        recordSeenZombies(game);
        if (!game.getZombies().isEmpty()) game.getStats().recordFirstWave(tick);

        if (game.isEndless()) {
            return null;
        }
        String defeat = game.getLevel() != null ? game.getLevel().checkDefeat(game) : null;
        if (defeat != null) return end(game, false, defeat);
        String victory = game.getLevel() != null ? game.getLevel().checkVictory(game) : null;
        if (victory != null) return end(game, true, victory);
        if (allWavesSpawned(game) && game.getZombies().isEmpty() && tick > MIN_LEVEL_TICKS)
            return end(game, true, "All zombies defeated. You win!");
        if (anyZombieAtHouse(game))
            return end(game, false, "A zombie reached your house. You lose!");
        return null;
    }

    private void flyProjectiles(Game game) {
        for (model.entities.Projectile shot
                : new ArrayList<model.entities.Projectile>(game.getProjectiles())) {
            shot.advance(game);
        }
        java.util.Iterator<model.entities.Projectile> gone = game.getProjectiles().iterator();
        while (gone.hasNext()) {
            if (gone.next().isSpent()) {
                gone.remove();
            }
        }
    }

    private void coolDown(Game game) {
        for (Map.Entry<Plants, Double> e : game.getCooldowns().entrySet()) {
            if (e.getValue() > 0) {
                e.setValue(Math.max(0, e.getValue() - Game.SECONDS_PER_TICK));
            }
        }
    }

    private boolean skyEnabled(Game game) {
        if (game.getField() != null && game.getField().getChapter() == ChapterType.DARK_AGES) return false;
        if (game.getLevel() != null && !game.getLevel().isSkySunEnabled()) return false;
        return true;
    }

    private double skyIntervalTicks(Game game) {
        double t = game.getCurrentTick() * Game.SECONDS_PER_TICK;
        double x = Math.max(6 + 0.05 * t, 12);
        return x * Game.TICKS_PER_SECOND;
    }

    private void dropSkySun(Game game) {
        if (game.lawnIsLitteredWithSun()) {
            return;
        }
        SunType type = SunType.pickRandom(PlantCombat.RANDOM.nextDouble());
        double col = PlantCombat.RANDOM.nextDouble() * GameField.COLS;
        double row = PlantCombat.RANDOM.nextDouble() * GameField.ROWS;
        game.getSuns().add(new Sun(type, new Vec2(col, row)));
        System.out.println("New " + type + " sun is dropping at position ("
                + (int) (col + 1) + ", " + (int) (row + 1) + ")");
    }

    private void spawnWaves(Game game, int tick) {
        model.level.Level level = game.getLevel();

        if (level != null && level.areWavesHeld()) return;
        int nextWave = game.getCurrentWaveIndex() + 1;

        boolean timed = (level != null && level.manualWaves())
                ? (tick - level.getWaveStartTick()) >= nextWave * WAVE_INTERVAL
                : tick >= (nextWave + 1) * WAVE_INTERVAL;
        boolean cleared = game.getZombies().size() <= CLEAR_THRESHOLD
                && tick - lastWaveTick >= MIN_WAVE_GAP;
        boolean overdue = tick - lastWaveTick >= MAX_WAVE_GAP;
        boolean ready = (timed && game.getZombies().isEmpty()) || cleared || overdue;
        if (nextWave < game.getWaves().size() && ready) {
            lastWaveTick = tick;
            game.setCurrentWaveIndex(nextWave);
            Wave w = game.getWaves().get(nextWave);
            game.getZombies().addAll(w.getZombies());
            System.out.println("Wave " + (nextWave + 1) + " incoming! " + w.getZombies().size() + " zombie(s).");
            if (mechanics != null) mechanics.onWaveStart(game);
        }
    }

    private void recordSeenZombies(Game game) {
    }

    private boolean allWavesSpawned(Game game) {
        return game.getCurrentWaveIndex() >= game.getWaves().size() - 1;
    }

    private boolean anyZombieAtHouse(Game game) {
        for (Zombie z : game.getZombies()) {
            if (z.getPosition().x > 0) continue;
            Lawnmower guard = game.getField() == null ? null
                    : game.getField().getLawnmower(z.getRow());
            if (guard != null && (guard.isIsactive() || guard.isRunning())) continue;
            return true;
        }
        return false;
    }

    private String end(Game game, boolean won, String message) {
        game.setGameOver(true);
        game.setWon(won);
        game.getStats().setFinalSun(game.getSunAmount());
        return message;
    }
}
