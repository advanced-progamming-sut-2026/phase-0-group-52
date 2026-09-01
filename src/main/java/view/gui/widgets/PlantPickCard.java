package view.gui.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import model.entities.plants.PlantData;
import model.entities.plants.PlantRecord;
import model.entities.plants.Plants;
import view.gui.Assets;
import view.gui.Theme;
import view.gui.UiKit;

public final class PlantPickCard extends Table {

    private static final float NAME_HEIGHT = 44f;
    private static final float STAGE_WIDTH = 232f;

    private final UiKit ui;
    private final AlmanacControls controls;
    private final PlantStage stage;

    public PlantPickCard(UiKit ui, Assets assets) {
        this.ui = ui;
        this.controls = new AlmanacControls(ui, assets);
        this.stage = new PlantStage(ui, assets);
        top();
    }

    public void show(Plants plant, int level) {
        clearChildren();
        if (plant == null) {
            add(ui.muted("Pick a plant to see what it does.")).pad(Theme.PAD).center();
            return;
        }
        PlantRecord record = PlantData.record(plant);
        stage.show(record, "idle");

        Table body = new Table();
        body.add(stage).width(STAGE_WIDTH).growY();
        body.add(text(record)).grow().padLeft(Theme.PAD_SMALL).top();

        add(controls.nameHeader(plant.getName(), null,
                AlmanacControls.levelFace(level), NAME_HEIGHT))
                .growX().minHeight(NAME_HEIGHT).padBottom(Theme.PAD_SMALL).row();
        add(body).grow();
    }

    private Table text(PlantRecord record) {
        Table column = new Table();
        column.top().left();
        Label body = new Label(record == null ? "" : record.getDescription(),
                ui.skin(), "muted");
        body.setWrap(true);
        column.add(body).growX().left().row();
        if (record != null && record.getAbility() != null && !record.getAbility().isEmpty()) {
            Label ability = new Label(record.getAbility(), ui.skin(), "small");
            ability.setWrap(true);
            column.add(ability).growX().left().padTop(Theme.PAD_SMALL);
        }
        ScrollPane pane = new ScrollPane(column, ui.skin(), "bare");
        pane.setScrollingDisabled(true, false);
        pane.setFadeScrollBars(false);
        pane.setOverscroll(false, false);
        UiKit.focusOnHover(pane);
        Table holder = new Table();
        holder.add(pane).grow();
        return holder;
    }
}
