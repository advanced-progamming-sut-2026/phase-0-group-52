package model.entities.zombies.types;

import model.ChapterType;
import model.Game;
import model.Vec2;
import model.entities.plants.Plant;
import model.entities.plants.PlantTag;
import model.entities.plants.Plants;
import model.entities.plants.PlantsCategory;
import model.entities.zombies.ZombieState;
import model.entities.zombies.ZombieType;
import model.entities.zombies.Zombies;

public class DodoRider extends WalkingZombie {

    private boolean gliding;

    public boolean isGliding() {
        return gliding;
    }

    public void setGliding(boolean value) {
        this.gliding = value;
    }


    public DodoRider(int line, Vec2 position, ChapterType chapter, ZombieType type) {
        super(Zombies.ZOMBIE_ICE_AGE_DODO, line, position, chapter, type);
    }

    @Override
    public void onTick(Game game) {
        if (getState() == ZombieState.DISABLED || isHypnotized()) {
            super.onTick(game);
            return;
        }
        Plant here = topPlantHere(game);
        if (here != null && fliesOver(here)) {
            move(game);
            return;
        }
        super.onTick(game);
    }

    private boolean fliesOver(Plant plant) {
        Plants type = plant.getType();
        if (type == Plants.TALL_NUT) return false;
        return type.getCategory() == PlantsCategory.WALL_NUT
                || type.getTags().contains(PlantTag.TRAP)
                || type.getTags().contains(PlantTag.MOVE_ZOMBIES);
    }
}
