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

    @Test
    void gravesStayOffTheBackOfTheLawnAndNeverFillIt() {
        int lastColumn = 0;
        int nearBack = 0;
        int total = 0;
        for (int run = 0; run < 60; run++) {
            Game game = lawn();
            DarkAgesMechanics dark = new DarkAgesMechanics();
            for (int wave = 0; wave < 12; wave++) {
                dark.onWaveStart(game);
            }
            int cols = game.getField().getCols();
            assertTrue(game.getTombstones().size() <= 5,
                    "a lawn should never hold more than five graves, saw "
                            + game.getTombstones().size());
            for (Tombstone stone : game.getTombstones()) {
                total++;
                if (stone.getColumn() == cols - 1) {
                    lastColumn++;
                }
                if (stone.getColumn() >= cols - 3) {
                    nearBack++;
                }
            }
        }
        assertEquals(0, lastColumn, "nothing may rise on the very last column");
        assertTrue(nearBack * 100 < total * 20,
                "graves near the mowers should be rare, saw " + nearBack + " of " + total);
    }

    @Test
    void gravesFavourTheEmptyKindSevenTimesOutOfTen() {
        int plain = 0;
        int sun = 0;
        int food = 0;
        for (int run = 0; run < 120; run++) {
            Game game = lawn();
            DarkAgesMechanics dark = new DarkAgesMechanics();
            for (int wave = 0; wave < 6; wave++) {
                dark.onWaveStart(game);
            }
            for (Tombstone stone : game.getTombstones()) {
                if ("sun".equals(stone.getBonus())) {
                    sun++;
                } else if (stone.getBonus() != null) {
                    food++;
                } else {
                    plain++;
                }
            }
        }
        int total = plain + sun + food;
        assertTrue(plain > sun && sun > food,
                "plain graves are commonest, then sun, then plant food: "
                        + plain + "/" + sun + "/" + food);
        assertTrue(plain * 100 > total * 55,
                "most graves should be plain, saw " + plain + " of " + total);
    }
}
