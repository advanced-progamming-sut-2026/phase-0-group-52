package view.gui.layout;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import view.gui.Animations;
import view.gui.Theme;
import view.gui.UiKit;

final class LayoutHud extends Table {

    private static final float WIDTH = 420f;
    private static final float TEXT = 0.66f;
    private static final float TITLE = 0.78f;
    private static final int CORNERS = 4;
    private static final Color FACE = new Color(0.06f, 0.09f, 0.12f, 0.88f);

    private final LayoutEditor editor;
    private final UiKit ui;
    private final Label target;
    private final Label values;

    private int corner;

    LayoutHud(LayoutEditor editor, UiKit ui) {
        this.editor = editor;
        this.ui = ui;
        this.target = small("", "onDark", TEXT);
        this.values = small("", "onDark", TEXT);
        setBackground(ui.primitives().rounded(10, FACE, new Color(0.15f, 0.95f, 1f, 0.9f), 2));
        pad(Theme.PAD_SMALL, Theme.PAD, Theme.PAD_SMALL, Theme.PAD);
        build();
        refresh();
    }

    private Label small(String text, String style, float scale) {
        Label made = new Label(text, ui.skin(), style);
        made.setFontScale(scale);
        made.setWrap(true);
        return made;
    }

    private void build() {
        Label heading = small("UI EDIT MODE", "onDark", TITLE);
        heading.setColor(Theme.SUN);
        add(heading).width(WIDTH).left().row();
        add(target).width(WIDTH).left().padTop(2f).row();
        add(values).width(WIDTH).left().padTop(2f).row();
        add(buttons()).width(WIDTH).left().padTop(Theme.PAD_SMALL).row();
        add(hint("drag moves   the corner box resizes   right click deselects"))
                .width(WIDTH).left().padTop(4f).row();
        add(hint("arrows nudge   ctrl+arrows resize   shift steps by ten   ctrl+z undoes"))
                .width(WIDTH).left().row();
        add(hint("tab moves this panel   hold ALT to use the game as usual"))
                .width(WIDTH).left().row();
    }

    private Label hint(String text) {
        Label made = new Label(text, ui.skin(), "muted");
        made.setFontScale(TEXT);
        return made;
    }

    private Table buttons() {
        Table row = new Table();
        row.left();
        row.add(button("Parent", Theme.BLUE, new Runnable() {
            @Override
            public void run() {
                editor.selectParent();
            }
        })).padRight(4f);
        row.add(button("Child", Theme.BLUE, new Runnable() {
            @Override
            public void run() {
                editor.selectChild();
            }
        })).padRight(4f);
        row.add(button("Under", Theme.GREEN, new Runnable() {
            @Override
            public void run() {
                editor.stepUnder(1);
            }
        })).padRight(4f);
        row.add(button("Over", Theme.GREEN, new Runnable() {
            @Override
            public void run() {
                editor.stepUnder(-1);
            }
        })).padRight(4f);
        addActions(row);
        return row;
    }

    private void addActions(Table row) {
        row.add(button("Reset", Theme.RED, new Runnable() {
            @Override
            public void run() {
                editor.resetSelected();
            }
        })).padRight(4f);
        row.add(button("Reset screen", Theme.RED, new Runnable() {
            @Override
            public void run() {
                editor.resetScreen();
            }
        })).padRight(4f);
        row.add(button("Save", Theme.GREEN_DARK, new Runnable() {
            @Override
            public void run() {
                editor.saveNow();
            }
        })).padRight(4f);
        row.add(button("Done", Theme.PANEL_FRAME, new Runnable() {
            @Override
            public void run() {
                editor.leaveMode();
            }
        }));
    }

    private Table button(String text, Color face, Runnable action) {
        Table cell = new Table();
        cell.setBackground(ui.primitives().rounded(6, face, Theme.darken(face, 0.35f), 2));
        Label caption = new Label(text, ui.skin(), "onDark");
        caption.setFontScale(TEXT);
        cell.add(caption).pad(3f, 7f, 3f, 7f);
        Animations.attachPress(cell);
        UiKit.onClick(cell, action);
        return cell;
    }

    void refresh() {
        target.setText(editor.selectionId());
        target.setColor(editor.hasSelection() ? Color.WHITE : Theme.TEXT_DISABLED);
        values.setText(editor.selectionValues());
        invalidateHierarchy();
    }

    void cycleCorner() {
        corner = (corner + 1) % CORNERS;
    }

    void place(float width, float height) {
        pack();
        pack();
        float x = (corner == 0 || corner == 3) ? Theme.PAD : width - getWidth() - Theme.PAD;
        float y = corner < 2 ? Theme.PAD : height - getHeight() - Theme.PAD;
        setPosition(x, y);
    }
}
