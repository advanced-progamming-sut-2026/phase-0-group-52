package model.entities.zombies;

import model.ChapterType;
import model.Game;
import model.Vec2;
import model.entities.Cell;
import model.entities.CellType;

/** قبرساز: هر ۶ ثانیه یک خانه‌ی خالیِ جلویش را به سنگ‌قبر تبدیل می‌کند (حداکثر ۲بار). خودش هم راه می‌رود/می‌خورد. */
public class TombRaiserZombie extends Zombie {

    private static final int MAX_TOMBS = 2;
    private int tombsRaised = 0;
    private double raiseTimer = 0;

    public TombRaiserZombie(Zombies data, int line, Vec2 position, ChapterType chapter) {
        super(data.getHp(), data.getSpeed(), data.getEatDPS(), line, position,
            data.getArmor(), chapter, null, ZombieState.WALKING, null);
    }

    @Override
    public void onTick(Game game) {
        if (tombsRaised < MAX_TOMBS) {
            raiseTimer += 1;
            if (raiseTimer >= Game.secondsToTicks(6)) {
                raiseTimer = 0;
                if (raiseTombstone(game)) tombsRaised++;
            }
        }
        walkOrEat(game);
    }

    /** یک خانه‌ی NORMAL و خالیِ همان ردیف (جلوی زامبی) را سنگ‌قبر می‌کند. */
    private boolean raiseTombstone(Game game) {
        for (int col = Math.min(getCol(), game.getField().getCols() - 1); col >= 0; col--) {
            Cell c = game.getField().getCell(col, getRow());
            if (c != null && c.getType() == CellType.NORMAL && c.isEmpty()) {
                c.setType(CellType.TOMBSTONE);
                System.out.println("A tombstone was raised at (" + col + ", " + getRow() + ")");
                return true;
            }
        }
        return false;
    }
}
