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
        private Path _root;
        private Path _path;

        public FileIndexItem(Path root, Path path) {
            _root = root;
            _path = path;
        }

        public String displayString() {
            var path = _root.relativize(_path);
            return path.toString();
        }

        public void onClick() {
            Window.getInstance().setBufferPath(_path);
        }
    }

    public List<FileIndexItem> createFileIndex() {
        var list = new ArrayList<FileIndexItem>();
        try {
            var root = ProjectPaths.getSourceRootPath();
            if (root == null) {
                return list;
            }
            Files.find(root,
                       Integer.MAX_VALUE,
                       (filePath, fileAttr) -> fileAttr.isRegularFile())
            .forEach((path) -> {
                list.add(new FileIndexItem(root, path));
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
