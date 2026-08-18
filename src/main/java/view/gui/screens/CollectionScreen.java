package view.gui.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import controller.menu.CollectionMenuController;
import model.entities.plants.PlantData;
import model.entities.plants.Plants;
import model.entities.plants.PlantsCategory;
import model.entities.zombies.Zombies;
import view.gui.Animations;
import view.gui.BaseScreen;
import view.gui.GameContext;
import view.gui.Theme;
import view.gui.UiKit;
import view.gui.widgets.SeedPacket;

/**
 * The almanac: two tabs listing plants and zombies with a detail panel.
 *
 * <p>Plants can be filtered by family, ownership and upgradeability, as the
 * specification asks. Zombies that have never been seen show as blank cards so the
 * player can tell something is missing without learning what it is.
 *
 * <p>Buying and upgrading are state changes and go through
 * {@link CollectionMenuController}.
 */
public final class CollectionScreen extends BaseScreen {

    private static final String ALL_FAMILIES = "All families";

    private final CollectionMenuController controller;

    private boolean showingPlants = true;
    private String familyFilter = ALL_FAMILIES;
    private boolean onlyOwned;
    private boolean onlyUpgradeable;

    private Plants selectedPlant;
    private Zombies selectedZombie;

    private Table listArea;
    private Table detailArea;
    private TextButton plantsTab;
    private TextButton zombiesTab;

    public CollectionScreen(GameContext context) {
        super(context, "Collection");
        this.controller = new CollectionMenuController(context.app());
    }

    @Override
    protected void build() {
        Table panel = ui.panel();
        panel.top();

        panel.add(buildHeader()).growX().padBottom(Theme.PAD).row();

        Table body = new Table();
        listArea = new Table();
        detailArea = new Table();

        ScrollPane scroll = new ScrollPane(listArea, ui.skin());
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);

        body.add(scroll).grow().padRight(Theme.PAD);
        body.add(detailArea).width(300f).growY().top();
        panel.add(body).grow();

        rebuildList();
        rebuildDetail();

        content.add(panel).grow();
    }

    private Table buildHeader() {
        Table header = new Table();

        plantsTab = ui.styledButton("Plants", "tab", new Runnable() {
            @Override
            public void run() {
                showingPlants = true;
                selectedZombie = null;
                markTabs();
                rebuildList();
                rebuildDetail();
            }
        });
        zombiesTab = ui.styledButton("Zombies", "tab", new Runnable() {
            @Override
            public void run() {
                showingPlants = false;
                selectedPlant = null;
                markTabs();
                rebuildList();
                rebuildDetail();
            }
        });

        header.add(plantsTab).width(120f).padRight(Theme.PAD_SMALL);
        header.add(zombiesTab).width(120f).padRight(Theme.PAD_LARGE);
        header.add(buildFilters()).left().expandX();
        header.add(ui.secondaryButton("Back", new Runnable() {
            @Override
            public void run() {
                controller.handleCommand("menu enter main");
            }
        })).right();

        markTabs();
        return header;
    }

    /** Family / ownership / upgradeable filters, shown only on the plants tab. */
    private Table buildFilters() {
        final Table filters = new Table();

        final SelectBox<String> family = new SelectBox<String>(ui.skin());
        String[] options = new String[PlantsCategory.values().length + 1];
        options[0] = ALL_FAMILIES;
        for (int i = 0; i < PlantsCategory.values().length; i++) {
            options[i + 1] = pretty(PlantsCategory.values()[i].name());
        }
        family.setItems(options);
        family.setSelected(familyFilter);
        family.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                familyFilter = family.getSelected();
                rebuildList();
            }
        });

        final com.badlogic.gdx.scenes.scene2d.ui.CheckBox owned =
                new com.badlogic.gdx.scenes.scene2d.ui.CheckBox(" Owned", ui.skin());
        owned.setChecked(onlyOwned);
        owned.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                onlyOwned = owned.isChecked();
                rebuildList();
            }
        });

        final com.badlogic.gdx.scenes.scene2d.ui.CheckBox upgradeable =
                new com.badlogic.gdx.scenes.scene2d.ui.CheckBox(" Upgradeable", ui.skin());
        upgradeable.setChecked(onlyUpgradeable);
        upgradeable.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                onlyUpgradeable = upgradeable.isChecked();
                rebuildList();
            }
        });

        filters.add(family).width(190f).padRight(Theme.PAD);
        filters.add(owned).padRight(Theme.PAD);
        filters.add(upgradeable);
        return filters;
    }

    private void markTabs() {
        plantsTab.setColor(showingPlants ? Theme.PANEL : Theme.PANEL_SUNKEN);
        zombiesTab.setColor(showingPlants ? Theme.PANEL_SUNKEN : Theme.PANEL);
    }

    // ----------------------------------------------------------------- list

    private void rebuildList() {
        listArea.clear();
        listArea.top().left();
        listArea.defaults().pad(Theme.PAD_SMALL);

        int perRow = 7;
        int column = 0;

        if (showingPlants) {
            for (final Plants plant : Plants.values()) {
                if (!passesFilter(plant)) {
                    continue;
                }
                // Fixed cell size, otherwise a wrapped name makes one card taller
                // and the whole row grows with it.
                listArea.add(plantCard(plant)).size(Theme.PACKET_WIDTH, Theme.PACKET_HEIGHT);
                if (++column % perRow == 0) {
                    listArea.row();
                }
            }
            if (column == 0) {
                listArea.add(new Label("No plants match those filters.", ui.skin(), "muted")).left();
            }
        } else {
            for (final Zombies zombie : Zombies.values()) {
                listArea.add(zombieCard(zombie)).size(Theme.PACKET_WIDTH, Theme.PACKET_HEIGHT);
                if (++column % perRow == 0) {
                    listArea.row();
                }
            }
        }
    }

    private boolean passesFilter(Plants plant) {
        if (!familyFilter.equals(ALL_FAMILIES)
                && !pretty(plant.getCategory().name()).equals(familyFilter)) {
            return false;
        }
        if (onlyOwned && isLocked(plant)) {
            return false;
        }
        if (onlyUpgradeable && levelOf(plant) >= PlantData.MAX_LEVEL) {
            return false;
        }
        return true;
    }

    /**
     * Whether the plant is unavailable to the player.
     *
     * <p>Only the locked-plants level type records this, via {@code App}; there is
     * no per-account ownership in the model, so outside that level everything reads
     * as available. Inventing ownership here would put game state in the view.
     */
    private boolean isLocked(Plants plant) {
        return context.app().getLockedPlants().contains(plant);
    }

    private int levelOf(Plants plant) {
        return (context.user() == null) ? 1 : context.user().getPlantLevel(plant);
    }

    private SeedPacket plantCard(final Plants plant) {
        SeedPacket packet = new SeedPacket(ui, plant);
        packet.setLocked(isLocked(plant));
        packet.setLevel(levelOf(plant));
        packet.onClick(new Runnable() {
            @Override
            public void run() {
                selectedPlant = plant;
                rebuildDetail();
            }
        });
        return packet;
    }

    /**
     * A zombie card. Unseen zombies render as an empty frame, so the player knows
     * an entry exists without being told what it is.
     */
    private Table zombieCard(final Zombies zombie) {
        final boolean seen = hasBeenSeen(zombie);

        Table card = new Table();
        card.setBackground(ui.primitives().rounded(8,
                seen ? Theme.PANEL : Theme.PANEL_SUNKEN, Theme.OUTLINE, 2));
        card.pad(Theme.PAD_SMALL);
        card.setSize(Theme.PACKET_WIDTH, Theme.PACKET_HEIGHT);

        if (seen) {
            card.add(ui.token(42, Theme.plantFamily("MELEE"))).size(42f).padTop(6f).row();
            Label name = new Label(zombie.getName(), ui.skin(), "small");
            name.setAlignment(Align.center);
            name.setWrap(true);
            card.add(name).width(Theme.PACKET_WIDTH - 12f).expandY().top();
        } else {
            Label unknown = new Label("?", ui.skin(), "huge");
            unknown.setColor(Theme.TEXT_DISABLED);
            unknown.setAlignment(Align.center);
            card.add(unknown).expand().center();
        }

        Animations.attachPress(card);
        UiKit.onClick(card, new Runnable() {
            @Override
            public void run() {
                selectedZombie = seen ? zombie : null;
                if (!seen) {
                    context.toasts().info("You have not met this zombie yet.");
                }
                rebuildDetail();
            }
        });
        return card;
    }

    /**
     * Whether the player has encountered this zombie.
     *
     * <p>Phase one never recorded sightings, so there is nothing in the model to
     * read. Everything is treated as seen for now rather than inventing state here,
     * which would put game knowledge in the view. A {@code seenZombies} set on
     * {@code User} is the natural home for this when the lawn screen lands.
     */
    private boolean hasBeenSeen(Zombies zombie) {
        return true;
    }

    // --------------------------------------------------------------- detail

    private void rebuildDetail() {
        detailArea.clear();
        detailArea.top();

        Table box = ui.sunken();
        box.top();

        if (showingPlants && selectedPlant != null) {
            buildPlantDetail(box, selectedPlant);
        } else if (!showingPlants && selectedZombie != null) {
            buildZombieDetail(box, selectedZombie);
        } else {
            Label hint = new Label(showingPlants
                    ? "Select a plant to see its details."
                    : "Select a zombie to see its details.", ui.skin(), "muted");
            hint.setWrap(true);
            hint.setAlignment(Align.center);
            box.add(hint).width(250f).pad(Theme.PAD_LARGE);
        }

        detailArea.add(box).growX();
    }

    private void buildPlantDetail(Table box, final Plants plant) {
        box.add(ui.token(64, Theme.plantFamily(plant.getCategory().name())))
                .size(64f).padBottom(Theme.PAD_SMALL).row();
        Label name = new Label(plant.getName(), ui.skin(), "title");
        name.setAlignment(Align.center);
        box.add(name).padBottom(Theme.PAD_SMALL).row();

        stat(box, "Family", pretty(plant.getCategory().name()));
        stat(box, "Sun cost", String.valueOf(plant.getCost()));
        stat(box, "Health", String.valueOf(plant.getBaseHP()));
        stat(box, "Damage", String.valueOf(plant.getDamage()));
        stat(box, "Recharge", plant.getRecharge() + "s");
        stat(box, "Level", levelOf(plant) + " / " + PlantData.MAX_LEVEL);
        stat(box, "Upgrade cost", PlantData.COINS_PER_LEVEL + " coins");
        // Seed packets are a single account-wide pool in the model, not per plant.
        stat(box, "Seed packets held",
                String.valueOf(context.user() == null ? 0 : context.user().getSeedPacket()));

        addTags(box, plant);

        Table actions = new Table();
        actions.add(ui.button("Buy", new Runnable() {
            @Override
            public void run() {
                controller.handleCommand("buy plant -t " + plant.name());
                rebuildList();
                rebuildDetail();
            }
        })).padRight(Theme.PAD_SMALL);
        actions.add(ui.styledButton("Upgrade", "info", new Runnable() {
            @Override
            public void run() {
                controller.handleCommand("upgrade plant -t " + plant.name());
                rebuildList();
                rebuildDetail();
            }
        }));
        box.add(actions).padTop(Theme.PAD).row();
    }

    /** Lists the plant's tags under its stats, when it has any. */
    private void addTags(Table box, Plants plant) {
        StringBuilder tags = new StringBuilder();
        for (int i = 0; i < plant.getTags().size(); i++) {
            if (i > 0) {
                tags.append(", ");
            }
            tags.append(pretty(plant.getTags().get(i).name()));
        }
        if (tags.length() == 0) {
            return;
        }
        Label tagLabel = new Label(tags.toString(), ui.skin(), "muted");
        tagLabel.setWrap(true);
        tagLabel.setAlignment(Align.center);
        box.add(tagLabel).width(250f).padTop(Theme.PAD_SMALL).row();
    }

    private void buildZombieDetail(Table box, Zombies zombie) {
        box.add(ui.token(64, Theme.plantFamily("MELEE"))).size(64f).padBottom(Theme.PAD_SMALL).row();
        Label name = new Label(zombie.getName(), ui.skin(), "title");
        name.setAlignment(Align.center);
        box.add(name).padBottom(Theme.PAD_SMALL).row();

        stat(box, "Health", String.valueOf((int) zombie.getHp()));
        stat(box, "Speed", String.format("%.2f cells/s", zombie.getSpeed()));
        stat(box, "Eat damage", String.format("%.0f/s", zombie.getEatDPS()));
        stat(box, "Armour", zombie.getArmor() == null ? "None" : pretty(zombie.getArmor().name()));
        stat(box, "Wave cost", String.valueOf(zombie.getWaveCost()));
    }

    private void stat(Table box, String label, String value) {
        Table row = new Table();
        row.add(new Label(label, ui.skin(), "muted")).left().expandX();
        row.add(new Label(value, ui.skin(), "default")).right();
        box.add(row).growX().padTop(2f).row();
    }

    /** Turns SCREAMING_CASE enum names into readable text. */
    private String pretty(String enumName) {
        if (enumName == null) {
            return "";
        }
        String[] words = enumName.toLowerCase().split("_");
        StringBuilder out = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (out.length() > 0) {
                out.append(' ');
            }
            out.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return out.toString();
    }
}
