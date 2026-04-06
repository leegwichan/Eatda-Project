package eatda.persistence;

import eatda.DatabaseCleaner;
import eatda.client.file.FileClient;
import eatda.client.map.MapClient;
import eatda.client.oauth.OauthClient;
import eatda.fixture.CheerGenerator;
import eatda.fixture.CheerImageGenerator;
import eatda.fixture.CheerTagGenerator;
import eatda.fixture.MemberGenerator;
import eatda.fixture.StoreGenerator;
import eatda.fixture.StoryGenerator;
import eatda.fixture.StoryImageGenerator;
import eatda.repository.cheer.CheerRepository;
import eatda.repository.cheer.CheerTagRepository;
import eatda.repository.member.MemberRepository;
import eatda.repository.store.StoreRepository;
import eatda.repository.story.StoryRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ExtendWith(DatabaseCleaner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public abstract class BasePersistenceTest {

    @MockitoBean
    protected OauthClient oauthClient;

    @MockitoBean
    protected MapClient mapClient;

    @MockitoBean
    protected FileClient fileClient;

    @Autowired
    protected MemberGenerator memberGenerator;

    @Autowired
    protected StoreGenerator storeGenerator;

    @Autowired
    protected CheerGenerator cheerGenerator;

    @Autowired
    protected CheerTagGenerator cheerTagGenerator;

    @Autowired
    protected StoryGenerator storyGenerator;

    @Autowired
    protected CheerImageGenerator cheerImageGenerator;

    @Autowired
    protected StoryImageGenerator storyImageGenerator;

    @Autowired
    protected MemberRepository memberRepository;

    @Autowired
    protected StoreRepository storeRepository;

    @Autowired
    protected CheerRepository cheerRepository;

    @Autowired
    protected CheerTagRepository cheerTagRepository;

    @Autowired
    protected StoryRepository storyRepository;
}
