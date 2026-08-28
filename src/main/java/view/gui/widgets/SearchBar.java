package view.gui.widgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Scaling;
import view.gui.Theme;
import view.gui.UiKit;

public final class SearchBar {

    private static final float FIELD_WIDTH = 240f;
    private static final float FIELD_HEIGHT = 46f;

    public interface Sink {
        void typed(String text, TextField field);
    }

    private SearchBar() {}

    public static Table build(UiKit ui, String query, Drawable filterFace,
            final Sink sink, Runnable onFilter) {
        Table bar = new Table();
        bar.setBackground(ui.primitives().rounded(Theme.RADIUS,
                Theme.PANEL, Theme.OUTLINE_SOFT, 2));
        bar.pad(3f);

        final TextField field = new TextField(query, ui.skin());
        field.setMessageText("Search");
        field.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                sink.typed(((TextField) actor).getText(), (TextField) actor);
            }
        });
        bar.add(field).width(FIELD_WIDTH).height(FIELD_HEIGHT);

        Table button = new Table();
        if (filterFace != null) {
            Image mark = new Image(filterFace);
            mark.setScaling(Scaling.fit);
            button.add(mark).grow();
        } else {
            button.add(new Label("F", ui.skin(), "rowHeader"));
        }
        UiKit.onClick(button, onFilter);
        bar.add(button).size(FIELD_HEIGHT).padLeft(4f);
        return bar;
    }
}
