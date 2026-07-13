package model.entities.zombies;

import model.ChapterType;
import model.Game;
import model.Vec2;
import model.entities.Cell;
import model.entities.CellType;

/** غواص: روی آب غوطه‌ور می‌شود (فقط Lobberها آسیبش می‌زنند)؛ برای خوردنِ گیاه به سطح می‌آید و آسیب‌پذیر می‌شود. */
public class SnorkelZombie extends Zombie {

    private boolean submerged = false;

    public SnorkelZombie(Zombies data, int line, Vec2 position, ChapterType chapter) {
        super(data.getHp(), data.getSpeed(), data.getEatDPS(), line, position,
            data.getArmor(), chapter, null, ZombieState.WALKING, null);
    }

    @Override
    public void onTick(Game game) {
        Cell cell = game.getField().getCell(getCol(), getRow());
        boolean onWater = (cell != null && cell.getType() == CellType.WATER);
        boolean atPlant = (frontPlant(game) != null);   // به گیاهی رسیده که باید بخورد

        if (onWater && !atPlant) {
            submerged = true;                 // غوطه‌ور: فقط حرکت، مصون از تیرِ معمولی
            setState(ZombieState.SPECIAL);
            move(game);
        } else {
            submerged = false;                // خشکی یا در حالِ خوردنِ گیاهِ روی آب → به سطح می‌آید
            walkOrEat(game);
        }
    }

    /** زیر آب از تیرِ معمولی/شلیک آسیب نمی‌بیند. */
    @Override
    public void takeDamage(double dmg) {
        if (submerged) return;
        super.takeDamage(dmg);
    }

    /** Lobberها حتی زیر آب هم آسیب می‌زنند. */
    @Override
    public void takeDamageFromLobber(double dmg) {
        super.takeDamage(dmg);
    }
}
