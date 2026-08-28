package view.gui.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Scaling;
import view.gui.Theme;
import view.gui.UiKit;

public final class AlmanacTab {

    private static final float ICON = 38f;
    private static final int RADIUS = 14;

    private AlmanacTab() {}

    public static Table build(UiKit ui, Drawable face, Drawable icon, boolean active,
            float iconTop, Runnable onClick) {
        Table cell = new Table();
        if (face != null) {
            cell.setBackground(face);
        } else {
            cell.setBackground(ui.primitives().rounded(RADIUS,
                    active ? Theme.GREEN : Theme.PANEL_SUNKEN, Theme.OUTLINE, 2));
        }
        if (icon != null) {
            Image mark = new Image(icon);
            mark.setScaling(Scaling.fit);
            cell.add(mark).size(ICON).top().padTop(iconTop).expandY();
        }
        if (onClick != null) {
            UiKit.onClick(cell, onClick);
        }
        return cell;
    }
}
