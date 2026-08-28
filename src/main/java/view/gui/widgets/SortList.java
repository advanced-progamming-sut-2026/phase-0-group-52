package view.gui.widgets;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import view.gui.UiKit;

import java.util.List;

public final class SortList extends Table {

    private static final float PITCH = SortRow.HEIGHT + 3f;

    public interface Model {
        int size();

        String label(int index);

        boolean ascending(int index);

        void flip(int index);

        void move(int from, int to);
    }

    private final UiKit ui;
    private final Model model;
    private final Runnable onChange;

    private int dragging = -1;

    public SortList(UiKit ui, Model model, Runnable onChange) {
        this.ui = ui;
        this.model = model;
        this.onChange = onChange;
        top();
        fill();
        addListener(new DragListener() {
            @Override
            public void dragStart(InputEvent event, float x, float y, int pointer) {
                dragging = indexAt(y);
            }

            @Override
            public void drag(InputEvent event, float x, float y, int pointer) {
                int target = indexAt(y);
                if (dragging >= 0 && target >= 0 && target != dragging) {
                    model.move(dragging, target);
                    dragging = target;
                    fill();
                    onChange.run();
                }
            }

            @Override
            public void dragStop(InputEvent event, float x, float y, int pointer) {
                dragging = -1;
                fill();
            }
        });
    }

    private int indexAt(float y) {
        int index = (int) ((getHeight() - y) / PITCH);
        return index < 0 || index >= model.size() ? -1 : index;
    }

    public void fill() {
        clearChildren();
        for (int i = 0; i < model.size(); i++) {
            add(row(i)).growX().height(SortRow.HEIGHT).padBottom(3f).row();
        }
    }

    private Table row(final int index) {
        Table row = SortRow.face(ui, model.label(index), model.ascending(index),
                index == dragging);
        UiKit.onClick(row, new Runnable() {
            @Override
            public void run() {
                if (dragging < 0) {
                    model.flip(index);
                    fill();
                    onChange.run();
                }
            }
        });
        return row;
    }

    public static List<String> names(List<String> raw) {
        return raw;
    }
}
