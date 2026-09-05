package view.gui.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import model.entities.plants.PlantAnimations;
import model.entities.plants.PlantData;
import model.entities.plants.PlantProgress;
import model.entities.plants.PlantRecord;
import model.entities.plants.Plants;
import controller.menu.CollectionMenuController;
import view.gui.Animations;
import view.gui.Assets;
import view.gui.BaseScreen;
import view.gui.GameContext;
import view.gui.Theme;
import view.gui.TopBar;
import view.gui.UiKit;
import view.gui.widgets.AlmanacFilterPopup;
import view.gui.widgets.PamActor;
import view.gui.widgets.SeedPacket;

import java.util.ArrayList;
import java.util.List;

public final class AlmanacScreen extends BaseScreen {

    private static final float STAGE_WIDTH = 362f;
    private static final float NAME_HEIGHT = 48f;
    private static final float STAGE_HEIGHT = 162f;
    private static final float ARROW = 40f;
    private static final float TILE_HEIGHT = 54f;
    private static final float TILE_GAP = 3f;
    private static final com.badlogic.gdx.graphics.Color STATE_GREY =
            new com.badlogic.gdx.graphics.Color(0.82f, 0.85f, 0.8f, 1f);
    private static final float PACKET_SCALE = 0.68f;
    private static final int PACKET_ROWS = 3;
    private static final float INNER_PAD = 9f;
    private static final int PANEL_RADIUS = 30;
    private static final int PANEL_BORDER = 3;
    private static final float TAB_WIDTH = 54f;
    private static final float TAB_HEIGHT = 54f;
    private static final float TAB_ACTIVE_HEIGHT = 72f;
    private static final float TAB_ICON_TOP = 5f;
    private static final float TAB_INDENT = 14f;
    private static final float TAB_GAP = 3f;
    private static final float TAB_SEAM = 14f;
    private static final float OUTER_PAD = 12f;
    private static final float STRIP_HEIGHT = 160f;
    private static final Color PANEL_SHADE = new Color(0.62f, 0.6f, 0.58f, 1f);
    private static final Color DIMMED = new Color(0.55f, 0.55f, 0.58f, 1f);

    private final AlmanacFilterPopup.Rules rules = new AlmanacFilterPopup.Rules();
    private final CollectionMenuController controller;
    private view.gui.widgets.PlantLevelRow levelRow;
    private final view.gui.widgets.AlmanacControls controls;
    private final view.gui.widgets.StatTiles tiles;

    private int tileColumn;
    private boolean zombieTab;
    private String query = "";
    private com.badlogic.gdx.scenes.scene2d.ui.TextField searchField;
    private ZombieAlmanac zombies;
    private Plants selected = Plants.SUNFLOWER;
    private int clipIndex;
    private view.gui.widgets.PlantStage plantStage;
    private Label stateLabel;
    private ScrollPane strip;
    private float scrollX;

    public AlmanacScreen(GameContext context) {
        super(context, "Almanac");
        this.controller = new CollectionMenuController(context.app());
        this.controls = new view.gui.widgets.AlmanacControls(context.ui(), context.assets());
        this.tiles = new view.gui.widgets.StatTiles(context.ui(), context.assets());
    }

    @Override
    protected TopBar.Section section() {
        return TopBar.Section.ALMANAC;
    }

    @Override
    protected boolean scrollContent() {
        return false;
    }

    @Override
    protected String backdropImage() {
        return "assets/backgrounds/main_menu.png";
    }

    private Assets art() {
        return context.assets();
    }

    private PlantRecord record() {
        return PlantData.record(selected);
    }

    private PlantProgress progress() {
        if (context.user() == null) {
            return new PlantProgress(selected);
        }
        return context.user().getPlants().progress(selected);
    }

    private boolean unlocked() {
        return progress().isUnlocked();
    }

    @Override
    protected void build() {
        content.clearChildren();
        content.top();

        Table panel = ui.panel();
        panel.pad(OUTER_PAD);
        panel.setColor(PANEL_SHADE);
        panel.top();

        Table body = new Table();
        body.top();
        if (zombieTab) {
            body.add(inner(zombies().statsPanel())).grow();
            body.add(inner(zombies().stagePanel())).width(STAGE_WIDTH).growY();
        } else {
            body.add(inner(leftColumn())).width(STAGE_WIDTH).growY();
            body.add(inner(statsPane())).grow();
        }
        panel.add(body).grow().row();

        panel.add(zombieTab ? zombies().cardStrip() : packetStrip())
                .growX().height(STRIP_HEIGHT)
                .padTop(Theme.PAD).padLeft(Theme.PAD_SMALL).padRight(Theme.PAD_SMALL)
                .padBottom(Theme.PAD);

        Table below = new Table();
        below.top();
        below.add(panel).grow().padTop(TAB_HEIGHT - TAB_SEAM);

        Table above = new Table();
        above.top().left();
        above.add(tabRow()).left().padLeft(TAB_INDENT).growX();

        Stack stack = new Stack();
        stack.add(below);
        stack.add(above);
        content.add(stack).grow();
    }

    private Table inner(Table body) {
        Table frame = new Table();
        frame.setBackground(ui.primitives().rounded(PANEL_RADIUS, Theme.PANEL,
                Theme.OUTLINE_SOFT, PANEL_BORDER));
        frame.pad(INNER_PAD);
        frame.add(body).grow();
        return frame;
    }

    private ZombieAlmanac zombies() {
        if (zombies == null) {
            zombies = new ZombieAlmanac(context, new Runnable() {
                @Override
                public void run() {
                    rebuild();
                }
            });
        }
        return zombies;
    }

    private void showTab(boolean toZombies) {
        zombieTab = toZombies;
        rebuild();
    }

    private void rebuild() {
        if (strip != null) {
            scrollX = strip.getScrollX();
        }
        if (zombies != null) {
            zombies.rememberScroll();
        }
        content.clear();
        build();
    }

    private Table tabRow() {
        Table row = new Table();
        row.top().left();
        row.add(artTab("PLANTS", !zombieTab, zombieTab ? new Runnable() {
            @Override
            public void run() {
                showTab(false);
            }
        } : null)).size(TAB_WIDTH, zombieTab ? TAB_HEIGHT : TAB_ACTIVE_HEIGHT).top();
        row.add(artTab("ZOMBIES", zombieTab, zombieTab ? null : new Runnable() {
            @Override
            public void run() {
                showTab(true);
            }
        })).size(TAB_WIDTH, zombieTab ? TAB_ACTIVE_HEIGHT : TAB_HEIGHT).top().padLeft(TAB_GAP);
        row.add(new Table()).growX();
        row.add(searchBar()).height(TAB_HEIGHT).top().padRight(TAB_INDENT);
        return row;
    }

    private Table artTab(String kind, boolean active, final Runnable onClick) {
        return view.gui.widgets.AlmanacTab.build(ui,
                regionOf("IMAGE_UI_ALMANAC_TABS_" + kind + (active ? "_ACTIVE" : "_DOWN")),
                regionOf("IMAGE_UI_STORE_TABICONS_" + kind), active, TAB_ICON_TOP, onClick);
    }

    private Table searchBar() {
        return view.gui.widgets.SearchBar.build(ui, query, regionOf("IMAGE_UI_ALMANAC_FILTER_BUTTON_UP"),
                new view.gui.widgets.SearchBar.Sink() {
                    @Override
                    public void typed(String text,
                            com.badlogic.gdx.scenes.scene2d.ui.TextField field) {
                        query = text;
                        searchField = field;
                        applyQuery();
                        rebuild();
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        openFilter();
                    }
                });
    }

    private void applyQuery() {
        rules.setQuery(query);
        if (zombies != null) {
            zombies.rules().setQuery(query);
        }
        if (searchField != null) {
            stage.setKeyboardFocus(searchField);
            searchField.setCursorPosition(searchField.getText().length());
        }
    }

    private void openFilter() {
        if (zombieTab) {
            new view.gui.widgets.ZombieFilterPopup(context, zombies().rules(), new Runnable() {
                @Override
                public void run() {
                    rebuild();
                }
            }).showOn(stage);
            return;
        }
        new AlmanacFilterPopup(context, rules, new Runnable() {
            @Override
            public void run() {
                rebuild();
            }
        }).showOn(stage);
    }

    private Table leftColumn() {
        Table column = new Table();
        column.top();
        column.add(nameHeader()).growX().minHeight(NAME_HEIGHT).row();
        column.add(stagePanel()).growX().height(STAGE_HEIGHT).row();
        column.add(levelingPanel()).growX().padTop(Theme.PAD_SMALL);
        return column;
    }

    private Table nameHeader() {
        Label.LabelStyle base = ui.skin().get("smallOnDark", Label.LabelStyle.class);
        stateLabel = new Label(currentClip(), new Label.LabelStyle(base.font, STATE_GREY));
        return controls.nameHeader(selected.getName(), stateLabel,
                view.gui.widgets.AlmanacControls.levelFace(progress().getLevel()), NAME_HEIGHT);
    }

    private List<String> clips() {
        List<String> names = new ArrayList<String>();
        PlantRecord r = record();
        if (r != null) {
            names.addAll(r.getAnimations().getClips().keySet());
        }
        if (names.isEmpty()) {
            names.add("idle");
        }
        return names;
    }

    private String currentClip() {
        List<String> all = clips();
        return all.get(Math.floorMod(clipIndex, all.size()));
    }

    private Table stagePanel() {
        plantStage = new view.gui.widgets.PlantStage(ui, art());
        plantStage.show(record(), currentClip());
        plantStage.setDimmed(!unlocked());

        Stack shell = new Stack();
        Table frameHolder = new Table();
        frameHolder.add(plantStage).grow().padLeft(ARROW / 2f).padRight(ARROW / 2f);
        shell.add(frameHolder);

        Table arrows = new Table();
        arrows.add(arrow(false)).size(ARROW).left().expandX();
        arrows.add(arrow(true)).size(ARROW).right().expandX();
        shell.add(arrows);

        Table wrap = new Table();
        wrap.add(shell).grow();
        return wrap;
    }

    private Table arrow(final boolean forward) {
        Table cell = new Table();
        Drawable art = regionOf(forward
                ? "IMAGE_UI_ALMANAC_STATS_SCREEN_NAV_ARROW_NEXT"
                : "IMAGE_UI_ALMANAC_STATS_SCREEN_NAV_ARROW_PREVIOUS");
        if (art != null) {
            final Image mark = new Image(art);
            mark.setScaling(Scaling.fit);
            cell.add(mark).grow();
            controls.hoverTint(cell, mark);
        } else {
            cell.add(new Label(forward ? ">" : "<", ui.skin(), "rowHeader"));
        }
        UiKit.onClick(cell, new Runnable() {
            @Override
            public void run() {
                clipIndex += forward ? 1 : -1;
                showClip();
            }
        });
        return cell;
    }

    private void showClip() {
        String clip = currentClip();
        if (stateLabel != null) {
            stateLabel.setText(clip);
        }
        if (plantStage != null) {
            plantStage.play(clip);
        }
    }

    private Drawable regionOf(String id) {
        if (art() == null) {
            return null;
        }
        TextureRegion found = art().region(id);
        return found == null ? null : new TextureRegionDrawable(found);
    }

    private Table levelingPanel() {
        if (levelRow == null) {
            levelRow = new view.gui.widgets.PlantLevelRow(context, new Runnable() {
                @Override
                public void run() {
                    rebuild();
                }
            });
        }
        levelRow.setBurstHost(plantStage == null ? null : plantStage.overlay());
        levelRow.show(selected);
        return levelRow;
    }

    private Table statsPane() {
        Table box = new Table() {
            @Override
            public float getPrefWidth() {
                return 0f;
            }
        };
        box.top().left();
        PlantRecord r = record();
        if (r == null) {
            return box;
        }

        tileColumn = 0;
        addCombatStats(box);
        java.util.Set<String> seen = new java.util.LinkedHashSet<String>();
        for (String taken : new String[]{"Sun Cost", "Recharge", "Toughness", "Damage",
            "Attack Speed", "Attack Rate", "Damage per Second", "Attack Damage",
            "Sun Production", "Firing Rate", "Growth Speed", "Damage Details"}) {
            seen.add(key(taken));
        }
        List<PlantRecord.Stat> rows = new ArrayList<PlantRecord.Stat>(r.getStats());
        rows.addAll(r.getDetails());
        for (PlantRecord.Stat stat : rows) {
            if (seen.add(key(stat.getLabel()))) {
                addTile(box, stat.getLabel(), stat.getValue(), false);
            }
        }

        closeTiles(box, r);

        if (!r.getDescription().isEmpty()) {
            Label desc = new Label(r.getDescription(), ui.skin(), "rowSub");
            desc.setWrap(true);
            box.add(desc).colspan(2).growX().minWidth(0f).left()
                    .padTop(Theme.PAD).padLeft(TILE_GAP).row();
        }
        if (!r.getFlavorText().isEmpty()) {
            Label flavor = new Label(r.getFlavorText(), ui.skin(), "story");
            flavor.setWrap(true);
            box.add(flavor).colspan(2).growX().minWidth(0f).left()
                    .padTop(Theme.PAD).padLeft(TILE_GAP).row();
        }

        return statsScroller(box);
    }

    private Table statsScroller(Table box) {
        ScrollPane scroll = new ScrollPane(box, ui.skin());
        scroll.setStyle(ui.skin().get("bare", ScrollPane.ScrollPaneStyle.class));
        scroll.setScrollingDisabled(true, false);
        scroll.setOverscroll(false, true);
        UiKit.focusOnHover(scroll);

        Table holder = new Table();
        holder.add(scroll).grow();
        if (!unlocked()) {
            holder.setColor(DIMMED);
        }
        return holder;
    }

    private static String key(String label) {
        return label == null ? "" : label.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    private void addCombatStats(Table box) {
        int level = progress().getLevel();
        int cost = PlantData.effectiveCost(selected, level);
        int recharge = PlantData.effectiveRecharge(selected, level);
        int hp = PlantData.effectiveHp(selected, level);
        int damage = PlantData.effectiveDamage(selected, level);
        double interval = PlantData.effectiveInterval(selected, level);

        addTile(box, "Sun cost", String.valueOf(cost), cost != selected.getCost());
        addTile(box, "Recharge", recharge + "s", recharge != (int) selected.getRecharge());
        addTile(box, "Toughness", String.valueOf(hp), hp != selected.getBaseHP());
        if (selected.getDamage() > 0) {
            addTile(box, "Damage", String.valueOf(damage), damage != selected.getDamage());
        }
        boolean attacks = selected.getDamage() > 0 && selected.getActionInterval() > 0;
        if (attacks) {
            boolean moved = Math.abs(interval - selected.getActionInterval()) > 0.01;
            addTile(box, "Attack rate", String.format("%.1fs", interval), moved);
            addTile(box, "Damage per second", String.format("%.1f", damage / interval),
                    moved || damage != selected.getDamage());
        } else if (selected.getActionInterval() > 0) {
            addTile(box, "Production time", String.format("%.0fs", interval),
                    Math.abs(interval - selected.getActionInterval()) > 0.01);
        }
    }

    private void closeTiles(Table box, PlantRecord r) {
        if (tileColumn % 2 == 1) {
            box.add(new Table()).growX().uniformX().pad(TILE_GAP).height(TILE_HEIGHT);
        }
        box.row();
        box.add(tiles.categoryTile(r)).colspan(2).growX()
                .pad(TILE_GAP).height(TILE_HEIGHT).row();
    }

    private void addTile(Table grid, String label, String value, boolean upgraded) {
        grid.add(tiles.tile(label, value, upgraded)).growX().uniformX()
                .pad(TILE_GAP).height(TILE_HEIGHT);
        tileColumn++;
        if (tileColumn % 2 == 0) {
            grid.row();
        }
    }

    private Table packetStrip() {
        List<Plants> plants = rules.apply();
        final float restore = scrollX;
        Table grid = new Table();
        grid.bottom().left();
        int rows = PACKET_ROWS;
        int columns = (plants.size() + rows - 1) / rows;
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                int at = column * rows + row;
                if (at < plants.size()) {
                    SeedPacket card = packet(plants.get(at));
                    grid.add(card).size(card.width(), card.height());
                } else {
                    grid.add().size(1f, 1f);
                }
            }
            grid.row();
        }

        ScrollPane scroll = new ScrollPane(grid, ui.skin());
        scroll.setStyle(ui.skin().get("bare", ScrollPane.ScrollPaneStyle.class));
        scroll.setScrollingDisabled(false, true);
        scroll.setOverscroll(true, false);
        UiKit.focusOnHover(scroll);
        scroll.validate();
        scroll.setScrollX(restore);
        scroll.updateVisualScroll();
        strip = scroll;

        Table holder = new Table();
        holder.bottom();
        holder.add(scroll).grow();
        return holder;
    }

    private SeedPacket packet(final Plants plant) {
        final boolean isLocked = context.user() != null
                && !context.user().getPlants().isUnlocked(plant);
        SeedPacket card = new SeedPacket(ui, art(), plant, SeedPacket.Mode.ALMANAC, PACKET_SCALE);
        card.setLocked(isLocked);
        card.setLevel(context.user() == null ? 1 : context.user().getPlantLevel(plant));
        card.setSelected(plant == selected);
        card.setBoosted(context.user() != null && context.user().getStoredBoosts().contains(plant));
        card.onClick(new Runnable() {
            @Override
            public void run() {
                if (isLocked && context.settings().isDebugMode()) {
                    context.user().getPlants().grant(plant, 1);
                    context.toasts().success(plant.getName() + " unlocked.");
                }
                selected = plant;
                clipIndex = 0;
                rebuild();
            }
        });
        return card;
    }

    @Override
    public void show() {
        super.show();
        if (strip != null) {
            uiStage().setScrollFocus(strip);
        }
    }
}
