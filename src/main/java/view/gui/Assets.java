package view.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import model.entities.plants.PlantAnimations;
import model.entities.plants.PlantData;
import model.entities.plants.PlantRecord;
import model.entities.plants.Plants;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;
import util.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public final class Assets implements Disposable {

    public static final String PORTAL =
            "768/INITIAL/UI/UNIVERSE/UNIVERSE_PORTAL/UNIVERSE_PORTAL.PAM";
    public static final String WORLD_LOCK =
            "768/INITIAL/UI/UNIVERSE/WORLD_LOCK/WORLD_LOCK.PAM";
    public static final String DIFFICULTY_METER =
            "768/DEV/UI/QUESTS/DIFFICULTY_METER/DIFFICULTY_METER.PAM";
    public static final String BEE = "768/INITIAL/ZEN_GARDEN/BEE/BEE.PAM";
    public static final String SKULL =
            "IMAGE_UI_HUD_INGAME_ZOMBOSS_HEALTH_METER_SKULL_ICON";

    private static final String PAM_ROOT = "assets/pvz";
    private static final String RESOLUTION = "768";
    private static final String BACKDROP_FOLDER = "assets/backgrounds";
    private static final String[] BACKDROPS =
            {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};

    private final Set<String> loaded = new HashSet<String>();
    private final Set<String> failed = new HashSet<String>();
    private final Map<String, Texture> files = new HashMap<String, Texture>();
    private final Random random = new Random();

    private final SpriteBatch batch = new SpriteBatch();
    private TextureBank textures;
    private PamPlayer player;

    public Assets() {
        FileHandle root = Gdx.files.local(PAM_ROOT);
        if (!root.exists()) {
            Log.warn("gui", "No PAM assets under " + PAM_ROOT + "; animations stay off");
            return;
        }
        try {
            textures = new TextureBank(RESOLUTION, root);
            player = new PamPlayer(textures, root);
        } catch (RuntimeException e) {
            Log.warn("gui", "Could not start the PAM player: " + e.getMessage());
            textures = null;
            player = null;
        }
    }

    public Batch batch() {
        return batch;
    }

    public Stage newStage() {
        return new Stage(new FitViewport(Theme.WORLD_WIDTH, Theme.WORLD_HEIGHT), batch);
    }

    public PamPlayer player() {
        return player;
    }

    public boolean load(String path) {
        if (player == null || path == null || failed.contains(path)) {
            return false;
        }
        if (loaded.contains(path)) {
            return true;
        }
        try {
            player.loadSync(path);
            loaded.add(path);
            return true;
        } catch (RuntimeException e) {
            Log.warn("gui", "Could not load " + path + ": " + e.getMessage());
            failed.add(path);
            return false;
        }
    }

    public boolean loadPlant(Plants plant) {
        PlantAnimations animations = animations(plant);
        return animations != null && animations.hasPlant() && load(animations.getPlant());
    }

    public PlantAnimations animations(Plants plant) {
        PlantRecord record = PlantData.record(plant);
        return record == null ? null : record.getAnimations();
    }

    public String plantPam(Plants plant) {
        PlantAnimations animations = animations(plant);
        return animations == null ? null : animations.getPlant();
    }

    public String plantEffect(Plants plant, String key) {
        PlantAnimations animations = animations(plant);
        return animations == null ? null : animations.effect(key);
    }

    public TextureRegion packetIcon(Plants plant) {
        PlantRecord record = PlantData.record(plant);
        return record == null ? null : region(record.getPacketIcon());
    }

    public TextureRegion packetBackground(Plants plant) {
        PlantRecord record = PlantData.record(plant);
        return record == null ? null : region(record.getPacketBackground());
    }

    public TextureRegion region(String imageId) {
        if (textures == null || imageId == null || failed.contains(imageId)) {
            return null;
        }
        try {
            TextureRegion found = textures.region(imageId);
            if (found == null) {
                failed.add(imageId);
                Log.warn("gui", "No image " + imageId + " in the PAM bundle");
            }
            return found;
        } catch (RuntimeException e) {
            Log.warn("gui", "Could not read " + imageId + ": " + e.getMessage());
            failed.add(imageId);
            return null;
        }
    }

    public Texture texture(String path) {
        Texture cached = files.get(path);
        if (cached != null) {
            return cached;
        }
        if (failed.contains(path)) {
            return null;
        }
        FileHandle file = Gdx.files.local(path);
        if (!file.exists()) {
            Log.debug("gui", "Missing file " + path);
            failed.add(path);
            return null;
        }
        try {
            Texture texture = new Texture(file);
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            files.put(path, texture);
            return texture;
        } catch (RuntimeException e) {
            Log.warn("gui", "Could not read " + path + ": " + e.getMessage());
            failed.add(path);
            return null;
        }
    }

    public TextureRegion regionFile(String path) {
        Texture texture = texture(path);
        return texture == null ? null : new TextureRegion(texture);
    }

    public TextureRegion randomBackdrop() {
        for (int attempt = 0; attempt < BACKDROPS.length; attempt++) {
            String letter = BACKDROPS[random.nextInt(BACKDROPS.length)];
            TextureRegion region =
                    regionFile(BACKDROP_FOLDER + "/backdrop_" + letter + ".png");
            if (region != null) {
                Log.info("gui", "Title backdrop " + letter);
                return region;
            }
        }
        Log.warn("gui", "No title backdrops found in " + BACKDROP_FOLDER);
        return null;
    }

    public void update() {
        if (textures != null) {
            textures.update();
        }
    }

    @Override
    public void dispose() {
        for (Texture texture : files.values()) {
            texture.dispose();
        }
        files.clear();
        if (textures != null) {
            textures.dispose();
            textures = null;
        }
        batch.dispose();
    }
}
