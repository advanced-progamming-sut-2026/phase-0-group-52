package view.gui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import minigame.MinigameType;
import view.gui.Theme;
import view.gui.UiKit;

public final class MinigameCard extends Table {

    public static final float WIDTH = 196f;
    public static final float HEIGHT = 168f;
    public static final float ART_HEIGHT = 104f;

    private static final float HOVER_LIFT = 1.05f;
    private static final float LOCK_DIM = 0.42f;
    private static final float LOCK_ICON = 52f;
    private static final float NAME_SCALE = 0.8f;
    private static final float NOTE_SCALE = 0.7f;
    private static final int CORNER = 14;

    private final Table sheen = new Table();

    public MinigameCard(UiKit ui, view.gui.Assets assets, MinigameType type,
            boolean locked, String note, final Runnable onPick) {
        Stack layers = new Stack();
        layers.add(artLayer(ui, type, locked));
        if (locked) {
            layers.add(lockLayer(assets));
        }
        sheen.setBackground(ui.primitives().rounded(CORNER,
                new Color(1f, 1f, 1f, 0f), Theme.SUN, 3));
        sheen.setVisible(false);
        layers.add(sheen);

        add(frame(ui, layers, type, locked, note)).size(WIDTH, HEIGHT);
        setTransform(true);
        setOrigin(Align.center);
        setTouchable(Touchable.enabled);
        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onPick.run();
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer,
                    com.badlogic.gdx.scenes.scene2d.Actor from) {
                sheen.setVisible(true);
                setScale(HOVER_LIFT);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer,
                    com.badlogic.gdx.scenes.scene2d.Actor to) {
                sheen.setVisible(false);
                setScale(1f);
            }
        });
    }

    private Table frame(UiKit ui, Stack layers, MinigameType type,
            boolean locked, String note) {
        Table frame = new Table();
        frame.setBackground(ui.primitives().rounded(CORNER,
                Theme.alpha(Theme.PANEL_SUNKEN, 0.9f), Theme.OUTLINE, 3));
        frame.add(layers).size(WIDTH - 10f, ART_HEIGHT).pad(4f).row();

        Label name = new Label(type.getDisplayName(), ui.skin(), "titleOnDark");
        name.setFontScale(NAME_SCALE);
        name.setAlignment(Align.center);
        name.setColor(locked ? Theme.TEXT_MUTED : Theme.SUN);
        frame.add(name).growX().padTop(2f).row();

        Label sub = new Label(note == null ? "" : note, ui.skin(), "muted");
        sub.setFontScale(NOTE_SCALE);
        sub.setAlignment(Align.center);
        sub.setEllipsis(true);
        frame.add(sub).growX().padBottom(4f);
        return frame;
    }

    private Table artLayer(UiKit ui, MinigameType type, boolean locked) {
        Table layer = new Table();
        Drawable art = ui.imageFile("assets/ui/minigames/" + type.getIconName() + ".png");
        if (art == null) {
            layer.setBackground(ui.primitives().rounded(CORNER - 4,
                    Theme.darken(Theme.PANEL, 0.45f), Theme.OUTLINE_SOFT, 2));
            Label fallback = new Label(type.getDisplayName(), ui.skin(), "muted");
            fallback.setAlignment(Align.center);
            fallback.setWrap(true);
            layer.add(fallback).grow().pad(Theme.PAD_SMALL);
            return layer;
        }
        Image image = new Image(art);
        image.setScaling(Scaling.fit);
        float shade = locked ? LOCK_DIM : 1f;
        image.setColor(shade, shade, shade, 1f);
        Container<Image> box = new Container<Image>(image);
        box.setClip(true);
        box.fill();
        layer.add(box).grow();
        return layer;
    }

    private Table lockLayer(view.gui.Assets assets) {
        Table layer = new Table();
        layer.center();
        com.badlogic.gdx.graphics.g2d.TextureRegion art = assets == null
                ? null : assets.region("IMAGE_UI_PACKETS_LOCK_SMALL");
        if (art != null) {
            Image icon = new Image(art);
            icon.setScaling(Scaling.fit);
            layer.add(icon).size(LOCK_ICON);
        }
        return layer;
    }
}
