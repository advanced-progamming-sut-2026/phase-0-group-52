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

    public static final int WAVE_INTERVAL = 30;
    public static final int SKY_SUN_INTERVAL = 100;

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
            if (skySunTimer >= SKY_SUN_INTERVAL) {
                skySunTimer = 0;
                dropSkySun(game);
            }
        }
        for (Sun s : game.getSuns()) s.tickFall();

        for (Plant p : new ArrayList<Plant>(game.getPlants())) p.onTick(game);
        for (Zombie z : new ArrayList<Zombie>(game.getZombies())) z.onTick(game);
        if (mechanics != null) mechanics.onTick(game);
        if (game.getLevel() != null) game.getLevel().onTick(game);

        if (game.getField() != null)
            for (Lawnmower lm : game.getField().getLawnmowers()) lm.triggerIfZombieAtHouse(game);

        for (Map.Entry<Plants, Double> e : game.getCooldowns().entrySet())
            if (e.getValue() > 0) e.setValue(Math.max(0, e.getValue() - 1));

        PlantCombat.removeDeadZombies(game);
        spawnWaves(game, tick);
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

    private void dropSkySun(Game game) {
        SunType type = SunType.pickRandom(PlantCombat.RANDOM.nextDouble());
        int col = PlantCombat.RANDOM.nextInt(GameField.COLS);
        int row = PlantCombat.RANDOM.nextInt(GameField.ROWS);
        game.getSuns().add(new Sun(type, new Vec2(col, row)));
        System.out.println("A " + (type == SunType.NORMAL ? "" : type + " ") + "sun is falling from the sky.");
    }

    private void spawnWaves(Game game, int tick) {
        int nextWave = game.getCurrentWaveIndex() + 1;
        if (nextWave < game.getWaves().size() && tick >= (nextWave + 1) * WAVE_INTERVAL) {
            game.setCurrentWaveIndex(nextWave);
            Wave w = game.getWaves().get(nextWave);
            game.getZombies().addAll(w.getZombies());
            System.out.println("Wave " + (nextWave + 1) + " incoming! " + w.getZombies().size() + " zombie(s).");
            if (mechanics != null) mechanics.onWaveStart(game);
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
