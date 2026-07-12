package controller;

import model.App;
import model.Game;
import model.GameEngine;
import model.GameField;
import model.entities.Cell;
import model.entities.CellType;
import model.entities.Lawnmower;
import model.entities.plants.Plant;
import model.entities.plants.Plants;
import model.entities.zombies.Zombie;
import model.enums.Menu;

import java.util.Map;

public class GameMenuController {

    private GameEngine engine() { return App.getInstance().getGameEngine(); }
    private Game game()         { return App.getInstance().getGame(); }

    /** اگر بازی فعالی نباشد پیام می‌دهد و true برمی‌گرداند. */
    private boolean noGame() {
        if (engine() == null || game() == null) {
            System.out.println("No active game. Start a game first.");
            return true;
        }
        return false;
    }

    // ---------- گذر زمان و خورشید ----------
    public void advanceTime(int count) {
        if (noGame()) return;
        engine().advance(count);
    }

    public void collectSun(int x, int y) {
        if (noGame()) return;
        boolean ok = engine().collectSun(x, y);
        System.out.println(ok ? "Collected sun at (" + x + ", " + y + ")."
            : "No sun to collect at (" + x + ", " + y + ").");
    }

    public void showSunAmount() {
        if (noGame()) return;
        System.out.println("Sun: " + game().getSunAmount());
    }

    // ---------- کاشت / برداشت / غذا ----------
    public void plantPlant(String typeStr, int x, int y) {
        if (noGame()) return;
        Plants type = parsePlant(typeStr);
        if (type == null) { System.out.println("Unknown plant: " + typeStr); return; }
        System.out.println(engine().plantPlant(type, x, y));
    }

    public void pluckPlant(int x, int y) {
        if (noGame()) return;
        System.out.println(engine().pluckPlant(x, y));
    }

    public void feedPlant(int x, int y) {
        if (noGame()) return;
        System.out.println(engine().feedPlant(x, y));
    }

    // ---------- تقلب‌ها ----------
    public void cheatAddSun(int n) {
        if (noGame()) return;
        engine().cheatAddSun(n);
        System.out.println("Added " + n + " suns. Sun: " + game().getSunAmount());
    }

    public void cheatRemoveCooldown() {
        if (noGame()) return;
        engine().cheatRemoveCooldown();
        System.out.println("All cooldowns cleared.");
    }

    public void cheatAddPlantFood() {
        if (noGame()) return;
        engine().cheatAddPlantFood();
        System.out.println("Plant food: " + game().getPlantFoodCount());
    }

    public void releaseNuke() {
        if (noGame()) return;
        engine().releaseNuke();
    }

    // ---------- نمایش ----------
    public void showMap() {
        if (noGame()) return;
        Game g = game();
        GameField f = g.getField();
        System.out.println("Wave: " + (g.getCurrentWaveIndex() + 1)
            + " | Sun: " + g.getSunAmount()
            + " | Plant food: " + g.getPlantFoodCount()
            + " | Tick: " + g.getCurrentTick());
        for (int row = 0; row < f.getRows(); row++) {
            StringBuilder sb = new StringBuilder();
            Lawnmower m = f.getLawnmower(row);
            sb.append(m != null && m.isIsactive() ? "[M]" : "[ ]");
            for (int col = 0; col < f.getCols(); col++) {
                sb.append(' ').append(cellToken(g, f, col, row));
            }
            System.out.println(sb);
        }
    }

    private char cellToken(Game g, GameField f, int col, int row) {
        Cell c = f.getCell(col, row);
        for (Zombie z : g.getZombies())
            if (z.getRow() == row && z.getCol() == col) return 'Z';
        if (!c.getPlants().isEmpty()) return 'P';
        return terrainChar(c.getType());
    }

    private char terrainChar(CellType t) {
        switch (t) {
            case NORMAL:
                return '.';
            case TOMBSTONE:
                return 'T';
            case WATER:
                return '~';
            case FROZEN:
                return '*';
            case SLIPPERY_UP:
                return '^';
            case SLIPPERY_DOWN:
                return 'v';
            case LOW_GROUND:
                return '_';
            case NECROMANCY:
                return 'N';
            default:
                return '?';
        }
    }

    public void showPlantsStatus() {
        if (noGame()) return;
        // TODO (گام ۹): لیست واقعی = گیاهانِ انتخاب‌شده در ChoosePlant. فعلاً از روی cooldownها.
        Map<Plants, Double> cd = game().getCooldowns();
        if (cd.isEmpty()) { System.out.println("No plant cooldowns active."); return; }
        for (Map.Entry<Plants, Double> e : cd.entrySet()) {
            double rem = e.getValue();
            if (rem > 0)
                System.out.println(e.getKey().getName() + ": cooling down ("
                    + (int) Math.ceil(rem) + " ticks left)");
            else
                System.out.println(e.getKey().getName() + ": ready (cost " + e.getKey().getCost() + ")");
        }
    }

    public void showTileStatus(int x, int y) {
        if (noGame()) return;
        Cell c = game().getField().getCell(x, y);
        if (c == null) { System.out.println("Invalid tile."); return; }
        System.out.println("Tile (" + x + ", " + y + ") type: " + c.getType());
        for (Plant p : c.getPlants())
            System.out.println("  Plant: " + p.getType().getName() + " HP=" + p.getHp());
        for (Zombie z : game().getZombies())
            if (z.getRow() == y && z.getCol() == x)
                System.out.println("  Zombie: " + z.getClass().getSimpleName() + " HP=" + z.getHp());
    }

    // ---------- ناوبری منو ----------
    public void showCurrentMenu() {
        System.out.println("You are now in the Game menu.");
    }

    public void exitMenu() {
        App.getInstance().setCurrentMenu(Menu.MainMenu);
    }

    // ---------- کمکی ----------
    /** نامِ گیاه را به enum تبدیل می‌کند (چه نام enum مثل SUNFLOWER، چه نام نمایشی مثل Sunflower). */
    private Plants parsePlant(String s) {
        String norm = s.trim().toUpperCase().replace('-', '_').replace(' ', '_');
        try {
            return Plants.valueOf(norm);
        } catch (IllegalArgumentException ignored) { }
        for (Plants p : Plants.values())
            if (p.getName().equalsIgnoreCase(s.trim()) || p.name().equalsIgnoreCase(norm))
                return p;
        return null;
    }
}
