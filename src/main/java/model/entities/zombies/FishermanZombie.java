package model.entities.zombies;

import model.ChapterType;
import model.Game;
import model.Vec2;
import model.entities.Cell;
import model.entities.plants.Plant;

/** ماهیگیر: در راست‌ترین ستون ثابت می‌ماند و هر ۲.۵ث گیاهِ ردیف را با قلاب می‌کشد؛ اگر کنارش باشد نابودش می‌کند. */
public class FishermanZombie extends Zombie {

    private static final int MAX_RANGE = 8;   // CastingAreaMaxRange
    private static final int MIN_RANGE = 2;   // CastingAreaMinRange
    private double castTimer = 0;

    public FishermanZombie(Zombies data, int line, Vec2 position, ChapterType chapter) {
        super(data.getHp(), data.getSpeed(), data.getEatDPS(), line, position,
            data.getArmor(), chapter, null, ZombieState.IDLE, null);
    }

    @Override
    public void onTick(Game game) {
        // ثابت در راست‌ترین ستون — حرکت نمی‌کند و نمی‌خورد.
        castTimer += 1;
        if (castTimer >= Game.secondsToTicks(2.5)) {
            castTimer = 0;
            cast(game);
        }
    }

    private void cast(Game game) {
        Plant target = nearestPlantInRange(game);
        if (target == null) return;
        double dist = getPosition().x - target.getCol();

        if (dist <= MIN_RANGE) {
            // کنار ماهیگیر: گیاهِ قلاب‌شده را پرتاب و نابود می‌کند
            destroyPlant(game, target);
            System.out.println("The fisherman threw and destroyed a plant at ("
                + target.getCol() + ", " + getRow() + ")");
        } else {
            pullRight(game, target); // یک خانه به سمت خود (راست)
        }
    }

    /** نزدیک‌ترین گیاهِ ردیف که جلوی ماهیگیر و در بردِ حداکثری است. */
    private Plant nearestPlantInRange(Game game) {
        Plant best = null;
        int bestCol = -1;
        double zx = getPosition().x;
        for (Plant p : game.getPlants()) {
            if (p.getRow() != getRow()) continue;
            double dist = zx - p.getCol();
            if (dist > 0 && dist <= MAX_RANGE && p.getCol() > bestCol) {
                bestCol = p.getCol();
                best = p;
            }
        }
        return best;
    }

    private void pullRight(Game game, Plant plant) {
        int newCol = plant.getCol() + 1;
        if (newCol >= game.getField().getCols()) {          // بیرونِ نقشه → نابود
            destroyPlant(game, plant);
            System.out.println("The fisherman pulled a plant off the map at ("
                + plant.getCol() + ", " + getRow() + ")");
            return;
        }
        Cell to = game.getField().getCell(newCol, getRow());
        if (to != null && to.isEmpty()) {                   // خانه‌ی خالی → جابه‌جایی
            game.getField().getCell(plant.getCol(), getRow()).getPlants().remove(plant);
            to.getPlants().add(plant);
            plant.setPosition(new Vec2(newCol, getRow()));
            System.out.println("The fisherman pulled a plant to (" + newCol + ", " + getRow() + ")");
        } else {                                            // مقصد اشغال (به‌ندرت) → نابود
            destroyPlant(game, plant);
            System.out.println("The fisherman crushed a stuck plant at ("
                + plant.getCol() + ", " + getRow() + ")");
        }
    }


    private void destroyPlant(Game game, Plant plant) {
        game.getPlants().remove(plant);
        Cell c = game.getField().getCell(plant.getCol(), getRow());
        if (c != null) c.getPlants().remove(plant);
    }
}
