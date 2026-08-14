package kopo.kkeudeok.service.impl;

import kopo.kkeudeok.dto.MissionType;
import kopo.kkeudeok.dto.StoryNodeDTO;
import kopo.kkeudeok.dto.StoryOptionDTO;
import kopo.kkeudeok.dto.SituationType;
import kopo.kkeudeok.dto.StoryScenarioDTO;
import kopo.kkeudeok.dto.StoryStage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// 정석 12주 스토리 (AI X)
final class FallbackStory {

    private FallbackStory() {
    }

    private static final Map<String, String> CHAR_NAMES = Map.of(
            "tori", "토리",
            "koko", "코코",
            "lala", "라라",
            "bomi", "보미",
            "bada", "바다",
            "rubi", "루비"
    );

    static String charName(String characterType, String nickname) {

        if (nickname != null && !nickname.isBlank()) {
            return nickname.trim();
        }

        String v = characterType == null ? "" : characterType.trim();

        if (CHAR_NAMES.containsKey(v)) {
            return CHAR_NAMES.get(v);
        }
        if (CHAR_NAMES.containsValue(v)) {
            return v;
        }
        return "토리";
    }

    static String emotionSet(String emotion) {
        return switch (emotion == null ? "" : emotion) {
            case "happy", "angry", "surprise" -> emotion;
            default -> "sad";
        };
    }

    static StoryScenarioDTO scenario(String emotion, String friend) {

        StoryScenarioDTO sc = build(emotion, friend);
        sc.setSource("FALLBACK");
        return sc;
    }

    private static StoryScenarioDTO build(String emotion, String friend) {

        return switch (emotionSet(emotion)) {

            case "angry" -> StoryScenarioDTO.builder()
                    .title(friend + "가 쌓은 블록을 내가 실수로 무너뜨렸어요")
                    .emotion("angry")
                    .situation("열심히 만든 게 무너져서 화가 났어요.")
                    .situationType(SituationType.APOLOGIZE.label())
                    .cause("내가 무너뜨려서")
                    .causeDistractor("졸려서")
                    .gesture("sorry")
                    .praise("마음이 따뜻해졌어")
                    .build();

            case "surprise" -> StoryScenarioDTO.builder()
                    .title(friend + "가 갑자기 큰 소리에 깜짝 놀랐어요")
                    .emotion("surprise")
                    .situation("쿵 소리가 나서 눈이 동그래졌어요. 가슴이 콩콩 뛰어요.")
                    .situationType(SituationType.COMFORT.label())
                    .cause("큰 소리가 나서")
                    .causeDistractor("졸려서")
                    .gesture("comfort")
                    .praise("옆에 있어 줘서 안 무서웠어")
                    .build();

            case "happy" -> StoryScenarioDTO.builder()
                    .title(friend + "가 친구에게 깜짝 선물을 받았어요")
                    .emotion("happy")
                    .situation("너무 좋아서 깡충깡충 뛰었어요. 기분이 좋대요!")
                    .situationType(SituationType.CELEBRATE.label())
                    .cause("선물을 받아서")
                    .causeDistractor("졸려서")
                    .gesture("celebrate")
                    .praise("같이 기뻐해 줘서 고마워")
                    .build();

            default -> StoryScenarioDTO.builder()
                    .title(friend + "가 처음 간 곳에서 길을 잃을 뻔했어요")
                    .emotion("sad")
                    .situation("낯선 곳이라 무서웠어요. 그래서 울고 있어요.")
                    .situationType(SituationType.COMFORT.label())
                    .cause("길을 잃어서 무서워서")
                    .causeDistractor("졸려서")
                    .gesture("comfort")
                    .praise("마음이 따뜻해졌어")
                    .build();
        };
    }


    static StoryNodeDTO node(StoryStage stage, StoryScenarioDTO sc, String child, String friend) {

        String emo = emotionSet(sc.getEmotion());

        return switch (stage) {

            case STORY -> base(stage)
                    .title(sc.getTitle())
                    .narration(sc.getSituation())
                    .questionText(switch (emo) {
                        case "happy" -> friend + "랑 같이 기뻐할래?";
                        case "surprise" -> friend + "를 안심시켜 줄래?";
                        default -> friend + "를 도와줄래?";
                    })
                    .charPose(emo)
                    .build();

            case MIND -> base(stage)
                    .missionType(MissionType.CHOICE.name())
                    .title(friend + "는 지금 어떤 마음일까?")
                    .narration(recap(emo, friend))
                    .questionText(friend + "는 지금 어떤 마음일까?")
                    .targetValue(emo)
                    .charPose("surprise")
                    .hintText("그림 속 " + friend + "의 얼굴을 잘 봐. 어떤 표정이야?")
                    .coachText("다시 한 번 " + friend + "의 얼굴을 볼까?")
                    .options(mindOptions(emo))
                    .build();

            case CAUSE -> base(stage)
                    .missionType(MissionType.CHOICE.name())
                    .title(switch (emo) {
                        case "angry" -> friend + "는 왜 화가 났을까?";
                        case "happy" -> friend + "는 왜 기뻐졌을까?";
                        case "surprise" -> friend + "는 왜 놀랐을까?";
                        default -> friend + "는 왜 슬퍼졌을까?";
                    })
                    .narration(sc.getSituation())
                    .questionText("말로 해도 되고, 그림을 눌러도 돼")
                    .targetValue("cause")
                    .charPose(emo)
                    .hintText(switch (emo) {
                        case "angry" -> "그림 속 표정을 잘 봐. 속상해서 찌푸린 얼굴일까, 졸려서 하품하는 얼굴일까?";
                        case "happy" -> "그림 속 표정을 잘 봐. 좋아서 웃는 얼굴일까, 졸려서 하품하는 얼굴일까?";
                        case "surprise" -> "그림 속 표정을 잘 봐. 놀라서 눈이 커진 얼굴일까, 졸려서 하품하는 얼굴일까?";
                        default -> "그림 속 표정을 잘 봐. 무서워서 찡그린 얼굴일까, 졸려서 하품하는 얼굴일까?";
                    })
                    .coachText("아까 무슨 일이 있었는지 다시 떠올려 볼까?")
                    .options(causeOptions(sc, emo))
                    .build();

            case EXPRESSION -> base(stage)
                    .missionType(MissionType.EXPRESSION.name())
                    .title(switch (emo) {
                        case "angry" -> "화난 표정으로 " + friend + " 마음을 느껴봐요";
                        case "happy" -> "기쁜 표정으로 " + friend + " 마음을 느껴봐요";
                        case "surprise" -> "놀란 표정으로 " + friend + " 마음을 느껴봐요";
                        default -> "슬픈 표정으로 " + friend + " 마음을 느껴봐요";
                    })
                    .narration(friend + "와 같은 표정을 지어 볼까?")
                    .questionText("같이 해봐")
                    .targetValue(emo)
                    .charPose(emo)
                    .coachText(switch (emo) {
                        case "angry" -> "눈썹을 가운데로 모아 볼까?";
                        case "happy" -> "입꼬리를 조금만 더 올려 볼까?";
                        case "surprise" -> "눈을 크게 뜨고 입을 동그랗게 벌려 볼까?";
                        default -> "입꼬리를 조금만 더 아래로 내려 볼까?";
                    })
                    .hintText("코너에 있는 " + friend + "의 얼굴을 따라 해 봐")
                    .build();

            case ACTION -> base(stage)
                    .missionType(MissionType.GESTURE.name())
                    .title(switch (emo) {
                        case "angry" -> friend + "가 속상한가봐. 미안하다고 말해줄까?";
                        case "happy" -> friend + "가 기분이 좋대! 우리도 같이 신나게 축하해 줄까?";
                        case "surprise" -> friend + "가 깜짝 놀랐나봐. 괜찮다고 토닥여 줄까?";
                        default -> friend + "가 슬픈가봐. " + friend + "를 위로해주자";
                    })
                    .narration(friend + "에게 마음을 보여 줄 차례야.")
                    .questionText(switch (emo) {
                        case "angry" -> "이렇게 해봐";
                        case "happy" -> "같이 축하해봐";
                        case "surprise" -> "괜찮다고 토닥여봐";
                        default -> "같이 토닥여봐";
                    })
                    .targetValue(sc.getGesture() == null ? "comfort" : sc.getGesture())
                    .charPose(emo)
                    .coachText("팔을 조금만 더 크게 움직여 볼까?")
                    .hintText("코너에 있는 그림처럼 해 봐")
                    .build();

            case PRAISE -> base(stage)
                    .title("고마워, " + child + "야!")
                    .narration(sc.getPraise() == null ? "마음이 따뜻해졌어" : sc.getPraise())
                    .charPose("proud")
                    .build();
        };
    }

    private static StoryNodeDTO.StoryNodeDTOBuilder base(StoryStage stage) {
        return StoryNodeDTO.builder()
                .nodeOrder(stage.seq())
                .stageType(stage.name())
                .narration("");
    }

    private static String recap(String emo, String friend) {
        return switch (emo) {
            case "angry" -> friend + "는 쌓아 올린 블록이 무너져 주먹을 꼭 쥐었어요";
            case "happy" -> friend + "는 선물을 받고 깡충깡충 뛰었어요";
            case "surprise" -> friend + "는 큰 소리에 눈이 동그래지고 어깨가 움찔했어요";
            default -> friend + "는 낯선 곳에서 길을 잃을 뻔해 눈물이 맺혔어요";
        };
    }

    // 마음 카드 선택지
    static final String[][] MIND_CARDS = {
            {"happy", "기뻐요", "마음이 콩콩 뛰어요"},
            {"sad", "슬퍼요", "가슴이 콕콕 아파요"},
            {"angry", "화나요", "얼굴이 뜨거워져요"},
            {"surprise", "놀랐어요", "눈이 동그래져요"}
    };

    static final int MIND_CARD_COUNT = 3;

    static boolean isMindKey(String key) {
        for (String[] c : MIND_CARDS) {
            if (c[0].equals(key)) {
                return true;
            }
        }
        return false;
    }

    static String[] mindCard(String key) {
        for (String[] c : MIND_CARDS) {
            if (c[0].equals(key)) {
                return c;
            }
        }
        return new String[]{key, key, ""};
    }

    private static List<StoryOptionDTO> mindOptions(String answer) {

        List<StoryOptionDTO> out = new ArrayList<>(MIND_CARDS.length);

        for (String[] c : MIND_CARDS) {
            out.add(StoryOptionDTO.builder()
                    .key(c[0])
                    .label(c[1])
                    .description(c[2])
                    .pose(c[0])
                    .answer(c[0].equals(answer))
                    .build());
        }

        for (int i = out.size() - 1; i >= 0 && out.size() > MIND_CARD_COUNT; i--) {
            if (!out.get(i).isAnswer()) {
                out.remove(i);
            }
        }

        return out;
    }

    private static List<StoryOptionDTO> causeOptions(StoryScenarioDTO sc, String emo) {

        String causePose = switch (emo) {
            case "angry" -> "angry";
            case "happy" -> "happy";
            case "surprise" -> "surprise";
            default -> "sad";
        };

        List<StoryOptionDTO> out = new ArrayList<>(2);

        out.add(StoryOptionDTO.builder()
                .key("cause")
                .label(sc.getCause())
                .pose(causePose)
                .answer(true)
                .build());

        out.add(StoryOptionDTO.builder()
                .key("other")
                .label(sc.getCauseDistractor() == null ? "졸려서" : sc.getCauseDistractor())
                .pose("sleepy")
                .answer(false)
                .build());

        return out;
    }
}
