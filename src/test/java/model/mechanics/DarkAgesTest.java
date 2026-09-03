package model.mechanics;

import model.ChapterType;
import model.Game;
import model.GameField;
import model.GameLoop;
import model.entities.Cell;
import model.entities.CellType;
import model.entities.Tombstone;
import model.entities.plants.Plant;
import model.entities.zombies.Zombies;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DarkAgesTest {

    private Game lawn() {
        GameField field = new GameField(ChapterType.DARK_AGES);
        return new Game(null, null, null, field, 0, new ArrayList<Plant>(),
                new ArrayList<model.Wave>());
    }

    @Test
    void aGraveThatRisesIsARealEntityYouCanShoot() {
        Game game = lawn();
        DarkAgesMechanics dark = new DarkAgesMechanics();
        dark.onWaveStart(game);
        assertFalse(game.getTombstones().isEmpty(),
                "a wave should raise graves you can actually hit");
        for (Tombstone stone : game.getTombstones()) {
            Cell cell = game.getField().getCell(stone.getColumn(), stone.getRow());
            assertEquals(CellType.TOMBSTONE, cell.getType(),
                    "the cell and the entity must agree");
        }
    }

    @Test
    void breakingAGraveClearsItsCell() {
        Game game = lawn();
        DarkAgesMechanics dark = new DarkAgesMechanics();
        dark.onWaveStart(game);
        Tombstone stone = game.getTombstones().get(0);
        int column = stone.getColumn();
        int row = stone.getRow();
        stone.takeDamage(99999d);
        dark.onTick(game);
        assertFalse(game.getTombstones().contains(stone), "the broken grave is gone");
        assertEquals(CellType.NORMAL, game.getField().getCell(column, row).getType(),
                "and its cell is plantable again");
    }

    @Test
    void everyDarkAgesZombieIsOnTheRoster() {
        java.util.List<Zombies> late =
                model.level.WavePlan.roster(ChapterType.DARK_AGES, 3);
        for (Zombies wanted : new Zombies[] {Zombies.ZOMBIE_DEFAULT,
            Zombies.ZOMBIE_DARK_ARMOR3, Zombies.ZOMBIE_DARK_JUGGLER,
            Zombies.ZOMBIE_WIZARD, Zombies.ZOMBIE_DARK_IMP_DRAGON,
            Zombies.ZOMBIE_DARK_KING}) {
            assertTrue(late.contains(wanted), wanted.name() + " never shows up");
        }
    }

    @Test
    void aGraveWearsTheArtOfWhatIsInsideIt() {
        assertTrue(view.gui.ChapterArt.gravestone(ChapterType.DARK_AGES, "sun")
                .contains("DARK_SUN"), "a sun grave looks like one");
        assertTrue(view.gui.ChapterArt.gravestone(ChapterType.DARK_AGES, "plant food")
                .contains("DARK_PLANTFOOD"), "a plant food grave looks like one");
        assertTrue(view.gui.ChapterArt.gravestone(ChapterType.DARK_AGES, null)
                .contains("DARK_NOOP"), "an empty grave is plain");
        assertTrue(view.gui.ChapterArt.gravestone(ChapterType.ANCIENT_EGYPT, null)
                .contains("EGYPT"), "Egypt keeps its own stone");
    }
}
