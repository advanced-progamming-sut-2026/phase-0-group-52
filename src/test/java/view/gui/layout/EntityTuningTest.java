package view.gui.layout;

import com.badlogic.gdx.Gdx;
import model.entities.Projectile;
import view.gui.EntityTuning;
import model.entities.plants.Plants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityTuningTest {

    @TempDir
    java.io.File temp;

    @BeforeEach
    void sandbox() {
        Gdx.files = new SandboxFiles(temp);
        EntityTuning.reset();
    }

    private String key(Plants plant, String port, String state) {
        return EntityTuning.shotKey(plant, port, state);
    }

    @Test
    void everyMuzzleKeepsItsOwnTuneAcrossASaveAndLoad() {
        String up = key(Plants.THREEPEATER, "up", "");
        String mid = key(Plants.THREEPEATER, "mid", "");
        String down = key(Plants.THREEPEATER, "down", "");
        EntityTuning.edit(up).dy = 11f;
        EntityTuning.edit(mid).dy = 22f;
        EntityTuning.edit(down).dy = 33f;
        EntityTuning.touch();
        EntityTuning.reset();

        assertEquals(11f, EntityTuning.of(up).dy, 0.01f, "the up muzzle lost its tune");
        assertEquals(22f, EntityTuning.of(mid).dy, 0.01f, "the mid muzzle lost its tune");
        assertEquals(33f, EntityTuning.of(down).dy, 0.01f, "the down muzzle lost its tune");
    }

    @Test
    void aStateKeepsItsOwnTuneAcrossASaveAndLoad() {
        String plain = key(Plants.REPEATER, "trail", "");
        String fed = key(Plants.REPEATER, "trail", Projectile.FED);
        EntityTuning.edit(plain).dx = 5f;
        EntityTuning.edit(fed).dx = 40f;
        EntityTuning.touch();
        EntityTuning.reset();

        assertEquals(5f, EntityTuning.of(plain).dx, 0.01f, "the plain shot lost its tune");
        assertEquals(40f, EntityTuning.of(fed).dx, 0.01f, "the fed shot lost its tune");
    }

    @Test
    void aLegacyPerWorldZombieKeyStillCollapsesOntoTheGlobalOne() {
        Gdx.files.local("assets/" + EntityTuning.FILE).writeString(
                "{\n  \"zombie|ZOMBIE_DEFAULT|ANCIENT_EGYPT\": {\"dx\": 7, \"dy\": 0,"
                        + " \"scale\": 1, \"speed\": 1, \"dw\": 0, \"dh\": 0}\n}\n",
                false, "UTF-8");
        assertEquals(7f, EntityTuning.of(EntityTuning.zombieKey("ZOMBIE_DEFAULT")).dx, 0.01f,
                "the old per-world zombie tune should still migrate");
    }

    @Test
    void aTuneFallsBackThroughPlantThenKind() {
        EntityTuning.edit(EntityTuning.kindKey(Plants.PEASHOOTER)).dy = 9f;
        assertEquals(9f, EntityTuning.shotTune(Plants.PEASHOOTER, "main", "").dy, 0.01f,
                "a plant with no tune of its own should inherit its kind's");
        EntityTuning.edit(key(Plants.PEASHOOTER, "main", "")).dy = 4f;
        assertEquals(4f, EntityTuning.shotTune(Plants.PEASHOOTER, "main", "").dy, 0.01f,
                "its own tune should win once it has one");
        assertTrue(EntityTuning.has(key(Plants.PEASHOOTER, "main", "")),
                "the plant key should exist after editing it");
    }
}
