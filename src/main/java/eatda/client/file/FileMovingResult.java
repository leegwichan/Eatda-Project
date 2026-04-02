package eatda.client.file;

import java.util.List;
import java.util.Map;

public class FileMovingResult {

    private final Map<String, String> oldToNewPath;

    public FileMovingResult(Map<String, String> oldToNewPath) {
        this.oldToNewPath = Map.copyOf(oldToNewPath);
    }

    public String findNewPath(String oldPath) {
        return oldToNewPath.getOrDefault(oldPath, oldPath);
    }

    public List<String> getResults() {
        return oldToNewPath.values()
                .stream()
                .toList();
    }
}
