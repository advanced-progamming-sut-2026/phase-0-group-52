package model.level;

import model.ChapterType;
import model.Game;
import model.entities.plants.Plants;
import model.entities.zombies.Zombie;

import java.util.ArrayList;

public class DeadLine extends Level {

    private int deadlineCol = 2;

    public DeadLine(int levelnumber, ChapterType chaptertype,
                    ArrayList<Plants> allowedplants, AttackPattern attackPattern) {
        super(levelnumber, chaptertype, allowedplants, attackPattern);
    }

    public int getDeadlineCol() {
        return deadlineCol;
    }

    public void setDeadlineCol(int deadlineCol) {
        this.deadlineCol = deadlineCol;
    }

    @Override
    public String checkDefeat(Game game) {
        for (Zombie z : game.getZombies())
            if (!z.isHypnotized() && z.getPosition().x < deadlineCol)
                return "A zombie crossed the dead line. You lose!";
        return null;
    }
}
