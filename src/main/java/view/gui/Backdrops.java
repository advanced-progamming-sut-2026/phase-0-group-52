package view.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import util.Log;

import java.util.Random;

public final class Backdrops implements Disposable {
    private static final String FOLDER = "assets/backgrounds";
    private static final String[] LETTERS = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};
    private static final int CONTENT_WIDTH = 1024;
    private static final int CONTENT_HEIGHT = 768;

    private final Random random = new Random();
    private Texture texture;

    public TextureRegion random() {
        dispose();
        for (int attempt = 0; attempt < LETTERS.length; attempt++) {
            String letter = LETTERS[random.nextInt(LETTERS.length)];
            TextureRegion region = load(letter);
            if (region != null) {
                Log.info("gui", "Title backdrop " + letter);
                return region;
            }
        }
        Log.warn("gui", "No title backdrops found in " + FOLDER);
        return null;
    }

    private TextureRegion load(String letter) {
        com.badlogic.gdx.files.FileHandle file =
                Gdx.files.local(FOLDER + "/backdrop_" + letter + ".png");
        if (!file.exists()) {
            return null;
        }
        try {
            texture = new Texture(file);
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            int width = Math.min(CONTENT_WIDTH, texture.getWidth());
            int height = Math.min(CONTENT_HEIGHT, texture.getHeight());
            return new TextureRegion(texture, 0, 0, width, height);
        } catch (RuntimeException e) {
            Log.warn("gui", "Could not read " + file.name() + ": " + e.getMessage());
            return null;
        }
    }

    @Override
    public void dispose() {
        if (texture != null) {
            texture.dispose();
            texture = null;
        }
    }
}
