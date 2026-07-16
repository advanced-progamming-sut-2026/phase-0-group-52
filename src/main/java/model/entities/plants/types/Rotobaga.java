package model.entities.plants.types;

import model.Game;
import model.Vec2;
import model.entities.plants.PlantCombat;
import model.entities.plants.Plants;
import model.entities.plants.Shooter;
import model.entities.zombies.Zombie;

public class Rotobaga extends Shooter {

    public Rotobaga(Vec2 position) {
        super(Plants.ROTOBAGA, position);
    }

    @Override
    protected void shoot(Game game) {
        shootDiagonals(game, 1);
    }

    @Override
    public void onPlantFood(Game game) {

        shootDiagonals(game, 10);
    }

    private void shootDiagonals(Game game, int multiplier) {
        for (int r = getRow() - 1; r <= getRow() + 1; r += 2) {
            Zombie ahead = PlantCombat.frontmostAhead(game, r, getCol());
            if (ahead != null) ahead.takeDamage(multiplier * getAttackdamage());
            Zombie behind = PlantCombat.nearestBehind(game, r, getCol());
            if (behind != null) behind.takeDamage(multiplier * getAttackdamage());
        }
        PlantCombat.removeDeadZombies(game);
    }
}
