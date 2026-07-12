package model.entities.plants;

import model.Game;
import model.Vec2;
import model.entities.Cell;
import model.entities.Sun;

/** تولیدکننده‌ی خورشید. دسته‌بندی کاربر. */
public class SunProducer extends Plant {

    private int stage = 1;          // فقط برای Sun-shroom (۱..۳)
    private double growthTimer = 0;

    public SunProducer(Plants type, Vec2 position) {
        super(type, type.getBaseHP(), type.getCost(), position, type.getDamage());
    }

    @Override
    public void onPlanted(Game game) {
        if (getType() == Plants.GOLD_BLOOM) {
            addSun(game, 375);      // یک‌باره
            removeSelf(game);       // سپس ناپدید
        }
    }

    @Override
    public void onTick(Game game) {
        if (getType() == Plants.GOLD_BLOOM) return;   // کارش در onPlanted بود
        if (getType() == Plants.SUN_SHROOM) grow();

        if (hasUncollectedSun(game)) return;          // تا برداشت نشود، تولید بعدی نه
        actionTimer += 1;
        if (actionTimer >= Game.secondsToTicks(getType().getActionInterval())) {
            actionTimer = 0;
            addSun(game, sunAmount());
        }
    }

    @Override
    public void onPlantFood(Game game) {
        int amount;
        switch (getType()) {
            case TWIN_SUNFLOWER:
                amount = 250;
                break;
            case PRIMAL_SUNFLOWER:
            case SUN_SHROOM:
                amount = 225;
                break;
            case GOLD_BLOOM:
                amount = 0;
                break;
            default:
                amount = 150;   // Sunflower
                break;
        }

        if (getType() == Plants.SUN_SHROOM) {
            stage = 3; // رشد آنی به مرحله آخر
        }

        if (amount > 0) {
            addSun(game, amount);
        }
    }

    // ---------- کمکی ----------
    private int sunAmount() {
        switch (getType()) {
            case TWIN_SUNFLOWER:
                return 100;
            case PRIMAL_SUNFLOWER:
                return 75;
            case SUN_SHROOM:
                if (stage == 1) {
                    return 25;
                } else if (stage == 2) {
                    return 50;
                } else {
                    return 75; // stage == 3
                }
            default:
                return 50; // Sunflower
        }
    }

    /** Sun-shroom: مرحله ۲ بعد از ۲۴ث، مرحله ۳ بعد از ۲۴+۷۲=۹۶ث. */
    private void grow() {
        if (stage >= 3) return;
        growthTimer += 1;
        if (stage == 1 && growthTimer >= Game.secondsToTicks(24)) stage = 2;
        else if (stage == 2 && growthTimer >= Game.secondsToTicks(96)) stage = 3;
    }

    private void addSun(Game game, int amount) {
        game.getSuns().add(new Sun(amount, new Vec2(getCol(), getRow())));
        System.out.println("plant " + getType().getName()
            + " produced a sun at (" + getCol() + ", " + getRow() + ")");
    }

    private boolean hasUncollectedSun(Game game) {
        for (Sun s : game.getSuns())
            if (!s.isFromSky() && s.getCol() == getCol() && s.getRow() == getRow()) return true;
        return false;
    }

    private void removeSelf(Game game) {
        game.getPlants().remove(this);
        Cell c = game.getField().getCell(getCol(), getRow());
        if (c != null) c.getPlants().remove(this);
    }
}
