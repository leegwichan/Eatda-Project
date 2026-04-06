package eatda.repository.story;

import eatda.domain.story.Story;
import eatda.domain.story.StoryImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoryImageRepository extends JpaRepository<StoryImage, Long> {

    List<StoryImage> findByStoryId(long storyId);

    List<StoryImage> findAllByStoryIn(List<Story> stories);
}
