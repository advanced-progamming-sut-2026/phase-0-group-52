package view.gui.layout;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.files.FileHandle;

import java.io.File;

final class SandboxFiles implements Files {

    private final File root;

    SandboxFiles(File root) {
        this.root = root;
    }

    private FileHandle under(String path) {
        return new FileHandle(new File(root, path));
    }

    @Override
    public FileHandle getFileHandle(String path, FileType type) {
        return under(path);
    }

    @Override
    public FileHandle classpath(String path) {
        return under(path);
    }

    @Override
    public FileHandle internal(String path) {
        return under(path);
    }

    @Override
    public FileHandle external(String path) {
        return under(path);
    }

    @Override
    public FileHandle absolute(String path) {
        return under(path);
    }

    @Override
    public FileHandle local(String path) {
        return under(path);
    }

    @Override
    public String getExternalStoragePath() {
        return root.getAbsolutePath();
    }

    @Override
    public boolean isExternalStorageAvailable() {
        return true;
    }

    @Override
    public String getLocalStoragePath() {
        return root.getAbsolutePath();
    }

    @Override
    public boolean isLocalStorageAvailable() {
        return true;
    }
}
