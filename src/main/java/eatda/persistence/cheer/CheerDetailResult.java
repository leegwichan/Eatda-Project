package eatda.persistence.cheer;

import eatda.domain.cheer.Cheer;
import eatda.domain.cheer.CheerImage;
import eatda.domain.cheer.CheerTags;
import java.util.List;

public record CheerDetailResult(
        Cheer cheer,
        long storeId,
        CheerTags tags,
        List<CheerImage> images
) {

}
