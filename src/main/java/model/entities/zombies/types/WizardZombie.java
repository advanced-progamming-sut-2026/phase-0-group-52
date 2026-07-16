package model.entities.zombies.types;

import model.ChapterType;
import model.Game;
import model.Vec2;
import model.entities.plants.Plant;
import model.entities.plants.PlantCombat;
import model.entities.zombies.ZombieType;
import model.entities.zombies.Zombies;

import java.util.ArrayList;
import java.util.List;

public class WizardZombie extends WalkingZombie {

    private static final double CAST_INTERVAL = 5;

    private double castTimer = 0;
    private final List<Plant> transformed = new ArrayList<Plant>();

    public WizardZombie(int line, Vec2 position, ChapterType chapter, ZombieType type) {
        super(Zombies.ZOMBIE_WIZARD, line, position, chapter, type);
    }

    @Override
    public void onTick(Game game) {
        if (isHypnotized()) {
            super.onTick(game);
            return;
        }
        castTimer += 1;
        if (castTimer >= CAST_INTERVAL) {
            castTimer = 0;
            castOnRandomPlant(game);
        }
        Plant here = topPlantHere(game);
        if (here != null) {
            if (!here.isFrozen()) transform(here);
            move(game);
            return;
        }
        super.onTick(game);
    }

    private void castOnRandomPlant(Game game) {
        List<Plant> candidates = new ArrayList<Plant>();
        for (Plant p : game.getPlants())
            if (!p.isFrozen()) candidates.add(p);
        if (candidates.isEmpty()) return;
        transform(candidates.get(PlantCombat.RANDOM.nextInt(candidates.size())));
    }

    private void transform(Plant plant) {
        plant.setFrozen(true);
        transformed.add(plant);
    }

    @Override
    public void onDeath(Game game) {

        for (Plant p : transformed)
            p.setFrozen(false);
        transformed.clear();
    }
}
