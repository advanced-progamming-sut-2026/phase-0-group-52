package view.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;
import util.Log;

import java.util.HashSet;
import java.util.Set;

public final class Pam implements Disposable {
    public static final String PORTAL =
            "768/INITIAL/UI/UNIVERSE/UNIVERSE_PORTAL/UNIVERSE_PORTAL.PAM";
    public static final String WORLD_LOCK =
            "768/INITIAL/UI/UNIVERSE/WORLD_LOCK/WORLD_LOCK.PAM";
    public static final String DIFFICULTY_METER =
            "768/DEV/UI/QUESTS/DIFFICULTY_METER/DIFFICULTY_METER.PAM";
    public static final String BEE =
            "768/INITIAL/ZEN_GARDEN/BEE/BEE.PAM";
    public static final String SKULL =
            "IMAGE_UI_HUD_INGAME_ZOMBOSS_HEALTH_METER_SKULL_ICON";

    private static final String ROOT = "assets/pvz";
    private static final String RESOLUTION = "768";

    private final Set<String> loaded = new HashSet<String>();
    private final Set<String> failed = new HashSet<String>();

    private TextureBank textures;
    private PamPlayer player;

    public Pam() {
        FileHandle root = Gdx.files.local(ROOT);
        if (!root.exists()) {
            Log.warn("gui", "No PAM assets under " + ROOT + "; animations stay off");
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

    public PamPlayer player() {
        return player;
    }

    public boolean load(String path) {
        if (player == null || failed.contains(path)) {
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

    public com.badlogic.gdx.graphics.g2d.TextureRegion region(String imageId) {
        if (textures == null || failed.contains(imageId)) {
            return null;
        }
        try {
            com.badlogic.gdx.graphics.g2d.TextureRegion found = textures.region(imageId);
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

    public void update() {
        if (textures != null) {
            textures.update();
        }
    }

    @Override
    public void dispose() {
        if (textures != null) {
            textures.dispose();
        }
    }
}
