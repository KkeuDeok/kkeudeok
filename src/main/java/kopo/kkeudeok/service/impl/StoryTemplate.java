package kopo.kkeudeok.service.impl;

import kopo.kkeudeok.dto.CharacterType;
import kopo.kkeudeok.dto.MissionType;
import kopo.kkeudeok.dto.StoryNodeDTO;
import kopo.kkeudeok.dto.StoryOptionDTO;
import kopo.kkeudeok.dto.StoryScenarioDTO;
import kopo.kkeudeok.dto.StoryStage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

//  이야기 화면 템플릿
final class StoryTemplate {

    private StoryTemplate() {
    }

    private static boolean hasJong(String word) {

        if (word == null || word.isBlank()) {
            return false;
        }

        char last = word.trim().charAt(word.trim().length() - 1);
        int code = last - 0xAC00;

        return code >= 0 && code < 11172 && code % 28 != 0;
    }

    static String with(String word, String pair) {
        String[] p = pair.split("/");
        return word + (hasJong(word) ? p[0] : p[1]);
    }

    static String fixChildName(String text, String given, String callName, String vocative) {

        if (text == null || text.isBlank() || callName == null || callName.isBlank()) {
            return text;
        }

        String out = text;

        if (vocative != null && !vocative.isBlank() && !vocative.equals(callName)) {
            out = out.replace(vocative, callName);
        }

        if (given != null && !given.isBlank() && !given.equals(callName)) {
            out = out.replaceAll(java.util.regex.Pattern.quote(given) + "(?![이아])",
                    java.util.regex.Matcher.quoteReplacement(callName));
        }

        return fixParticles(out, callName);
    }

    private static final String[][] JOSA = {
            {"이랑", "랑"}, {"으로", "로"}, {"이", "가"}, {"은", "는"}, {"을", "를"}, {"과", "와"}
    };

    private static final String[] FLAT_JOSA = {
            "에게서", "한테서", "에게", "한테", "에서", "처럼", "부터", "까지", "에", "의", "도", "만"
    };

    static String fixParticles(String text, String word) {

        if (text == null || text.isBlank() || word == null || word.isBlank()) {
            return text;
        }

        String w = word.trim();
        String quoted = java.util.regex.Pattern.quote(w);
        boolean jong = hasJong(w);

        String out = text;

        for (String[] p : JOSA) {
            out = out.replaceAll(quoted + "\\s*(?:" + p[0] + "|" + p[1] + ")(?![가-힣])",
                    java.util.regex.Matcher.quoteReplacement(w + (jong ? p[0] : p[1])));
        }

        for (String flat : FLAT_JOSA) {
            out = out.replaceAll(quoted + "\\s+" + flat + "(?![가-힣])",
                    java.util.regex.Matcher.quoteReplacement(w + flat));
        }

        return out;
    }

    private static final java.util.regex.Pattern LOOSE_JOSA = java.util.regex.Pattern.compile(
            "([가-힣])[ \\t]+(이랑|으로|에게서|한테서|에게|한테|에서|처럼|부터|까지|은|는|을|를|과|의|에)(?=[ \\t]|$)");

    static String tightenParticles(String text) {

        if (text == null || text.isBlank()) {
            return text;
        }

        return LOOSE_JOSA.matcher(text).replaceAll("$1$2");
    }

    static String fixJosa(String text, String word) {
        return tightenParticles(fixParticles(text, word));
    }

    static String charName(String characterType, String nickname) {

        if (nickname != null && !nickname.isBlank()) {
            return nickname.trim();
        }

        return CharacterType.labelOf(characterType);
    }

    static String emotionSet(String emotion) {
        return switch (emotion == null ? "" : emotion) {
            case "happy", "angry", "surprise" -> emotion;
            default -> "sad";
        };
    }

    static StoryNodeDTO node(StoryStage stage, StoryScenarioDTO sc, String child, String friend) {

        String emo = emotionSet(sc.getEmotion());

        return switch (stage) {

            case STORY -> base(stage)
                    .title(sc.getTitle())
                    .narration(sc.getSituation())
                    .questionText(switch (emo) {
                        case "happy" -> with(friend, "이랑/랑") + " 같이 기뻐할래?";
                        case "surprise" -> with(friend, "을/를") + " 안심시켜 줄래?";
                        default -> with(friend, "을/를") + " 도와줄래?";
                    })
                    .charPose(emo)
                    .build();

            case MIND -> base(stage)
                    .missionType(MissionType.CHOICE.name())
                    .title(with(friend, "은/는") + " 지금 어떤 마음일까?")
                    .narration(recap(sc, emo, friend))
                    .questionText(with(friend, "은/는") + " 지금 어떤 마음일까?")
                    .targetValue(emo)
                    .charPose("surprise")
                    .hintText("그림 속 " + friend + "의 얼굴을 잘 봐. 어떤 표정이야?")
                    .coachText("다시 한 번 " + friend + "의 얼굴을 볼까?")
                    .options(mindOptions(emo))
                    .build();

            case CAUSE -> base(stage)
                    .missionType(MissionType.CHOICE.name())
                    .title(switch (emo) {
                        case "angry" -> with(friend, "은/는") + " 왜 화가 났을까?";
                        case "happy" -> with(friend, "은/는") + " 왜 기뻐졌을까?";
                        case "surprise" -> with(friend, "은/는") + " 왜 놀랐을까?";
                        default -> with(friend, "은/는") + " 왜 슬퍼졌을까?";
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
                    .coachText(recap(sc, emo, friend))
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
                    .narration(with(friend, "과/와") + " 같은 표정을 지어 볼까?")
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

            case ACTION -> {
                String act = gesture(sc.getGesture(), emo);

                yield base(stage)
                        .missionType(MissionType.GESTURE.name())
                        .title(feels(friend, emo) + " " + doWhat(friend, act))
                        .narration(friend + "에게 마음을 보여 줄 차례야.")
                        .questionText(switch (act) {
                            case "sorry" -> "두 손을 모아 봐";
                            case "celebrate" -> "두 손을 활짝 펴고 번쩍 들어 봐";
                            case "wave" -> "손을 펴고 흔들어 봐";
                            default -> "손을 들고 토닥토닥 해 봐";
                        })
                        .targetValue(act)
                        .charPose(emo)
                        .coachText(switch (act) {
                            case "sorry" -> "두 손을 가슴 앞에 모아 볼까?";
                            case "celebrate" -> "손가락을 펴고 두 손을 얼굴 위로 번쩍 들어 볼까?";
                            case "wave" -> "손가락을 활짝 펴고 좌우로 흔들어 볼까?";
                            default -> friend + " 쪽으로 손을 가져가서 위아래로 토닥여 볼까?";
                        })
                        .hintText(switch (act) {
                            case "sorry" -> "두 손을 가슴 앞에 모아 봐";
                            case "celebrate" -> "두 손을 활짝 펴고 높이 들어 봐";
                            case "wave" -> "손을 펴고 좌우로 흔들어 봐";
                            default -> "손을 들고 위아래로 토닥여 봐";
                        })
                        .build();
            }

            case PRAISE -> base(stage)
                    .title("고마워, " + child + "!")
                    .narration("고마워!")
                    .charPose("proud")
                    .build();
        };
    }

    private static String feels(String friend, String emo) {
        return switch (emo) {
            case "angry" -> with(friend, "이/가") + " 속상한가봐.";
            case "happy" -> with(friend, "이/가") + " 기분이 좋대!";
            case "surprise" -> with(friend, "이/가") + " 깜짝 놀랐나봐.";
            default -> with(friend, "이/가") + " 슬픈가봐.";
        };
    }

    private static String doWhat(String friend, String act) {
        return switch (act) {
            case "sorry" -> "미안하다고 말해줄까?";
            case "celebrate" -> "같이 신나게 축하해 줄까?";
            case "wave" -> friend + "에게 손 흔들어 인사해 볼까?";
            default -> with(friend, "을/를") + " 토닥여 주자";
        };
    }

    private static final Set<String> GESTURES = Set.of("comfort", "sorry", "celebrate", "wave");

    static String gesture(String raw, String emo) {

        String v = raw == null ? "" : raw.trim().toLowerCase();

        if (GESTURES.contains(v)) {
            return v;
        }

        return switch (emo == null ? "" : emo) {
            case "angry" -> "sorry";
            case "happy" -> "celebrate";
            default -> "comfort";
        };
    }

    private static StoryNodeDTO.StoryNodeDTOBuilder base(StoryStage stage) {
        return StoryNodeDTO.builder()
                .nodeOrder(stage.seq())
                .stageType(stage.name())
                .narration("");
    }

    private static String recap(StoryScenarioDTO sc, String emo, String friend) {

        String written = sc == null ? null : sc.getRecap();

        if (written != null && !written.isBlank()) {
            return written.trim();
        }

        String situation = sc == null ? null : sc.getSituation();

        if (situation != null && !situation.isBlank()) {
            return situation.trim();
        }

        return switch (emo) {
            case "angry" -> with(friend, "은/는") + " 화가 난 것 같아요";
            case "happy" -> with(friend, "은/는") + " 기분이 좋아 보여요";
            case "surprise" -> with(friend, "은/는") + " 깜짝 놀란 것 같아요";
            default -> with(friend, "은/는") + " 슬퍼 보여요";
        };
    }

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

        List<StoryOptionDTO> out = new ArrayList<>(2);

        out.add(StoryOptionDTO.builder()
                .key("cause")
                .label(sc.getCause())
                .pose(emotionSet(emo))
                .answer(true)
                .build());

        out.add(StoryOptionDTO.builder()
                .key("other")
                .label(sc.getCauseDistractor() == null ? "졸려서" : sc.getCauseDistractor())
                .pose("sleepy")
                .answer(false)
                .build());

        Collections.shuffle(out);

        return out;
    }
}
