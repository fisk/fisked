package org.fisk.fisked.fileindex;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.fisk.fisked.ui.Window;
import org.fisk.fisked.ui.ListView.ListItem;

public class FileIndex {
    private static class FileIndexItem extends ListItem {
        private Path _path;

        public FileIndexItem(Path path) {
            _path = path;
        }

        public String displayString() {
            return _path.toString();
        }

        public void onClick() {
            Window.getInstance().setBufferPath(_path);
        }
    }

    public List<FileIndexItem> createFileIndex() {
        var list = new ArrayList<FileIndexItem>();
        try {
            var root = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
            boolean found = false;
            while (!root.getRoot().equals(root)) {
                if (root.resolve(".git").toFile().exists() ||
                    root.resolve(".fisked").toFile().exists()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return list;
            }
            Files.find(root,
                       Integer.MAX_VALUE,
                       (filePath, fileAttr) -> fileAttr.isRegularFile())
            .forEach((path) -> {
                list.add(new FileIndexItem(path));
            });
        } catch (IOException e) {
        }
        list.sort((FileIndexItem i1, FileIndexItem i2) -> {
            return i1.displayString().compareTo(i2.displayString());
        });
        return list;
    }

    public static List<FileIndexItem> createFileList() {
        var index = new FileIndex();
        return index.createFileIndex();
    }
}
