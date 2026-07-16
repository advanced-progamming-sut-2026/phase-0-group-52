package pvz.model.level;

import pvz.model.ChapterType;
import pvz.model.Game;
import pvz.model.entities.plants.PlantCombat;
import pvz.model.entities.plants.Plants;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class ConveyorBeltLevel extends Level {

    public static final int DELIVERY_INTERVAL = 12;

    private final Queue<Plants> belt = new LinkedList<Plants>();
    private double timer = 0;
    private boolean firstDelivered = false;

    public ConveyorBeltLevel(int levelnumber, ChapterType chaptertype,
            ArrayList<Plants> allowedplants, AttackPattern attackPattern) {
        super(levelnumber, chaptertype, allowedplants, attackPattern);
    }

    @Override
    public void onTick(Game game) {
        if (!firstDelivered) {
            firstDelivered = true;
            deliver();
            return;
        }
        timer += 1;
        if (timer >= DELIVERY_INTERVAL) {
            timer = 0;
            deliver();
        }
    }

    private void deliver() {
        ArrayList<Plants> pool = getAllowedplants();
        if (pool == null || pool.isEmpty()) return;
        Plants plant = pool.get(PlantCombat.RANDOM.nextInt(pool.size()));
        belt.add(plant);
        System.out.println("The conveyor belt delivered a " + plant.getName() + ".");
    }

    public Queue<Plants> getBelt() { return belt; }

    public boolean hasOnBelt(Plants type) { return belt.contains(type); }

    public boolean takeFromBelt(Plants type) { return belt.remove(type); }
}
