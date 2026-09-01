package model.mechanics;

import model.Game;
import model.GameField;
import model.Vec2;
import model.entities.plants.PlantCombat;
import model.entities.zombies.Zombie;

public class AncientEgyptMechanics implements ChapterMechanics {

    private static final int FIRST_STORM_WAVE = 2;
    private static final int MAX_RIDERS = 4;
    private static final int LANDING_MIN = 2;
    private static final double STORM_SPEED = 0.55d;
    private static final double SWEEP_CHANCE = 0.06d;
    private static final int STARTING_TOMBS = 3;
    private static final int FIRST_TOMB_COLUMN = 3;

    private boolean seeded;

    @Override
    public void onWaveStart(Game game) {
        if (game.getField() == null) {
            return;
        }
        int wave = game.getCurrentWaveIndex();
        boolean finale = wave >= game.getWaves().size() - 1;
        if (!finale && wave + 1 < FIRST_STORM_WAVE) {
            return;
        }
        int riders = 0;
        for (Zombie z : game.getZombies()) {
            if (riders >= MAX_RIDERS) {
                break;
            }
            if (z.getPosition().x < GameField.COLS || z.isRidingStorm()) {
                continue;
            }
            double landing = LANDING_MIN
                    + PlantCombat.RANDOM.nextInt(GameField.COLS - LANDING_MIN);
            z.rideStorm(landing);
            riders++;
        }
        if (riders > 0) {
            game.setStormPending(true);
            System.out.println("Sandstorms sweep in carrying " + riders + " zombie(s)!");
        }
    }

    private void carryRiders(Game game) {
        for (Zombie z : new java.util.ArrayList<Zombie>(game.getZombies())) {
            if (!z.isRidingStorm()) {
                continue;
            }
            double next = z.getPosition().x - STORM_SPEED;
            sweepPlants(game, z, next);
            if (next <= z.stormLanding()) {
                next = z.stormLanding();
                z.landFromStorm();
            }
            z.setPosition(new Vec2(next, z.getRow()));
        }
    }

    private void sweepPlants(Game game, Zombie z, double next) {
        if (PlantCombat.RANDOM.nextDouble() >= SWEEP_CHANCE) {
            return;
        }
        int column = (int) Math.floor(next);
        java.util.ArrayList<model.entities.plants.Plant> hit =
                game.getPlantsAt(column, z.getRow());
        for (model.entities.plants.Plant plant : hit) {
            plant.setHp(0);
            PlantCombat.removePlant(game, plant);
            System.out.println("The sandstorm tore a plant out of the ground!");
        }
    }

    @Override
    public void onTick(Game game) {
        carryRiders(game);
        if (!seeded) {
            seeded = true;
            seedTombstones(game);
        }
        java.util.Iterator<model.entities.Tombstone> gone =
                game.getTombstones().iterator();
        while (gone.hasNext()) {
            model.entities.Tombstone stone = gone.next();
            if (stone.isDestroyed()) {
                model.entities.Cell cell =
                        game.getField().getCell(stone.getColumn(), stone.getRow());
                if (cell != null) {
                    cell.setType(model.entities.CellType.NORMAL);
                }
                gone.remove();
            }
        }
    }

    private void seedTombstones(Game game) {
        if (game.getField() == null) {
            return;
        }
        int wanted = STARTING_TOMBS + PlantCombat.RANDOM.nextInt(2);
        int columns = game.getField().getCols();
        int rows = game.getField().getRows();
        for (int made = 0; made < wanted; made++) {
            for (int attempt = 0; attempt < columns * rows; attempt++) {
                int col = FIRST_TOMB_COLUMN
                        + PlantCombat.RANDOM.nextInt(Math.max(1, columns - FIRST_TOMB_COLUMN));
                int row = PlantCombat.RANDOM.nextInt(rows);
                model.entities.Cell cell = game.getField().getCell(col, row);
                if (cell == null || cell.getType() != model.entities.CellType.NORMAL
                        || !cell.getPlants().isEmpty()) {
                    continue;
                }
                cell.setType(model.entities.CellType.TOMBSTONE);
                game.getTombstones().add(new model.entities.Tombstone(col, row));
                break;
            }
        }
    }
}
