package view.gui.widgets;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import model.entities.plants.Plants;
import view.gui.Assets;
import view.gui.Popup;
import view.gui.Theme;
import view.gui.UiKit;

import java.util.List;

public final class PlantPickPopup extends Popup {

    public interface Choice {
        void picked(Plants plant);
    }

    private static final int COLUMNS = 5;
    private static final float PACKET = 0.62f;

    public PlantPickPopup(UiKit ui, Assets assets, String title, List<Plants> owned,
            final Choice onPick) {
        super(ui, title, 860f, 560f);
        Table grid = new Table();
        grid.defaults().pad(Theme.PAD_SMALL);
        int column = 0;
        for (final Plants plant : owned) {
            SeedPacket packet = new SeedPacket(ui, assets, plant,
                    SeedPacket.Mode.ALMANAC, PACKET);
            packet.setTouchable(Touchable.enabled);
            packet.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    onPick.picked(plant);
                    close();
                }
            });
            grid.add(packet);
            if (++column % COLUMNS == 0) {
                grid.row();
            }
        }
        body().add(grid).grow();
    }
}
