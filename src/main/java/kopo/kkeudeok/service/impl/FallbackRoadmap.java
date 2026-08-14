package kopo.kkeudeok.service.impl;

import kopo.kkeudeok.dto.RoadmapPlanDTO;
import kopo.kkeudeok.dto.SituationType;

import java.util.ArrayList;
import java.util.List;

// 정석 12주 커리큘럼 (AI X)
final class FallbackRoadmap {

    private FallbackRoadmap() {
    }

    private static final Object[][] STAGES = {
            {"감정 표현 집중", 3},
            {"감정 이해 심화", 2},
            {"사회적 상호작용", 2},
            {"공감 실전 연습", 2},
            {"일상 적용", 2},
            {"자기 평가", 1}
    };

    private static final String[][] WEEKS = {
            {"표정으로 표현하기", SituationType.EXPRESS.label(), "기쁨과 슬픔을 표정으로 나타내 봐요"},
            {"몸짓으로 표현하기", SituationType.EXPRESS.label(), "말 대신 몸짓으로 마음을 보여 줘요"},
            {"슬픔 감정 표현하기", SituationType.EXPRESS.label(), "속상할 때 어떻게 말할지 연습해요"},
            {"복합 감정 이해하기", SituationType.COMFORT.label(), "한 상황에 여러 마음이 있을 수 있어요"},
            {"상황과 감정 잇기", SituationType.COMFORT.label(), "무슨 일이 있었는지와 마음을 이어 봐요"},
            {"먼저 말 걸어보기", SituationType.GREET.label(), "친구에게 먼저 인사해 봐요"},
            {"눈 맞추고 대화하기", SituationType.GREET.label(), "이야기할 때 얼굴을 바라봐요"},
            {"친구 위로하기", SituationType.COMFORT.label(), "속상해하는 친구를 토닥여 줘요"},
            {"다툰 뒤 화해하기", SituationType.APOLOGIZE.label(), "미안한 마음을 말로 전해요"},
            {"가정에서 연습하기", SituationType.SHARE.label(), "집에서 형제·자매와 나눠 써요"},
            {"학교에서 연습하기", SituationType.WAIT_TURN.label(), "차례를 기다리는 연습을 해요"},
            {"스스로 돌아보기", SituationType.EXPRESS.label(), "이번 과정에서 무엇이 달라졌는지 함께 봐요"}
    };

    static RoadmapPlanDTO plan() {

        List<RoadmapPlanDTO.Stage> stages = new ArrayList<>();
        for (Object[] s : STAGES) {
            stages.add(RoadmapPlanDTO.Stage.builder()
                    .name((String) s[0])
                    .weeks((Integer) s[1])
                    .build());
        }

        List<RoadmapPlanDTO.Week> weeks = new ArrayList<>();
        for (int i = 0; i < WEEKS.length; i++) {
            weeks.add(RoadmapPlanDTO.Week.builder()
                    .no(i + 1)
                    .stage(stageNameAt(stages, i))
                    .topic(WEEKS[i][0])
                    .situationType(WEEKS[i][1])
                    .goal(WEEKS[i][2])
                    .build());
        }

        return RoadmapPlanDTO.builder()
                .version(1)
                .totalWeeks(WEEKS.length)
                .stages(stages)
                .weeks(weeks)
                .source(kopo.kkeudeok.dto.RoadmapDTO.TYPE_STANDARD)
                .build();
    }

    static String stageNameAt(List<RoadmapPlanDTO.Stage> stages, int idx) {
        int acc = 0;
        for (RoadmapPlanDTO.Stage s : stages) {
            acc += s.getWeeks();
            if (idx < acc) {
                return s.getName();
            }
        }
        return stages.isEmpty() ? "" : stages.get(stages.size() - 1).getName();
    }
}
