package view.gui.widgets;

import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Scaling;
import view.gui.Theme;
import view.gui.UiKit;

public final class SortRow {

    public static final float HEIGHT = 38f;
    private static final float ARROW = 15f;
    private static final float GRIP = 14f;

    private SortRow() {}

    public static Table face(UiKit ui, String name, boolean ascending, boolean lifted) {
        Table row = new Table();
        row.setBackground(ui.primitives().rounded(Theme.RADIUS,
                lifted ? Theme.lighten(Theme.PANEL_SUNKEN, 0.35f) : Theme.PANEL_SUNKEN,
                lifted ? Theme.SUN : Theme.OUTLINE, lifted ? 3 : 2));
        row.pad(Theme.PAD_SMALL);
        row.setTouchable(Touchable.enabled);

        Table grip = new Table();
        for (int i = 0; i < 3; i++) {
            Table line = new Table();
            line.setBackground(ui.primitives().flat(Theme.OUTLINE_SOFT));
            grip.add(line).width(GRIP).height(2f).padBottom(2f).row();
        }
        row.add(grip).padRight(Theme.PAD_SMALL);

        Label label = new Label(name, ui.skin(), "rowSub");
        row.add(label).left().growX().padTop(UiKit.opticalPad(label));

        Image arrow = new Image(ui.drawable(ascending ? "sortAscending" : "sortDescending"));
        arrow.setScaling(Scaling.fit);
        row.add(arrow).size(ARROW).padLeft(Theme.PAD_SMALL);
        return row;
    }
}
