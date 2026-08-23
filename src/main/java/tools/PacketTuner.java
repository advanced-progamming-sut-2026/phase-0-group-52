package tools;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import model.entities.plants.PlantData;
import model.entities.plants.PlantRecord;
import model.entities.plants.Plants;
import view.gui.Assets;
import view.gui.PacketLayout;

public final class PacketTuner extends ApplicationAdapter {

    private static final int WIDTH = 980;
    private static final int HEIGHT = 620;
    private static final float ZOOM = 6f;
    private static final float CARD_X = 120f;
    private static final float CARD_Y = 180f;

    private Assets assets;
    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private BitmapFont font;
    private int index;
    private String status = "";

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Seed packet tuner");
        config.setWindowedMode(WIDTH, HEIGHT);
        new Lwjgl3Application(new PacketTuner(), config);
    }

    @Override
    public void create() {
        assets = new Assets();
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        font = new BitmapFont();
        font.getData().setScale(1.2f);
    }

    private Plants plant() {
        return Plants.values()[Math.floorMod(index, Plants.values().length)];
    }

    private boolean fast() {
        return Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
                || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
    }

    private void handleInput() {
        Plants current = plant();
        PacketLayout.Placement p = PacketLayout.all().get(current);
        if (p == null) {
            p = PacketLayout.fallback(current);
            PacketLayout.all().put(current, p);
        }
        float step = fast() ? 5f : 1f;
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            p.move(-step, 0f);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            p.move(step, 0f);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            p.move(0f, -step);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            p.move(0f, step);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
            p.zoom(fast() ? -0.1f : -0.02f);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
            p.zoom(fast() ? 0.1f : 0.02f);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            PacketLayout.all().put(current, PacketLayout.fallback(current));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT_BRACKET)) {
            index--;
            status = "";
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT_BRACKET)) {
            index++;
            status = "";
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            PacketLayout.save();
            status = "saved " + PacketLayout.PATH;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
    }

    @Override
    public void render() {
        handleInput();
        Gdx.gl.glClearColor(0.13f, 0.15f, 0.17f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Plants current = plant();
        PlantRecord record = PlantData.record(current);
        PacketLayout.Placement p = PacketLayout.of(current);

        batch.begin();
        drawCard(record, p);
        drawText(current, record, p);
        batch.end();

        drawGuides();
    }

    private void drawCard(PlantRecord record, PacketLayout.Placement p) {
        if (record == null) {
            return;
        }
        float cardH = PacketLayout.CARD_HEIGHT * ZOOM;
        TextureRegion bg = assets.region(record.getPacketBackground());
        if (bg != null) {
            batch.draw(bg, CARD_X, CARD_Y, PacketLayout.CARD_WIDTH * ZOOM, cardH);
        }
        TextureRegion icon = assets.region(record.getPacketIcon());
        if (icon == null) {
            return;
        }
        float w = record.getIconWidth() * p.getScale() * ZOOM;
        float h = record.getIconHeight() * p.getScale() * ZOOM;
        float x = CARD_X + p.getX() * ZOOM;
        float y = CARD_Y + cardH - p.getY() * ZOOM - h;
        batch.draw(icon, x, y, w, h);
    }

    private void drawText(Plants current, PlantRecord record, PacketLayout.Placement p) {
        float x = 120f;
        float y = 150f;
        font.setColor(Color.WHITE);
        font.draw(batch, (index % Plants.values().length + 1) + " / " + Plants.values().length
                + "   " + current.getName(), x, y);
        if (record != null) {
            font.setColor(Color.LIGHT_GRAY);
            font.draw(batch, String.format("x %.0f   y %.0f   scale %.2f   native %dx%d",
                    p.getX(), p.getY(), p.getScale(),
                    record.getIconWidth(), record.getIconHeight()), x, y - 26f);
        }
        font.setColor(Color.GRAY);
        font.draw(batch, "arrows move (shift x5)   Z / X zoom (shift x5)   "
                + "[ ] prev/next   R reset   S save   Esc quit", x, y - 56f);
        if (!status.isEmpty()) {
            font.setColor(Color.LIME);
            font.draw(batch, status, x, y - 84f);
        }
    }

    private void drawGuides() {
        float cardW = PacketLayout.CARD_WIDTH * ZOOM;
        float cardH = PacketLayout.CARD_HEIGHT * ZOOM;
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(1f, 1f, 1f, 0.35f);
        shapes.rect(CARD_X, CARD_Y, cardW, cardH);
        shapes.setColor(0.2f, 1f, 0.4f, 0.55f);
        shapes.rect(CARD_X + PacketLayout.INNER_LEFT * ZOOM,
                CARD_Y + PacketLayout.INNER_BOTTOM * ZOOM,
                cardW - PacketLayout.INNER_LEFT * 2f * ZOOM,
                cardH - PacketLayout.INNER_BOTTOM * 2f * ZOOM);
        shapes.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapes.dispose();
        font.dispose();
        assets.dispose();
    }
}
