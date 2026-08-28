package kopo.kkeudeok.mapper;

import kopo.kkeudeok.dto.ChildDTO;
import kopo.kkeudeok.dto.ExpressionCalibDTO;
import kopo.kkeudeok.dto.RoadmapDTO;
import kopo.kkeudeok.dto.MissionLogDTO;
import kopo.kkeudeok.dto.StoryDTO;
import kopo.kkeudeok.dto.StoryNodeDTO;
import kopo.kkeudeok.dto.StorySessionDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "classpath:db/schema-h2.sql")
class StoryMapperTest {

    @Autowired
    private IChildMapper childMapper;

    @Autowired
    private IStoryMapper storyMapper;

    @Autowired
    private IStorySessionMapper sessionMapper;

    @Autowired
    private IMissionLogMapper missionLogMapper;

    @Autowired
    private IExpressionCalibMapper calibMapper;

    @Autowired
    private IRoadmapMapper roadmapMapper;

    @Test
    @DisplayName("중복이 났을 때 쓰는 조회가 이미 만들어진 노드를 찾아낸다")
    void liveLookupFindsExistingNode() {
        StoryDTO story = StoryDTO.builder()
                .childId(1L).title("중복 확인용").situationType("sad").isGenerated(true)
                .build();
        storyMapper.insertStory(story);
        storyMapper.insertNode(StoryNodeDTO.builder()
                .storyId(story.getStoryId())
                .nodeOrder(3)
                .stageType("CAUSE")
                .narration("왜 그런 마음이 들었을까")
                .build());

        StoryNodeDTO found = storyMapper.selectNodeByOrderLive(story.getStoryId(), 3);

        assertThat(found).isNotNull();
        assertThat(found.getStageType()).isEqualTo("CAUSE");

        assertThat(storyMapper.selectNodeByOrderLive(story.getStoryId(), 5)).isNull();
    }

    @Test
    @DisplayName("아이를 읽는다")
    void selectChild() {

        ChildDTO child = childMapper.selectChild(1L);

        assertThat(child).isNotNull();
        assertThat(child.getName()).isEqualTo("김지우");
        assertThat(child.getCharacterType()).isEqualTo("tori");
        assertThat(child.getCallName()).isEqualTo("지우");
        assertThat(child.getDisorderType()).isEqualTo("자폐");

        assertThat(childMapper.selectChildByMember(1L).getChildId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("이야기 → 노드 → 세션 → 결과가 순서대로 저장되고 다시 읽힌다")
    void fullRound() {

        StoryDTO story = StoryDTO.builder()
                .childId(1L)
                .title("토리가 길을 잃을 뻔했어요")
                .situationType("sad")
                .isGenerated(true)
                .build();

        storyMapper.insertStory(story);
        assertThat(story.getStoryId()).isNotNull();

        assertThat(storyMapper.selectStory(story.getStoryId()).getSituationType()).isEqualTo("sad");

        StoryNodeDTO node = StoryNodeDTO.builder()
                .storyId(story.getStoryId())
                .nodeOrder(2)
                .stageType("MIND")
                .narration("토리는 눈물이 맺혔어요")
                .questionText("토리는 지금 어떤 마음일까?")
                .choiceData("{\"title\":\"어떤 마음일까?\",\"mission\":\"CHOICE\",\"target\":\"sad\"}")
                .build();

        storyMapper.insertNode(node);
        assertThat(node.getNodeId()).isNotNull();
        StoryNodeDTO read = storyMapper.selectNodeByOrder(story.getStoryId(), 2);
        assertThat(read).isNotNull();
        assertThat(read.getStageType()).isEqualTo("MIND");
        assertThat(read.getNarration()).isEqualTo("토리는 눈물이 맺혔어요");
        assertThat(read.getChoiceData()).contains("\"target\":\"sad\"");

        assertThat(storyMapper.selectNodes(story.getStoryId())).hasSize(1);

        StorySessionDTO session = StorySessionDTO.builder()
                .childId(1L)
                .storyId(story.getStoryId())
                .dailyInput("놀이터에서 친구를 못 만났어요")
                .status(StorySessionDTO.INCOMPLETE)
                .build();

        sessionMapper.insertSession(session);
        assertThat(session.getSessionId()).isNotNull();

        StorySessionDTO loaded = sessionMapper.selectSession(session.getSessionId());
        assertThat(loaded.getStoryId()).isEqualTo(story.getStoryId());
        assertThat(loaded.getDailyInput()).isEqualTo("놀이터에서 친구를 못 만났어요");
        assertThat(loaded.getStatus()).isEqualTo("INCOMPLETE");

        assertThat(sessionMapper.countTodaySessions(1L)).isEqualTo(1);
        assertThat(sessionMapper.countTodayCompleted(1L)).isZero();

        missionLogMapper.insertLogs(List.of(MissionLogDTO.builder()
                .sessionId(session.getSessionId())
                .nodeId(node.getNodeId())
                .missionType("CHOICE")
                .targetValue("sad")
                .responseValue("angry")
                .isSuccess(false)
                .build()));

        List<MissionLogDTO> logs = missionLogMapper.selectBySession(session.getSessionId());
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getResponseValue()).isEqualTo("angry");
        assertThat(logs.get(0).getIsSuccess()).isFalse();
        assertThat(logs.get(0).getTargetValue()).isEqualTo("sad");

        assertThat(sessionMapper.updateSessionClosed(session.getSessionId(), "COMPLETED")).isEqualTo(1);
        assertThat(sessionMapper.selectSession(session.getSessionId()).getStatus()).isEqualTo("COMPLETED");
        assertThat(sessionMapper.countTodayCompleted(1L)).isEqualTo(1);

        assertThat(sessionMapper.updateSessionClosed(session.getSessionId(), "INCOMPLETE")).isZero();
        assertThat(sessionMapper.selectSession(session.getSessionId()).getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    @DisplayName("같은 세션 결과를 두 번 보내도 행이 늘지 않는다 (지우고 다시 넣기)")
    void resendDoesNotDuplicate() {

        StoryDTO story = StoryDTO.builder()
                .childId(1L).title("제목").situationType("happy").isGenerated(false).build();
        storyMapper.insertStory(story);
        StoryNodeDTO node = StoryNodeDTO.builder()
                .storyId(story.getStoryId()).nodeOrder(4).stageType("EXPRESSION")
                .narration("표정을 지어 보자").build();
        storyMapper.insertNode(node);

        StorySessionDTO session = StorySessionDTO.builder()
                .childId(1L).storyId(story.getStoryId())
                .status(StorySessionDTO.INCOMPLETE).build();
        sessionMapper.insertSession(session);

        MissionLogDTO log = MissionLogDTO.builder()
                .sessionId(session.getSessionId())
                .nodeId(node.getNodeId())
                .missionType("EXPRESSION")
                .targetValue("happy")
                .responseValue("sad")
                .isSuccess(false)
                .build();

        missionLogMapper.insertLogs(List.of(log));

        missionLogMapper.deleteByNodes(session.getSessionId(), List.of(node.getNodeId()));
        missionLogMapper.insertLogs(List.of(log));

        assertThat(missionLogMapper.selectBySession(session.getSessionId())).hasSize(1);
    }

    @Test
    @DisplayName("이어서 마쳐도 다른 노드의 기록은 지워지지 않는다")
    void deleteByNodesTouchesOnlyGivenNodes() {

        StoryDTO story = StoryDTO.builder()
                .childId(1L).title("제목").situationType("사과하기").isGenerated(true).build();
        storyMapper.insertStory(story);

        StoryNodeDTO mind = StoryNodeDTO.builder()
                .storyId(story.getStoryId()).nodeOrder(2).stageType("MIND").narration("마음").build();
        StoryNodeDTO cause = StoryNodeDTO.builder()
                .storyId(story.getStoryId()).nodeOrder(3).stageType("CAUSE").narration("이유").build();
        storyMapper.insertNode(mind);
        storyMapper.insertNode(cause);

        StorySessionDTO session = StorySessionDTO.builder()
                .childId(1L).storyId(story.getStoryId())
                .status(StorySessionDTO.INCOMPLETE).build();
        sessionMapper.insertSession(session);

        missionLogMapper.insertLogs(List.of(MissionLogDTO.builder()
                .sessionId(session.getSessionId()).nodeId(mind.getNodeId())
                .missionType("CHOICE").targetValue("sad").responseValue("sad")
                .isSuccess(true).build()));

        missionLogMapper.deleteByNodes(session.getSessionId(), List.of(cause.getNodeId()));
        missionLogMapper.insertLogs(List.of(MissionLogDTO.builder()
                .sessionId(session.getSessionId()).nodeId(cause.getNodeId())
                .missionType("CHOICE").targetValue("cause").responseValue("cause")
                .isSuccess(true).build()));
        assertThat(missionLogMapper.selectBySession(session.getSessionId())).hasSize(2);
    }

    @Test
    @DisplayName("표정 등록 — 다시 찍으면 갈아끼워지고 감정당 한 행만 남는다")
    void expressionCalib() {

        ExpressionCalibDTO happy = ExpressionCalibDTO.builder()
                .childId(1L).emotionType("HAPPY")
                .landmarkData("{\"v\":1,\"shapes\":{\"mouthSmileLeft\":0.85}}")
                .build();

        calibMapper.insertCalib(happy);
        assertThat(happy.getCalibId()).isNotNull();

        calibMapper.insertCalib(ExpressionCalibDTO.builder()
                .childId(1L).emotionType("HAPPY")
                .landmarkData("{\"v\":1,\"shapes\":{\"mouthSmileLeft\":0.91}}")
                .build());

        calibMapper.insertCalib(ExpressionCalibDTO.builder()
                .childId(1L).emotionType("SAD")
                .landmarkData("{\"v\":1,\"shapes\":{\"mouthFrownLeft\":0.7}}")
                .build());

        List<ExpressionCalibDTO> rows = calibMapper.selectByChild(1L);

        assertThat(rows).hasSize(2);
        assertThat(rows).extracting(ExpressionCalibDTO::getEmotionType)
                .containsExactlyInAnyOrder("HAPPY", "SAD");

        assertThat(rows).allSatisfy(r -> assertThat(r.getMediaUrl()).isNull());

        assertThat(calibMapper.deleteByChild(1L)).isEqualTo(2);
        assertThat(calibMapper.selectByChild(1L)).isEmpty();
    }

    @Test
    @DisplayName("로드맵 — 새로 올리면 쓰던 것이 내려간다")
    void roadmapActivation() {

        assertThat(roadmapMapper.selectActive(1L)).isNull();
        RoadmapDTO first = RoadmapDTO.builder()
                .childId(1L).roadmapType("STANDARD")
                .stepData("{\"totalWeeks\":12,\"weeks\":[]}").isActive(true).build();
        roadmapMapper.insertRoadmap(first);

        assertThat(roadmapMapper.selectActive(1L).getRoadmapId()).isEqualTo(first.getRoadmapId());

        roadmapMapper.deactivateAll(1L);

        RoadmapDTO second = RoadmapDTO.builder()
                .childId(1L).roadmapType("AI")
                .stepData("{\"totalWeeks\":12,\"weeks\":[]}").isActive(true).build();
        roadmapMapper.insertRoadmap(second);

        RoadmapDTO active = roadmapMapper.selectActive(1L);
        assertThat(active.getRoadmapId()).isEqualTo(second.getRoadmapId());
        assertThat(active.getRoadmapType()).isEqualTo("AI");
    }

    @Test
    @DisplayName("오늘 남긴 미완료 세션을 이어할 것으로 찾는다")
    void selectResumable() {

        assertThat(sessionMapper.selectResumable(1L)).isNull();

        StoryDTO story = StoryDTO.builder()
                .childId(1L).title("제목").situationType("친구 위로하기").isGenerated(true).build();
        storyMapper.insertStory(story);

        StorySessionDTO session = StorySessionDTO.builder()
                .childId(1L).storyId(story.getStoryId())
                .status(StorySessionDTO.INCOMPLETE).build();
        sessionMapper.insertSession(session);

        assertThat(sessionMapper.selectResumable(1L)).isNull();

        storyMapper.insertNode(StoryNodeDTO.builder()
                .storyId(story.getStoryId()).nodeOrder(1).stageType("STORY").narration("본문").build());

        assertThat(sessionMapper.selectResumable(1L)).isNotNull();

        sessionMapper.updateSessionClosed(session.getSessionId(), "COMPLETED");
        assertThat(sessionMapper.selectResumable(1L)).isNull();
    }
}
