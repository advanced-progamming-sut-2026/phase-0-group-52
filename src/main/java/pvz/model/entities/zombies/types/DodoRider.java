package pvz.model.entities.zombies.types;

import pvz.model.ChapterType;
import pvz.model.Game;
import pvz.model.Vec2;
import pvz.model.entities.plants.Plant;
import pvz.model.entities.plants.PlantTag;
import pvz.model.entities.plants.Plants;
import pvz.model.entities.plants.PlantsCategory;
import pvz.model.entities.zombies.ZombieState;
import pvz.model.entities.zombies.ZombieType;
import pvz.model.entities.zombies.Zombies;

public class DodoRider extends WalkingZombie {

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
