package model.entities.plants.types;

import model.Game;
import model.Vec2;
import model.entities.plants.PlantCombat;
import model.entities.plants.Plants;
import model.entities.plants.Shooter;
import model.entities.zombies.Zombie;

public class Starfruit extends Shooter {

    public Starfruit(Vec2 position) {
        super(Plants.STARFRUIT, position);
    }

    @Override
    protected void shoot(Game game) {
        shootStar(game, 1);
    }

    @Override
    public void onPlantFood(Game game) {

        shootStar(game, 15);
    }

    private void shootStar(Game game, int multiplier) {
        Zombie ahead = PlantCombat.frontmostAhead(game, getRow(), getCol());
        if (ahead != null) ahead.takeDamage(multiplier * getAttackdamage());
        Zombie behind = PlantCombat.nearestBehind(game, getRow(), getCol());
        if (behind != null) behind.takeDamage(multiplier * getAttackdamage());
        for (int r = getRow() - 1; r <= getRow() + 1; r += 2) {
            Zombie side = PlantCombat.frontmostAhead(game, r, 0);
            if (side != null) side.takeDamage(multiplier * getAttackdamage());
        }
        PlantCombat.removeDeadZombies(game);
    }
}
