package view.gui.layout;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.SnapshotArray;
import view.gui.Assets;

import java.util.ArrayList;
import java.util.List;

public final class Extras {

    public static final String LAYER = "ui-extras";

    private Extras() {}

    public static void sync(Stage stage, Assets assets) {
        if (stage == null) {
            return;
        }
        List<String> wanted = UiLayout.extrasFor(UiLayout.scopeName());
        Group layer = layer(stage, !wanted.isEmpty());
        if (layer == null) {
            return;
        }
        prune(layer, wanted);
        for (int i = 0; i < wanted.size(); i++) {
            place(layer, wanted.get(i), assets);
        }
        layer.toFront();
    }

    private static Group layer(Stage stage, boolean create) {
        SnapshotArray<Actor> children = stage.getRoot().getChildren();
        for (int i = 0; i < children.size; i++) {
            if (LAYER.equals(children.get(i).getName())) {
                return (Group) children.get(i);
            }
        }
        if (!create) {
            return null;
        }
        Group made = new Group();
        made.setName(LAYER);
        made.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.childrenOnly);
        made.setSize(stage.getWidth(), stage.getHeight());
        stage.getRoot().addActor(made);
        return made;
    }

    private static void prune(Group layer, List<String> wanted) {
        List<Actor> stale = new ArrayList<Actor>();
        SnapshotArray<Actor> children = layer.getChildren();
        for (int i = 0; i < children.size; i++) {
            if (!wanted.contains(children.get(i).getName())) {
                stale.add(children.get(i));
            }
        }
        for (int i = 0; i < stale.size(); i++) {
            stale.get(i).remove();
        }
    }

    private static void place(Group layer, String id, Assets assets) {
        UiLayout.Tweak tweak = UiLayout.tweak(id);
        Actor found = layer.findActor(id);
        if (found == null) {
            found = build(id, tweak.getImage(), assets);
            if (found == null) {
                return;
            }
            layer.addActor(found);
        }
        found.setBounds(tweak.getDx(), tweak.getDy(),
                Math.max(1f, tweak.getDw()), Math.max(1f, tweak.getDh()));
        found.setVisible(!tweak.isHidden());
    }

    private static Actor build(String id, String file, Assets assets) {
        TextureRegion art = assets == null ? null : assets.regionFile(file);
        if (art == null) {
            return null;
        }
        Image image = new Image(new TextureRegionDrawable(art));
        image.setScaling(Scaling.stretch);
        image.setName(id);
        return image;
    }
}
