package view.gui.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import controller.menu.ChoosePlantMenuController;
import model.entities.plants.PlantData;
import model.entities.plants.Plants;
import view.gui.Animations;
import view.gui.BaseScreen;
import view.gui.GameContext;
import view.gui.Theme;
import view.gui.widgets.SeedPacket;

import java.util.List;

public final class ChoosePlantScreen extends BaseScreen {
    private final ChoosePlantMenuController controller;

    private Table availableArea;
    private Table slotArea;
    private Label slotCounter;

    public ChoosePlantScreen(GameContext context) {
        super(context, "Choose your plants");
        this.controller = new ChoosePlantMenuController(context.app());
    }

    @Override
    protected void build() {
        Table panel = ui.panel();
        panel.top();

        Table header = new Table();
        slotCounter = new Label("", ui.skin(), "title");
        header.add(slotCounter).left().expandX();
        header.add(ui.secondaryButton("Clear", new Runnable() {
            @Override
            public void run() {
                controller.handleCommand("clear selection");
                refreshAll();
            }
        })).padRight(Theme.PAD_SMALL);
        header.add(ui.secondaryButton("Back", new Runnable() {
            @Override
            public void run() {
                controller.handleCommand("menu enter chapter_menu");
            }
        }));
        panel.add(header).growX().padBottom(Theme.PAD_SMALL).row();

        slotArea = new Table();
        panel.add(slotArea).growX().padBottom(Theme.PAD).row();

        panel.add(ui.divider()).height(2f).growX().padBottom(Theme.PAD).row();

        availableArea = new Table();
        ScrollPane scroll = new ScrollPane(availableArea, ui.skin());
        scroll.setFadeScrollBars(false);
        view.gui.UiKit.focusOnHover(scroll);
        scroll.setScrollingDisabled(true, false);
        panel.add(scroll).grow().row();

        panel.add(ui.button("Let's Rock!", new Runnable() {
            @Override
            public void run() {
                startLevel();
            }
        })).padTop(Theme.PAD).height(48f).width(200f).center();

        refreshAll();
        content.add(panel).grow();
    }

    private void refreshAll() {
        rebuildSlots();
        rebuildAvailable();
    }

    private void rebuildSlots() {
        slotArea.clear();
        slotArea.left();
        slotArea.defaults().pad(Theme.PAD_SMALL);

        List<Plants> selection = context.app().getPlantSelection();
        int capacity = deckCapacity();

        slotCounter.setText("Deck  " + selection.size() + " / " + capacity);

        for (final Plants plant : selection) {
            SeedPacket packet = new SeedPacket(ui, plant);
            packet.setLevel(levelOf(plant));
            packet.setSelected(true);
            packet.setBoosted(context.app().getBoostedSelection().contains(plant));
            packet.onClick(new Runnable() {
                @Override
                public void run() {
                    controller.handleCommand("remove -t " + plant.name());
                    refreshAll();
                }
            });
            slotArea.add(packet).size(Theme.PACKET_WIDTH, Theme.PACKET_HEIGHT);
        }

        for (int i = selection.size(); i < capacity; i++) {
            slotArea.add(emptySlot()).size(Theme.PACKET_WIDTH, Theme.PACKET_HEIGHT);
        }
    }

    private Table emptySlot() {
        Table slot = new Table();
        slot.setBackground(ui.primitives().rounded(8,
                Theme.alpha(Theme.PANEL_SUNKEN, 0.5f), Theme.OUTLINE_SOFT, 2));
        Label plus = new Label("+", ui.skin(), "title");
        plus.setColor(Theme.TEXT_DISABLED);
        plus.setAlignment(Align.center);
        slot.add(plus).expand().center();
        return slot;
    }

    private void rebuildAvailable() {
        availableArea.clear();
        availableArea.top().left();
        availableArea.defaults().pad(Theme.PAD_SMALL);

        List<Plants> selection = context.app().getPlantSelection();
        int column = 0;

        for (final Plants plant : Plants.values()) {
            if (context.app().getLockedPlants().contains(plant)) {
                continue;
            }
            final boolean chosen = selection.contains(plant);

            Table cell = new Table();
            SeedPacket packet = new SeedPacket(ui, plant);
            packet.setLevel(levelOf(plant));
            packet.setSelected(chosen);
            packet.setBoosted(context.app().getBoostedSelection().contains(plant));
            packet.onClick(new Runnable() {
                @Override
                public void run() {
                    toggle(plant, chosen);
                }
            });
            cell.add(packet).size(Theme.PACKET_WIDTH, Theme.PACKET_HEIGHT).row();

            Table tools = new Table();
            tools.add(compact("Boost", new Runnable() {
                @Override
                public void run() {
                    controller.handleCommand("boost -t " + plant.name());
                    refreshAll();
                }
            })).width(58f).padRight(2f);
            tools.add(compact("Lv+", new Runnable() {
                @Override
                public void run() {
                    controller.handleCommand("upgrade plant -t " + plant.name());
                    refreshAll();
                }
            })).width(34f);
            cell.add(tools).padTop(3f);

            availableArea.add(cell);
            if (++column % 8 == 0) {
                availableArea.row();
            }
        }
    }

    private com.badlogic.gdx.scenes.scene2d.ui.TextButton compact(String text, Runnable action) {
        com.badlogic.gdx.scenes.scene2d.ui.TextButton button =
                new com.badlogic.gdx.scenes.scene2d.ui.TextButton(text, ui.skin(), "secondary");
        button.padLeft(Theme.PAD_SMALL).padRight(Theme.PAD_SMALL);
        button.getLabelCell().padBottom(view.gui.UiKit.opticalPad(button.getLabel()));
        button.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                action.run();
            }
        });
        view.gui.Animations.attachPress(button);
        return button;
    }

    private void toggle(Plants plant, boolean currentlyChosen) {
        controller.handleCommand((currentlyChosen ? "remove -t " : "choose -t ") + plant.name());
        refreshAll();
    }

    private int levelOf(Plants plant) {
        return (context.user() == null) ? 1 : context.user().getPlantLevel(plant);
    }

    private int deckCapacity() {
        return context.app().getSelectedLevel() <= 1
                ? ChoosePlantMenuController.FIRST_LEVEL_SLOTS
                : ChoosePlantMenuController.OTHER_LEVEL_SLOTS;
    }

    private void startLevel() {
        if (context.app().getPlantSelection().isEmpty()) {
            context.toasts().error("Pick at least one plant before starting.");
            Animations.shake(slotArea);
            return;
        }
        controller.handleCommand("start");
        context.toasts().info("The lawn screen arrives in the next milestone.");
    }

    @Override
    public void show() {
        super.show();
        refreshAll();
    }
}
