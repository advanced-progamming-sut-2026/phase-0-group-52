package model.level;

import minigame.MinigameType;
import model.ChapterType;
import model.Game;
import model.entities.BowlingNut;
import model.entities.plants.PlantCombat;
import model.entities.plants.Plants;
import model.entities.zombies.Zombie;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class WallNutBowlingLevel extends MinigameLevel {

    public static final int BOWLING_COLUMNS = 3;
    public static final int BELT_CAPACITY = 5;
    public static final int DELIVERY_INTERVAL = 18;
    public static final int EXPLOSIVE_ODDS = 22;
    public static final int TALL_ODDS = 18;
    public static final double NUT_DAMAGE = 1800d;
    public static final double BLAST_DAMAGE = 1800d;

    private final Queue<Plants> belt = new LinkedList<Plants>();
    private final List<BowlingNut> nuts = new ArrayList<BowlingNut>();

    private int timer;
    private boolean stocked;

    public WallNutBowlingLevel(int levelnumber, ChapterType chaptertype,
            ArrayList<Plants> allowedplants) {
        super(MinigameType.WALLNUT_BOWLING, levelnumber, chaptertype, allowedplants);
    }

    public Queue<Plants> getBelt() {
        return belt;
    }

    public List<BowlingNut> getNuts() {
        return nuts;
    }

    public boolean isBowlingColumn(int column) {
        return column >= 0 && column < BOWLING_COLUMNS;
    }

    public boolean roll(Game game, int column, int row) {
        if (belt.isEmpty() || !isBowlingColumn(column)) {
            return false;
        }
        Plants nut = belt.poll();
        nuts.add(new BowlingNut(row, column, nut == Plants.EXPLODE_O_NUT,
                nut == Plants.TALL_NUT));
        return true;
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
    public void onTick(Game game) {
        if (!stocked) {
            stocked = true;
            for (int i = 0; i < 3; i++) {
                deliver();
            }
        }
        timer++;
        if (timer >= DELIVERY_INTERVAL) {
            timer = 0;
            deliver();
        }
        rollNuts(game);
    }

    private void deliver() {
        if (belt.size() >= BELT_CAPACITY) {
            return;
        }
        int roll = PlantCombat.RANDOM.nextInt(100);
        belt.add(roll < EXPLOSIVE_ODDS ? Plants.EXPLODE_O_NUT
                : roll < EXPLOSIVE_ODDS + TALL_ODDS ? Plants.TALL_NUT : Plants.WALL_NUT);
    }

    private void rollNuts(Game game) {
        int rows = game.getField() == null ? 5 : game.getField().getRows();
        int columns = game.getField() == null ? 9 : game.getField().getCols();
        for (BowlingNut nut : new ArrayList<BowlingNut>(nuts)) {
            nut.advance(rows, columns);
            strike(game, nut, rows);
        }
        Iterator<BowlingNut> gone = nuts.iterator();
        while (gone.hasNext()) {
            if (gone.next().isSpent()) {
                gone.remove();
            }
        }
    }

    private void strike(Game game, BowlingNut nut, int rows) {
        for (Zombie zombie : new ArrayList<Zombie>(game.getZombies())) {
            if (zombie.getRow() != nut.getRow() || zombie.isDead()) {
                continue;
            }
            if (Math.abs(zombie.getPosition().x - nut.getColumn()) > BowlingNut.HIT_RANGE) {
                continue;
            }
            if (nut.isExplosive()) {
                blast(game, nut);
                nut.spend();
                return;
            }
            zombie.setHp(zombie.getHp() - NUT_DAMAGE);
            nut.bounce(rows);
            return;
        }
    }

    private void blast(Game game, BowlingNut nut) {
        for (Zombie zombie : new ArrayList<Zombie>(game.getZombies())) {
            double dx = zombie.getPosition().x - nut.getColumn();
            double dy = zombie.getRow() - nut.getLane();
            if (Math.sqrt(dx * dx + dy * dy) <= BowlingNut.BLAST_RANGE) {
                zombie.setHp(zombie.getHp() - BLAST_DAMAGE);
            }
        }
    }
}
