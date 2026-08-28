package database;

import model.ChapterType;
import model.User;
import model.adventure.AdventureProgress;
import model.adventure.ChapterMap;
import model.entities.plants.PlantData;
import model.entities.plants.Plants;
import org.junit.jupiter.api.Test;
import util.Json;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdventurePersistenceTest {

    @Test
    void claimedIslandsSurviveASaveAndLoad() {
        UserRepository repository = new UserRepository();
        User saved = new User();
        saved.setUsername("mapwalker");

        ChapterType egypt = ChapterType.ANCIENT_EGYPT;
        ChapterType beach = ChapterType.BIG_WAVE_BEACH;
        List<Plants> egyptPlants = PlantData.ofChapter(egypt);
        saved.getAdventure().record(egypt, 0, egyptPlants.get(0));
        saved.getAdventure().record(egypt, 7, egyptPlants.get(4));
        saved.getAdventure().record(beach, 2, PlantData.ofChapter(beach).get(1));

        User loaded = roundTrip(repository, saved);
        AdventureProgress progress = loaded.getAdventure();

        assertEquals(egyptPlants.get(0), progress.claimedPlant(egypt, 0));
        assertEquals(egyptPlants.get(4), progress.claimedPlant(egypt, 7));
        assertEquals(2, progress.claimedCount(egypt));
        assertEquals(1, progress.claimedCount(beach));
        assertFalse(progress.isClaimed(egypt, 1));
        assertEquals(ChapterMap.PLANT_ISLANDS - 2, progress.remaining(egypt).size());
    }

    @Test
    void anUntouchedMapLoadsEmptyRatherThanGuessing() {
        UserRepository repository = new UserRepository();
        User saved = new User();
        saved.setUsername("newcomer");

        User loaded = roundTrip(repository, saved);
        for (ChapterType chapter : ChapterType.values()) {
            assertEquals(0, loaded.getAdventure().claimedCount(chapter), chapter.name());
            assertEquals(ChapterMap.PLANT_ISLANDS,
                    loaded.getAdventure().remaining(chapter).size(), chapter.name());
        }
    }

    @Test
    void aSaveWrittenBeforeTheMapExistedStillLoads() {
        UserRepository repository = new UserRepository();
        Object parsed = Json.parse("{\"username\":\"veteran\",\"lastChapter\":2,\"lastLevel\":3}");
        assertTrue(parsed instanceof Map);

        User loaded = repository.fromMap((Map<?, ?>) parsed);
        assertEquals("veteran", loaded.getUsername());
        assertEquals(2, loaded.getLastChapter());
        assertEquals(0, loaded.getAdventure().claimedCount(ChapterType.ANCIENT_EGYPT));
    }

    private User roundTrip(UserRepository repository, User user) {
        Object parsed = Json.parse(repository.toJson(user));
        assertTrue(parsed instanceof Map, "the repository wrote something that is not an object");
        return repository.fromMap((Map<?, ?>) parsed);
    }
}
