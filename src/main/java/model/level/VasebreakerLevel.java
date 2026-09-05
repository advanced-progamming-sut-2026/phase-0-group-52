package model.level;

import minigame.MinigameType;
import model.ChapterType;
import model.Game;
import model.Vec2;
import model.entities.Cell;
import model.entities.Vase;
import model.entities.plants.Plant;
import model.entities.plants.PlantCombat;
import model.entities.plants.PlantFactory;
import model.entities.plants.Plants;
import model.entities.zombies.Zombie;
import model.entities.zombies.ZombieFactory;
import model.entities.zombies.Zombies;

import java.util.ArrayList;
import java.util.List;

public class VasebreakerLevel extends MinigameLevel {

    public static final int FIRST_COLUMN = 2;
    public static final int VASES_BASE = 8;
    public static final int VASES_PER_LEVEL = 3;

    private static final Plants[] PRIZES = {
        Plants.PEASHOOTER, Plants.WALL_NUT, Plants.SNOW_PEA,
        Plants.CHERRY_BOMB, Plants.SQUASH, Plants.REPEATER,
    };

    private static final Zombies[] LURKERS = {
        Zombies.ZOMBIE_DEFAULT, Zombies.ZOMBIE_DEFAULT,
        Zombies.ZOMBIE_ARMOR1, Zombies.ZOMBIE_IMP,
    };

    private final List<Vase> vases = new ArrayList<Vase>();

    private boolean laidOut;

    public VasebreakerLevel(int levelnumber, ChapterType chaptertype,
            ArrayList<Plants> allowedplants) {
        super(MinigameType.VASE_BREAKER, levelnumber, chaptertype, allowedplants);
    }

    public List<Vase> getVases() {
        return vases;
    }

    public Vase vaseAt(int column, int row) {
        for (Vase vase : vases) {
            if (vase.getColumn() == column && vase.getRow() == row && !vase.isBroken()) {
                return vase;
            }
        }
        return null;
    }

    public int unbroken() {
        int left = 0;
        for (Vase vase : vases) {
            if (!vase.isBroken()) {
                left++;
            }
        }
        return left;
    }

    @Override
    public boolean usesChapterMechanics() {
        return false;
    }

    @Override
    public boolean isSkySunEnabled() {
        return false;
    }

    @Override
    public boolean areWavesHeld() {
        return true;
    }

    @Override
    public String objective() {
        return "Smash every vase - some hide a plant, some hide a zombie.";
    }

    public Plants smash(Game game, int column, int row) {
        Vase vase = vaseAt(column, row);
        if (vase == null) {
            return null;
        }
        vase.breakOpen();
        Cell cell = game.getField() == null ? null : game.getField().getCell(column, row);
        if (cell != null && cell.getType() == model.entities.CellType.TOMBSTONE) {
            cell.setType(model.entities.CellType.NORMAL);
        }
        Vase.Content opened = vase.getContent() == Vase.Content.MYSTERY
                ? (PlantCombat.RANDOM.nextBoolean() ? Vase.Content.PLANT : Vase.Content.ZOMBIE)
                : vase.getContent();
        if (opened == Vase.Content.ZOMBIE) {
            game.getZombies().add(ZombieFactory.create(vase.getZombie(), row,
                    column, getChaptertype()));
            return null;
        }
        return vase.getPlant();
    }

    public boolean place(Game game, Plants type, int column, int row) {
        Cell cell = game.getField() == null ? null : game.getField().getCell(column, row);
        if (type == null || cell == null || !cell.isPlantable() || !cell.isEmpty()
                || vaseAt(column, row) != null) {
            return false;
        }
        Plant grown = PlantFactory.create(type, new Vec2(column, row));
        cell.getPlants().add(grown);
        game.getPlants().add(grown);
        grown.onPlanted(game);
        return true;
    }

    @Override
    public void onTick(Game game) {
        if (!laidOut) {
            laidOut = true;
            layOutVases(game);
        }
    }

    private void layOutVases(Game game) {
        if (game.getField() == null) {
            return;
        }
        int rows = game.getField().getRows();
        int columns = game.getField().getCols();
        int wanted = VASES_BASE + VASES_PER_LEVEL * Math.max(0, getLevelnumber() - 1);
        int tries = 0;
        while (vases.size() < wanted && tries < 500) {
            tries++;
            int row = PlantCombat.RANDOM.nextInt(rows);
            int column = FIRST_COLUMN
                    + PlantCombat.RANDOM.nextInt(Math.max(1, columns - FIRST_COLUMN));
            if (vaseAt(column, row) != null) {
                continue;
            }
            vases.add(new Vase(column, row, rollContent(),
                    PRIZES[PlantCombat.RANDOM.nextInt(PRIZES.length)],
                    LURKERS[PlantCombat.RANDOM.nextInt(LURKERS.length)]));
        }
    }

    private Vase.Content rollContent() {
        int roll = PlantCombat.RANDOM.nextInt(100);
        if (roll < 45) {
            return Vase.Content.PLANT;
        }
        return roll < 78 ? Vase.Content.ZOMBIE : Vase.Content.MYSTERY;
    }

    @Override
    public String checkVictory(Game game) {
        if (!laidOut || unbroken() > 0) {
            return null;
        }
        for (Zombie zombie : game.getZombies()) {
            if (!zombie.isDead()) {
                return null;
            }
        }
        return "Every vase broken and every zombie gone. You win!";
    }
}
