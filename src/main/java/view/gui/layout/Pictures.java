package view.gui.layout;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Pictures {

    private static final String[] FOLDERS = {"assets/ui", "assets/backgrounds"};
    private static final int MAX_FILES = 200;

    private static final List<String> FOUND = new ArrayList<String>();
    private static boolean scanned;

    private Pictures() {}

    public static List<String> available() {
        if (!scanned) {
            scanned = true;
            scan();
        }
        return Collections.unmodifiableList(FOUND);
    }

    public static void rescan() {
        scanned = false;
        FOUND.clear();
    }

    private static void scan() {
        if (Gdx.files == null) {
            return;
        }
        for (int i = 0; i < FOLDERS.length; i++) {
            FileHandle folder = Gdx.files.local(FOLDERS[i]);
            if (!folder.exists() || !folder.isDirectory()) {
                continue;
            }
            for (FileHandle file : folder.list()) {
                if (FOUND.size() < MAX_FILES && "png".equalsIgnoreCase(file.extension())) {
                    FOUND.add(FOLDERS[i] + "/" + file.name());
                }
            }
        }
        Collections.sort(FOUND);
    }
}
