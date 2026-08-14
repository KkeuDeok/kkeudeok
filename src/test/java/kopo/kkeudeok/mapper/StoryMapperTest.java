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
// Boot 4 에서 패키지가 옮겨졌다 — 3.x 의 org.springframework.boot.test.autoconfigure.jdbc 가 아니다
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
    private ChildMapper childMapper;

    @Autowired
    private StoryMapper storyMapper;

    @Autowired
    private StorySessionMapper sessionMapper;

    @Autowired
    private MissionLogMapper missionLogMapper;

    @Autowired
    private ExpressionCalibMapper calibMapper;

    @Autowired
    private RoadmapMapper roadmapMapper;

    @Test
    @DisplayName("아이를 읽는다")
    void selectChild() {

        ChildDTO child = childMapper.selectChild(1L);

        assertThat(child).isNotNull();
        assertThat(child.getName()).isEqualTo("김지우");
        assertThat(child.getCharacterType()).isEqualTo("tori");
        assertThat(child.getCallName()).isEqualTo("지우");
        assertThat(child.getDisorderType()).isEqualTo("자폐");

        assertThat(childMapper.selectFirstChild().getChildId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("이야기 → 노드 → 세션 → 결과가 순서대로 저장되고 다시 읽힌다")
    void fullRound() {

        // --- 이야기 ---
        StoryDTO story = StoryDTO.builder()
                .childId(1L)
                .title("토리가 길을 잃을 뻔했어요")
                .situationType("sad")
                .isGenerated(true)
                .build();

        storyMapper.insertStory(story);
        assertThat(story.getStoryId()).isNotNull();      // 생성 키가 되돌아왔는가

        assertThat(storyMapper.selectStory(story.getStoryId()).getSituationType()).isEqualTo("sad");

        // --- 노드 (choice_data 에 부가정보를 담는다) ---
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

        // --- 세션 ---
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

        // --- 미션 결과 ---
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

        // --- 세션 닫기 ---
        assertThat(sessionMapper.updateSessionClosed(session.getSessionId(), "COMPLETED")).isEqualTo(1);
        assertThat(sessionMapper.selectSession(session.getSessionId()).getStatus()).isEqualTo("COMPLETED");
        assertThat(sessionMapper.countTodayCompleted(1L)).isEqualTo(1);

        // 이미 마친 세션은 중도이탈로 되돌아가지 않는다 — 늦게 도착한 전송이 완료를 깨면 안 된다
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

        // 그만둘 때 저장된 앞부분
        missionLogMapper.insertLogs(List.of(MissionLogDTO.builder()
                .sessionId(session.getSessionId()).nodeId(mind.getNodeId())
                .missionType("CHOICE").targetValue("sad").responseValue("sad")
                .isSuccess(true).build()));

        // 이어서 마친 뒤 뒷부분만 전송
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

        calibMapper.deleteByEmotion(1L, "HAPPY");
        calibMapper.insertCalib(happy);
        assertThat(happy.getCalibId()).isNotNull();

        // 다시 찍기 — 지우고 넣으므로 행이 늘지 않는다
        calibMapper.deleteByEmotion(1L, "HAPPY");
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

        // ⚠ 얼굴 사진은 저장하지 않는다 — 온보딩이 "원본 사진은 바로 지워져요" 라고 약속했다
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

        assertThat(sessionMapper.selectResumable(1L)).isNull();   // 아직 없다

        StoryDTO story = StoryDTO.builder()
                .childId(1L).title("제목").situationType("친구 위로하기").isGenerated(true).build();
        storyMapper.insertStory(story);

        StorySessionDTO session = StorySessionDTO.builder()
                .childId(1L).storyId(story.getStoryId())
                .status(StorySessionDTO.INCOMPLETE).build();
        sessionMapper.insertSession(session);

        // 노드가 하나도 없으면 이어할 게 없다
        assertThat(sessionMapper.selectResumable(1L)).isNull();

        storyMapper.insertNode(StoryNodeDTO.builder()
                .storyId(story.getStoryId()).nodeOrder(1).stageType("STORY").narration("본문").build());

        assertThat(sessionMapper.selectResumable(1L)).isNotNull();

        // 마친 세션은 이어할 대상이 아니다
        sessionMapper.updateSessionClosed(session.getSessionId(), "COMPLETED");
        assertThat(sessionMapper.selectResumable(1L)).isNull();
    }
}
