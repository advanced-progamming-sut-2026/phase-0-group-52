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

    private final ChapterMechanics mechanics;
    private int skySunTimer = 0;

    public GameLoop(Game game) {
        this.mechanics = (game.getField() != null)
                ? ChapterMechanics.forChapter(game.getField().getChapter()) : null;
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

        for (Plant p : new ArrayList<Plant>(game.getPlants())) p.onTick(game);
        for (Zombie z : new ArrayList<Zombie>(game.getZombies())) {
            z.advanceFreeze();
            if (z.isFrozenSolid()) continue;
            z.onTick(game);
        }
        if (mechanics != null) mechanics.onTick(game);
        if (game.getLevel() != null) game.getLevel().onTick(game);

        if (game.getField() != null)
            for (Lawnmower lm : game.getField().getLawnmowers()) lm.triggerIfZombieAtHouse(game);

        for (Map.Entry<Plants, Double> e : game.getCooldowns().entrySet())
            if (e.getValue() > 0) e.setValue(Math.max(0, e.getValue() - Game.SECONDS_PER_TICK));

        PlantCombat.removeDeadZombies(game);
        spawnWaves(game, tick);
        recordSeenZombies(game);
        if (!game.getZombies().isEmpty()) game.getStats().recordFirstWave(tick);

        String defeat = game.getLevel() != null ? game.getLevel().checkDefeat(game) : null;
        if (defeat != null) return end(game, false, defeat);
        String victory = game.getLevel() != null ? game.getLevel().checkVictory(game) : null;
        if (victory != null) return end(game, true, victory);
        if (allWavesSpawned(game) && game.getZombies().isEmpty() && tick > WAVE_INTERVAL)
            return end(game, true, "All zombies defeated. You win!");
        if (anyZombieAtHouse(game))
            return end(game, false, "A zombie reached your house. You lose!");
        return null;
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
        SunType type = SunType.pickRandom(PlantCombat.RANDOM.nextDouble());
        int col = PlantCombat.RANDOM.nextInt(GameField.COLS);
        int row = PlantCombat.RANDOM.nextInt(GameField.ROWS);
        game.getSuns().add(new Sun(type, new Vec2(col, row)));
        System.out.println("New " + type + " sun is dropping at position ("
                + (col + 1) + ", " + (row + 1) + ")");
    }

    private void spawnWaves(Game game, int tick) {
        model.level.Level level = game.getLevel();

        if (level != null && level.areWavesHeld()) return;
        int nextWave = game.getCurrentWaveIndex() + 1;

        boolean ready = (level != null && level.manualWaves())
                ? (tick - level.getWaveStartTick()) >= nextWave * WAVE_INTERVAL
                : tick >= (nextWave + 1) * WAVE_INTERVAL;
        if (nextWave < game.getWaves().size() && ready) {
            game.setCurrentWaveIndex(nextWave);
            Wave w = game.getWaves().get(nextWave);
            game.getZombies().addAll(w.getZombies());
            System.out.println("Wave " + (nextWave + 1) + " incoming! " + w.getZombies().size() + " zombie(s).");
            if (mechanics != null) mechanics.onWaveStart(game);
        }
    }

    private void recordSeenZombies(Game game) {
        model.User user = (game.getApp() != null) ? game.getApp().getCurrentuser() : null;
        if (user == null) return;
        for (Zombie z : game.getZombies()) {
            String name = z.getDisplayName();
            if (user.markZombieSeen(name))
                user.getNewsList().addNews("A new zombie appeared in battle: " + name + "!");
        }
    }

    private boolean allWavesSpawned(Game game) {
        return game.getCurrentWaveIndex() >= game.getWaves().size() - 1;
    }

    private boolean anyZombieAtHouse(Game game) {
        for (Zombie z : game.getZombies()) if (z.getPosition().x <= 0) return true;
        return false;
    }

    private String end(Game game, boolean won, String message) {
        game.setGameOver(true);
        game.setWon(won);
        game.getStats().setFinalSun(game.getSunAmount());
        return message;
    }
}
