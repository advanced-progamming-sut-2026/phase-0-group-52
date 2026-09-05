package minigame;

import model.App;
import model.Game;
import model.LevelBuilder;
import model.level.MinigameLevel;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinigameCatalogueTest {

    @Test
    void everyLawnMinigameBuildsAndCarriesItsOwnLevel() {
        for (MinigameType kind : MinigameType.values()) {
            Game game = LevelBuilder.buildMinigame(App.getInstance(), kind, 1);
            if (!kind.isLawnBased()) {
                assertNull(game, kind + " is not a lawn game and must not build one");
                continue;
            }
            assertNotNull(game, kind + " must build");
            assertTrue(game.getLevel() instanceof MinigameLevel,
                    kind + " must carry a MinigameLevel");
            assertEquals(kind, ((MinigameLevel) game.getLevel()).getKind(),
                    kind + " built the wrong level");
        }
    }

    @Test
    void everyMinigameHasAUniqueNameIconAndTag() {
        Set<String> names = new HashSet<String>();
        Set<String> icons = new HashSet<String>();
        Set<String> tags = new HashSet<String>();
        for (MinigameType kind : MinigameType.values()) {
            assertTrue(names.add(kind.getDisplayName()), kind + " shares a name");
            assertTrue(icons.add(kind.getIconName()), kind + " shares an icon file");
            assertTrue(tags.add(kind.getTag()), kind + " shares a tag");
            assertFalse(kind.getBlurb().isEmpty(), kind + " needs a blurb");
        }
    }

    @Test
    void joustIsListedButNotPlayableYet() {
        assertFalse(MinigameType.JOUST.isPlayable(),
                "Joust lands in phase 3, so it must stay locked");
        assertFalse(MinigameType.JOUST.isLawnBased(),
                "and it must not try to build a lawn");
        for (MinigameType kind : MinigameType.values()) {
            if (kind != MinigameType.JOUST) {
                assertTrue(kind.isPlayable(), kind + " should be playable");
            }
        }
    }

    @Test
    void theKeyLookupAcceptsEveryNameSpellingTheUiUses() {
        for (MinigameType kind : MinigameType.values()) {
            assertEquals(kind, MinigameType.byKey(kind.name()), "enum name");
            assertEquals(kind, MinigameType.byKey(kind.getIconName()), "icon name");
            assertEquals(kind, MinigameType.byKey(
                    kind.name().toLowerCase().replace('_', '-')), "kebab name");
        }
        assertNull(MinigameType.byKey("not-a-game"), "unknown keys stay null");
    }

    @Test
    void aBoardsOwnRulesReplaceTheChapterTerrainWhereTheyWouldClash() {
        MinigameType[] ownBoard = {
            MinigameType.VASE_BREAKER, MinigameType.WALLNUT_BOWLING, MinigameType.I_ZOMBIE,
        };
        for (MinigameType kind : ownBoard) {
            Game game = LevelBuilder.buildMinigame(App.getInstance(), kind, 1);
            assertFalse(((MinigameLevel) game.getLevel()).usesChapterMechanics(),
                    kind + " lays out its own board, so chapter terrain must stay off");
        }
    }

    @Test
    void vasesNeverGiveAwayWhatIsInsideThem() {
        Game game = LevelBuilder.buildMinigame(App.getInstance(),
                MinigameType.VASE_BREAKER, 1);
        model.level.VasebreakerLevel level =
                (model.level.VasebreakerLevel) game.getLevel();
        level.onTick(game);
        assertTrue(level.unbroken() > 0, "a vasebreaker board has vases on it");
        int smashed = 0;
        for (model.entities.Vase vase : level.getVases()) {
            assertFalse(vase.isBroken(), "vases start whole");
            if (smashed == 0) {
                level.smash(game, vase.getColumn(), vase.getRow());
                assertTrue(vase.isBroken(), "smashing opens it");
                smashed++;
            }
        }
        assertEquals(level.getVases().size() - 1, level.unbroken(),
                "exactly one vase went");
    }
}
