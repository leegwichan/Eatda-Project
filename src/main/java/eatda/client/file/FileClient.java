package eatda.client.file;

import java.time.Duration;
import java.util.Collection;
import java.util.List;

public interface FileClient {

    String getImageUrl(String imagePath);

    String generateUploadPresignedUrl(String fileKey, Duration signatureDuration);

    FileMovingResult moveFiles(String domainName, long domainId, List<String> beforePaths);

    void deleteFiles(Collection<String> paths);
}
