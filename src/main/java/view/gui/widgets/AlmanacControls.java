package view.gui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import view.gui.Assets;
import view.gui.Theme;
import view.gui.UiKit;

public final class AlmanacControls {

    private static final float DIM = 0.45f;

    private final UiKit ui;
    private final Assets assets;

    public AlmanacControls(UiKit ui, Assets assets) {
        this.ui = ui;
        this.assets = assets;
    }

    private Drawable region(String id) {
        if (id == null || assets == null) {
            return null;
        }
        TextureRegion found = assets.region(id);
        return found == null ? null : new TextureRegionDrawable(found);
    }

    public static String pretty(String enumName) {
        String[] words = enumName.toLowerCase().split("_");
        StringBuilder out = new StringBuilder();
        for (String word : words) {
            if (out.length() > 0) {
                out.append(' ');
            }
            out.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return out.toString();
    }

    public void hoverTint(Table cell, final Actor target) {
        cell.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor from) {
                if (pointer == -1) {
                    target.setColor(Theme.SUN);
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor to) {
                if (pointer == -1) {
                    target.setColor(Color.WHITE);
                }
            }
        });
    }

    public Table iconButton(String id, boolean lit, Runnable action) {
        return iconButton(id, lit, false, action);
    }

    public Table iconButton(String id, boolean lit, boolean fill, Runnable action) {
        Table cell = new Table();
        Drawable art = region(id);
        if (art != null) {
            Image mark = new Image(art);
            mark.setScaling(fill ? Scaling.fill : Scaling.fit);
            cell.add(mark).grow();
        }
        cell.getColor().a = lit ? 1f : DIM;
        if (lit && cell.hasChildren()) {
            hoverTint(cell, cell.getChildren().first());
        }
        UiKit.onClick(cell, action);
        return cell;
    }

    public Table priceCell(String text, String iconId, float iconSize) {
        Table cell = new Table();
        cell.right();
        Label label = new Label(text, ui.skin(), "muted");
        cell.add(label).right().padTop(UiKit.opticalPad(label));
        Drawable art = iconId.startsWith("IMAGE_") ? region(iconId) : ui.drawable(iconId);
        if (art != null) {
            Image mark = new Image(art);
            mark.setScaling(Scaling.fit);
            cell.add(mark).size(iconSize).padLeft(2f);
        }
        return cell;
    }
}
