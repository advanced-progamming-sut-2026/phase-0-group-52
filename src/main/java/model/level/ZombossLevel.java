package model.level;

import model.ChapterType;
import model.Game;
import model.entities.plants.PlantCombat;
import model.entities.plants.Plants;
import model.entities.zombies.Zombie;
import model.entities.zombies.types.Zomboss;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class ZombossLevel extends ConveyorBeltLevel {

    public static final int BELT_INTERVAL = 40;
    public static final int BELT_CAPACITY = 8;
    public static final int OPENING_STOCK = 4;
    public static final double BOSS_COLUMN = 6.6d;
    public static final int FREEZE_COLUMN_EVERY = 220;

    private static final Plants[] DECK = {
        Plants.PEASHOOTER, Plants.SUNFLOWER, Plants.WALL_NUT, Plants.SNOW_PEA,
        Plants.REPEATER, Plants.CHERRY_BOMB, Plants.POTATO_MINE, Plants.SQUASH,
    };

    private final Queue<Plants> belt = new LinkedList<Plants>();

    private Zomboss boss;
    private int beltTimer;
    private int columnTimer;
    private boolean opened;

    public ZombossLevel(int levelnumber, ChapterType chaptertype) {
        super(levelnumber, chaptertype, new ArrayList<Plants>(
                java.util.Arrays.asList(DECK)), null);
    }

    public Zomboss getBoss() {
        return boss;
    }

    @Override
    public Queue<Plants> getBelt() {
        return belt;
    }

    @Override
    public boolean hasOnBelt(Plants type) {
        return belt.contains(type);
    }

    @Override
    public boolean takeFromBelt(Plants type) {
        return belt.remove(type);
    }

    @Override
    public boolean isSkySunEnabled() {
        return false;
    }

    @Override
    public String objective() {
        return "Bring down Dr. Zomboss and his machine.";
    }

    @Override
    public String objectiveTag() {
        return "ZOMBOSS";
    }

    public float bossHealth() {
        return boss == null ? 1f : boss.healthFraction();
    }

    public int bossSegments() {
        return boss == null ? Zomboss.SEGMENTS : boss.segmentsLeft();
    }

    public boolean bossStunned() {
        return boss != null && boss.isStunned();
    }

    @Override
    public void onTick(Game game) {
        if (!opened) {
            opened = true;
            arrive(game);
            for (int i = 0; i < OPENING_STOCK; i++) {
                deliver();
            }
        }
        beltTimer++;
        if (beltTimer >= BELT_INTERVAL) {
            beltTimer = 0;
            deliver();
        }
        if (getChaptertype() == ChapterType.FROSTBITE_CAVES && boss != null
                && !boss.isStunned()) {
            columnTimer++;
            if (columnTimer >= FREEZE_COLUMN_EVERY) {
                columnTimer = 0;
                boss.freezeColumn(game);
            }
        }
        coolBurntGround(game);
    }

    private void coolBurntGround(Game game) {
        if (game.getField() == null) {
            return;
        }
        for (int column = 0; column < game.getField().getCols(); column++) {
            for (int row = 0; row < game.getField().getRows(); row++) {
                model.entities.Cell cell = game.getField().getCell(column, row);
                if (cell != null) {
                    cell.coolIfDue(game.getCurrentTick());
                }
            }
        }
    }

    private void arrive(Game game) {
        if (game.getField() == null) {
            return;
        }
        int row = Math.max(0, game.getField().getRows() / 2 - 1);
        boss = new Zomboss(getChaptertype(), row, BOSS_COLUMN);
        game.getZombies().add(boss);
    }

    private void deliver() {
        if (belt.size() >= BELT_CAPACITY) {
            return;
        }
        belt.add(DECK[PlantCombat.RANDOM.nextInt(DECK.length)]);
    }

    @Override
    public String checkVictory(Game game) {
        if (boss == null || boss.getHp() > 0d) {
            return null;
        }
        return "Dr. Zomboss and his machine are scrap. You win!";
    }

    @Override
    public String checkDefeat(Game game) {
        for (Zombie zombie : game.getZombies()) {
            if (zombie == boss || zombie.isHypnotized()) {
                continue;
            }
            if (zombie.getPosition().x <= 0d) {
                return "A zombie slipped past while you fought the boss. You lose!";
            }
        }
        return null;
    }
}
