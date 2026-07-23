package model.entities.zombies.types;

import model.ChapterType;
import model.Game;
import model.Vec2;
import model.entities.zombies.ZombieType;
import model.entities.zombies.Zombies;

public class RaZombie extends WalkingZombie {

    private static final int SUN_PER_ORB = 25;
    private static final int MAX_STOLEN = 250;
    private static final double STEAL_INTERVAL = 2;

    private int stolenSun = 0;
    private double stealTimer = 0;

    public RaZombie(int line, Vec2 position, ChapterType chapter, ZombieType type) {
        super(Zombies.ZOMBIE_RA, line, position, chapter, type);
    }

    @Override
    public void onTick(Game game) {
        if (!isHypnotized() && stolenSun < MAX_STOLEN && game.getSunAmount() > 0) {
            stealTimer += Game.SECONDS_PER_TICK;
            if (stealTimer >= STEAL_INTERVAL) {
                stealTimer = 0;
                int steal = Math.min(SUN_PER_ORB, Math.min(game.getSunAmount(), MAX_STOLEN - stolenSun));
                game.setSunAmount(game.getSunAmount() - steal);
                stolenSun += steal;
                System.out.println("Zombie Ra stole " + steal + " sun! (reserve: " + game.getSunAmount() + ")");
            }
        }
        super.onTick(game);
    }

    @Override
    public void onDeath(Game game) {
        if (stolenSun > 0) {
            game.addSun(stolenSun);
            System.out.println("Zombie Ra died and dropped " + stolenSun + " sun back.");
        }
    }
}
