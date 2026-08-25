package kopo.kkeudeok.service.impl;

import kopo.kkeudeok.config.GeminiClient;
import kopo.kkeudeok.dto.ChildDTO;
import kopo.kkeudeok.dto.RoadmapPlanDTO;
import kopo.kkeudeok.dto.SituationType;
import kopo.kkeudeok.dto.StoryNodeDTO;
import kopo.kkeudeok.dto.StoryOptionDTO;
import kopo.kkeudeok.dto.StoryRequestDTO;
import kopo.kkeudeok.dto.StoryScenarioDTO;
import kopo.kkeudeok.dto.StoryStage;
import kopo.kkeudeok.service.IStoryAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
// ⚠ Jackson 3 (Spring Boot 4). 2.x 의 com.fasterxml.jackson.databind 이 아니다 —
//   그쪽을 쓰면 ObjectMapper 빈을 못 찾아 기동 자체가 실패한다.
//   애노테이션(@JsonIgnore 등)은 여전히 com.fasterxml.jackson.annotation 이다.
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

// AI 12주 이야기
@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiStoryAiService implements IStoryAiService {

    private final GeminiClient gemini;
    private final kopo.kkeudeok.config.GeminiProperties props;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM = """
            너는 자폐·발달장애 아동의 사회성과 감정 이해를 돕는 학습 콘텐츠 작가다.
            아이가 화면에서 읽고 들을 문장을 쓴다.

            반드시 지킬 것:
            - 한국어. 한 문장은 25자 이내, 쉬운 낱말만.
            - 비유·관용구·반어법 금지. 있는 그대로 쓴다. ("발이 넓다" X)
            - 부정문보다 긍정문. ("뛰지 마" 대신 "천천히 걷자")
            - 무섭거나 다치는 장면, 폭력, 차별, 죽음, 질병은 쓰지 않는다.
            - 아이를 탓하지 않는다. 틀려도 "괜찮아" 로 시작한다.
            - 이모지·특수문자·마크다운을 쓰지 않는다.
            - 답은 JSON 만. 설명이나 인사말을 붙이지 않는다.
            """;

    // ------------------------------------------------------------
    //  1) 시나리오 뼈대
    // ------------------------------------------------------------

    @Override
    public StoryScenarioDTO createScenario(ChildDTO child, String emotion, String dailyNote,
                                           Brief brief,
                                           RoadmapPlanDTO.Week week) {

        String friend = StoryTemplate.charName(child.getCharacterType(), child.getCharacterNickname());

        String prompt = """
                아래 아이를 위한 사회성 학습 이야기의 뼈대를 만들어라.

                [아이]
                - 이름: %s
                  ⚠ 아이를 가리킬 때는 <b>반드시 이 형태 그대로</b> 쓴다. 뒤에 아/야 를 붙여
                    부르는 형태로 바꾸지 않는다("%s" 처럼 쓰면 "%s와" 같은 말이 된다).
                  ⚠ 이 이름은 받침이 없다. 뒤에 오는 조사는 가·는·를·와·랑·로 를 쓴다.
                ⚠ 조사는 <b>앞말에 반드시 붙여 쓴다.</b> "스팸 가", "문구점 에서" 처럼 띄우지 않는다.
                  받침이 있는 말 뒤에는 이·은·을·과·이랑·으로 를 쓴다("스팸이", "문구점에서").
                - 나이: %s
                - 장애 유형: %s (%s)
                - 이야기에 나오는 친구 캐릭터 이름: %s

                [오늘의 일상] %s

                [이미 만든 이야기 — 겹치지 않게 한다] %s

                [이번 이야기가 벌어지는 곳] %s

                [앞으로 다룰 주제 — 지금 쓰면 안 된다] %s

                [이번 주 학습] %s

                [이번 주 첫 이야기인가] %s

                [이야기 조건]
                - 중심 감정은 '%s' 다.
                - 친구 %s 가 그 감정을 느끼고, 아이가 도와주는 이야기다.
                - ⚠ [이번 주 첫 이야기인가] 가 '예' 면 <b>이번 주 주제를 그대로 장면으로</b> 만든다.
                  주제에 적힌 사물·사람·행동을 그대로 쓴다. 비슷한 다른 장면으로 바꾸지 않는다.
                  ("장난감이 움직이지 않아 당황한 아이가 엄마를 바라보는 장면"
                    → 장난감이 멈추고, 아이가 엄마를 바라보는 그 장면을 만든다)
                  로드맵은 보호자가 미리 읽는 계획이라, 첫 이야기가 계획과 다르면 약속이 깨진다.
                - ⚠ [앞으로 다룰 주제] 에 있는 장면은 <b>지금 만들지 않는다</b>. 그 주가 왔을 때
                  새 이야기로 만나야 한다 — 미리 써 버리면 정작 그 주에 겹친 이야기가 나온다.
                - ⚠ [오늘의 일상] 이 있으면 <b>그것이 이 이야기의 무대이자 소재</b>다.
                  보호자가 오늘 아이에게 실제로 있었던 일을 적어 준 것이라, 아이가 겪은 일로
                  배우게 하는 것이 이 서비스의 핵심이다. 거기 나온 장소·사물·사람을 그대로 쓴다.
                  ("놀이터에서 장난감을 갖고 놀았어요" → 놀이터에서 장난감을 두고 벌어지는 일)
                - [오늘의 일상] 이 '(입력 없음)' 일 때만 [이번 이야기가 벌어지는 곳] 을 무대로 쓴다.
                - ⚠ 위 '이미 만든 이야기' 와 <b>다른 장면</b>을 만든다. 같은 목표라도 장소·사건·
                  등장인물이 겹치지 않게 바꾼다. 제목만 바꿔 같은 내용을 다시 쓰지 않는다.
                - ⚠ 이번 주 로드맵 목표가 이 이야기의 뼈대다. 곁들이는 양념이 아니다.
                  아이가 그 목표를 **직접 해 보게 되는** 장면으로 만든다.
                  상황·까닭·동작·칭찬이 모두 그 목표 하나를 향하게 한다.
                  목표와 상관없는 이야기를 만들지 않는다.

                [JSON 형식] 아래 키만 쓴다.
                {
                  "title": "이야기 제목. %s 로 시작하는 한 문장",
                  "emotion": "%s",
                  "situationType": "이 이야기가 다루는 사회적 상황. 반드시 다음 중 하나를 그대로 골라 쓴다: %s",
                  "situation": "무슨 일이 있었는지 두 문장 이내",
                  "recap": "그 일로 친구가 어떤 모습이 되었는지 한 문장. 마음 읽기 화면에서 다시 짚어 준다 (예: 토리는 책장이 접혀 주먹을 꼭 쥐었어요). 반드시 위 situation 과 같은 사건이어야 한다",
                  "cause": "그 감정이 된 까닭. **10자 이내**. 카드 한 장에 들어가야 한다 (예: 넘어져서 아파서)",
                  "causeDistractor": "정답이 아닌 까닭. 10자 이내. 그림으로 구분되는 것 (예: 졸려서, 배고파서)",
                  "gesture": "아이가 할 동작. comfort(토닥토닥) | sorry(미안해) | celebrate(축하) | wave(손 흔들어 인사) 중 하나. 먼저 말 걸기·인사 상황이면 wave 를 쓴다"
                }
                """.formatted(
                child.getCallName(), child.getVocative(), child.getVocative(),
                child.promptAge(),
                nvl(child.getDisorderType(), "발달"),
                nvl(child.getSeverity(), "정도 미상"),
                friend,
                (dailyNote == null || dailyNote.isBlank()) ? "(입력 없음)" : dailyNote.trim(),
                madeBefore(brief.recentTitles()),
                nvl(brief.place(), "(정하지 않음 — 위 [오늘의 일상] 이나 [이번 주 학습] 을 따른다)"),
                listOrNone(brief.laterTopics()),
                weekBrief(week),
                brief.weekOpener() ? "예 — 주제를 그대로 장면으로 만든다" : "아니오",
                emotion, friend, friend, emotion, SituationType.promptList()
        );

        if (!gemini.isEnabled()) {
            log.warn("AI 키가 없어 이야기를 만들지 못했습니다");
            return null;
        }

        Optional<String> json = gemini.generateJson(SYSTEM, prompt);

        if (json.isEmpty()) {
            log.warn("AI 가 이야기를 돌려주지 않았습니다");
            return null;
        }

        try {
            StoryScenarioDTO sc = objectMapper.readValue(json.get(), StoryScenarioDTO.class);

            sc.setEmotion(emotion);
            sc.setSource("AI");

            String wanted = (week != null && week.getSituationType() != null)
                    ? week.getSituationType()
                    : sc.getSituationType();

            sc.setSituationType(SituationType.normalize(wanted, emotion).label());

            if (isBlank(sc.getTitle()) || isBlank(sc.getSituation())
                    || isBlank(sc.getCause()) || isBlank(sc.getCauseDistractor())) {
                log.warn("AI 이야기에 빈 칸이 있어 쓰지 않습니다");
                return null;
            }

            sc.setGesture(StoryTemplate.gesture(sc.getGesture(), StoryTemplate.emotionSet(emotion)));

            fixNames(sc, child);

            return sc;

        } catch (Exception e) {
            log.warn("시나리오 JSON 파싱 실패 — 이야기를 만들지 못했습니다: {}", e.getMessage());
            return null;
        }
    }

    private static boolean isBlank(String v) {
        return v == null || v.isBlank();
    }

    private void fixNames(StoryScenarioDTO sc, ChildDTO child) {

        String friend = StoryTemplate.charName(child.getCharacterType(), child.getCharacterNickname());

        sc.setTitle(name(sc.getTitle(), child, friend));
        sc.setSituation(name(sc.getSituation(), child, friend));
        sc.setRecap(name(sc.getRecap(), child, friend));
        sc.setCause(name(sc.getCause(), child, friend));
        sc.setCauseDistractor(name(sc.getCauseDistractor(), child, friend));
        sc.setPraise(name(sc.getPraise(), child, friend));
    }

    private String name(String text, ChildDTO child) {
        return name(text, child,
                StoryTemplate.charName(child.getCharacterType(), child.getCharacterNickname()));
    }

    private String name(String text, ChildDTO child, String friend) {

        String out = StoryTemplate.fixChildName(text, child.getGivenName(),
                child.getCallName(), child.getVocative());

        return StoryTemplate.fixJosa(out, friend);
    }

    private String weekBrief(RoadmapPlanDTO.Week week) {

        if (week == null) {
            return "(로드맵 없음 — 또래가 흔히 겪는 상황으로 만든다)";
        }

        return "%d주차 '%s' · 다룰 상황: %s · 이번 주에 해 볼 것: %s".formatted(
                week.getNo(),
                nvl(week.getTopic(), "-"),
                nvl(week.getSituationType(), "-"),
                nvl(week.getGoal(), "-"));
    }

    // ------------------------------------------------------------
    //  2) 노드 이어 쓰기
    // ------------------------------------------------------------

    @Override
    public StoryNodeDTO writeNode(ChildDTO child,
                                  StoryScenarioDTO scenario,
                                  StoryStage stage,
                                  StoryRequestDTO.Next previous) {

        String friend = StoryTemplate.charName(child.getCharacterType(), child.getCharacterNickname());
        StoryNodeDTO fallback = StoryTemplate.node(stage, scenario, child.getVocative(), friend);

        if (stage == StoryStage.STORY || !props.isPolishNodes() || !gemini.isEnabled()) {
            return fallback;
        }

        String prompt = """
                진행 중인 학습 이야기의 다음 화면 문구를 쓴다.

                [아이] %s (가리킬 때 이 형태 그대로 쓴다. "%s" 처럼 부르는 형태로 바꾸지 않는다.
                       이 이름은 받침이 없으니 가·는·를·와·랑 을 쓰고, 조사는 앞말에 붙여 쓴다), %s, %s
                [친구] %s
                [이야기] %s
                [상황] %s
                [까닭] %s
                [할 동작] %s

                [직전에 아이가 한 것] %s

                [이번 화면] %s
                %s

                [JSON 형식] 쓰지 않는 키는 null 로 둔다.
                {
                  "title": "화면 제목 한 문장",
                  "narration": "읽어 줄 문장. 반드시 채운다",
                  "questionText": "아이에게 던지는 질문이나 안내 한 줄",
                  "targetValue": "%s",
                  "coachText": "틀렸을 때 되짚어 주는 한 문장. 탓하지 않고 어디를 볼지 알려 준다",
                  "hintText": "[잘 모르겠어] 를 눌렀을 때. 답을 말하지 말고 볼 곳만 짚는다",
                  "options": %s
                }
                """.formatted(
                child.getCallName(), child.getVocative(),
                child.promptAge(),
                nvl(child.getDisorderType(), "발달"),
                friend,
                scenario.getTitle(),
                scenario.getSituation(),
                scenario.getCause(),
                scenario.getGesture(),
                describePrevious(previous),
                stageBrief(stage, friend),
                coachingRule(previous),
                fallback.getTargetValue() == null ? "null" : fallback.getTargetValue(),
                optionSpec(stage)
        );

        Optional<String> json = gemini.generateJson(SYSTEM, prompt);

        if (json.isEmpty()) {
            return fallback;
        }

        try {
            StoryNodeDTO node = merge(objectMapper.readTree(json.get()), stage, fallback);

            node.setTitle(name(node.getTitle(), child));
            node.setNarration(name(node.getNarration(), child));
            node.setQuestionText(name(node.getQuestionText(), child));
            node.setCoachText(name(node.getCoachText(), child));
            node.setHintText(name(node.getHintText(), child));

            return node;

        } catch (Exception e) {
            log.warn("{} 노드 JSON 파싱 실패 — 내장 문구로 진행합니다: {}", stage, e.getMessage());
            return fallback;
        }
    }

    // ------------------------------------------------------------
    //  프롬프트 조각
    // ------------------------------------------------------------

    private String describePrevious(StoryRequestDTO.Next prev) {

        if (prev == null || prev.getStageType() == null) {
            return "(아직 없음 — 이야기의 시작이다)";
        }

        String stage = StoryStage.of(prev.getStageType()).map(StoryStage::label).orElse(prev.getStageType());
        String value = nvl(prev.getResponseValue(), "(반응 없음)");

        if (prev.getSuccess() == null) {
            return "'%s' 화면을 보았다".formatted(stage);
        }

        return prev.getSuccess()
                ? "'%s' 에서 '%s' 라고 답해 한 번에 맞혔다".formatted(stage, value)
                : "'%s' 에서 '%s' 라고 답해 처음에 틀렸다".formatted(stage, value);
    }

    // 코딩 규칙
    private String coachingRule(StoryRequestDTO.Next prev) {

        if (prev == null || prev.getSuccess() == null) {
            return "- 아이를 반갑게 맞이하며 시작한다.";
        }

        return prev.getSuccess()
                ? "- 아이가 잘 맞혔다. 짧게 칭찬 한 마디를 섞고 곧바로 다음으로 넘어간다."
                : """
                - 아이가 앞 단계를 어려워했다. 탓하지 말고 '괜찮아' 로 시작한다.
                - 친구가 어떤 마음인지 한 번 더 풀어 말해 주고, 다시 해 보자고 이끈다.""";
    }

    // 이번 화면이 무엇을 하는 자리인지
    private String stageBrief(StoryStage stage, String friend) {
        return switch (stage) {
            case STORY -> "상황 이야기 — 무슨 일이 있었는지 보여 준다";
            case MIND -> "마음 읽기 — " + friend + "가 지금 어떤 마음인지 카드에서 고른다";
            case CAUSE -> "이유 찾기 — 왜 그런 마음이 됐는지 카드에서 고르거나 말로 답한다";
            case EXPRESSION -> "표정 따라하기 — 카메라를 보고 " + friend + "와 같은 표정을 짓는다";
            case ACTION -> "동작 따라하기 — 카메라를 보고 도와주는 동작을 한다";
            case PRAISE -> "칭찬 — 다 마친 아이에게 고맙다고 말해 준다";
        };
    }

    // 선택지가 있는 단계인지 알려 준다
    private String optionSpec(StoryStage stage) {
        return switch (stage) {
            case MIND -> """
                    [
                      {"key":"감정 키","label":"카드 제목","description":"몸이 어떤 느낌인지 8자 이내"},
                      {"key":"감정 키","label":"카드 제목","description":"같은 형식"},
                      {"key":"감정 키","label":"카드 제목","description":"같은 형식"}
                    ]
                    - 정확히 3장. key 는 happy | sad | angry | surprise 중에서만 고르고 겹치지 않게 한다.
                    - **정답 감정(targetValue)이 반드시 세 장 안에 있어야 한다.**
                    - 나머지 두 장은 이 이야기에서 헷갈릴 만한 감정으로 고른다.
                    - label 은 아이가 읽을 말로 짧게 (기뻐요 / 슬퍼요 / 화나요 / 놀랐어요 처럼).""";

            case CAUSE -> """
                    [
                      {"key":"cause","label":"까닭을 짧게","description":null,"pose":"hurt","answer":true},
                      {"key":"other","label":"정답이 아닌 까닭","description":null,"pose":"sleepy","answer":false}
                    ]  (key 는 반드시 cause / other 두 개다)""";

            default -> "null  (이 화면에는 선택지가 없다)";
        };
    }

    // ------------------------------------------------------------
    //  응답 합치기
    // ------------------------------------------------------------

    private StoryNodeDTO merge(JsonNode json, StoryStage stage, StoryNodeDTO fallback) {

        StoryNodeDTO node = StoryNodeDTO.builder()
                .nodeOrder(fallback.getNodeOrder())
                .stageType(fallback.getStageType())
                .missionType(fallback.getMissionType())
                .title(text(json, "title", fallback.getTitle()))
                .narration(text(json, "narration", fallback.getNarration()))
                .questionText(text(json, "questionText", fallback.getQuestionText()))
                .coachText(text(json, "coachText", fallback.getCoachText()))
                .hintText(text(json, "hintText", fallback.getHintText()))
                .targetValue(fallback.getTargetValue())
                .charPose(fallback.getCharPose())
                .options(fallback.getOptions())
                .build();

        JsonNode options = json.get("options");

        if (options != null && options.isArray() && !options.isEmpty()) {
            node.setOptions(mergeOptions(options, stage, fallback));
        }

        return node;
    }

    private List<StoryOptionDTO> mergeOptions(JsonNode arr, StoryStage stage, StoryNodeDTO node) {

        if (stage == StoryStage.MIND) {
            List<StoryOptionDTO> picked = readMindOptions(arr, node.getTargetValue());
            if (picked != null) {
                return picked;
            }
            log.debug("AI 가 고른 마음 카드가 규칙에 안 맞아 기본 조합을 씁니다");
        }

        List<StoryOptionDTO> fallback = node.getOptions();
        List<StoryOptionDTO> merged = new ArrayList<>(fallback.size());

        for (StoryOptionDTO base : fallback) {

            JsonNode found = null;
            for (JsonNode n : arr) {
                if (base.getKey().equalsIgnoreCase(text(n, "key", ""))) {
                    found = n;
                    break;
                }
            }

            merged.add(StoryOptionDTO.builder()
                    .key(base.getKey())
                    .label(found == null ? base.getLabel() : text(found, "label", base.getLabel()))
                    .description(found == null ? base.getDescription()
                            : text(found, "description", base.getDescription()))
                    .pose(base.getPose())
                    .answer(base.isAnswer())
                    .build());
        }

        return merged;
    }

    // AI가 고른 마음 카드 판단
    private List<StoryOptionDTO> readMindOptions(JsonNode arr, String target) {

        if (target == null || arr.size() != StoryTemplate.MIND_CARD_COUNT) {
            return null;
        }

        List<StoryOptionDTO> out = new ArrayList<>(StoryTemplate.MIND_CARD_COUNT);
        Set<String> seen = new HashSet<>();

        for (JsonNode n : arr) {
            String key = text(n, "key", "").trim().toLowerCase(Locale.ROOT);

            if (!StoryTemplate.isMindKey(key) || !seen.add(key)) {
                return null;
            }

            String[] base = StoryTemplate.mindCard(key);

            out.add(StoryOptionDTO.builder()
                    .key(key)
                    .label(text(n, "label", base[1]))
                    .description(text(n, "description", base[2]))
                    .pose(key)
                    .answer(key.equals(target))
                    .build());
        }

        if (!seen.contains(target)) {
            return null;
        }

        Collections.shuffle(out);

        return out;
    }

    private String text(JsonNode json, String field, String fallback) {
        JsonNode n = json.get(field);

        if (n == null || n.isNull()) {
            return fallback;
        }

        String v = n.asString("").trim();
        return v.isEmpty() ? fallback : v;
    }

    private static String madeBefore(java.util.List<String> titles) {

        if (titles == null || titles.isEmpty()) {
            return "(없음 — 첫 이야기다)";
        }
        return String.join(" / ", titles);
    }

    private static String listOrNone(java.util.List<String> items) {

        if (items == null || items.isEmpty()) {
            return "(없음)";
        }
        return String.join(" / ", items);
    }

    private static String nvl(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
