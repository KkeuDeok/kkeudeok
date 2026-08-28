package kopo.kkeudeok.service.impl;

import kopo.kkeudeok.dto.ChecklistAnswerDTO;
import kopo.kkeudeok.dto.MissionLogDTO;
import kopo.kkeudeok.dto.ReportDTO;
import kopo.kkeudeok.mapper.IMissionLogMapper;
import kopo.kkeudeok.mapper.IOnboardingMapper;
import kopo.kkeudeok.service.IReportService;
import kopo.kkeudeok.service.IRoadmapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService implements IReportService {

    private final IMissionLogMapper missionLogMapper;
    private final IOnboardingMapper onboardingMapper;
    private final IRoadmapService roadmapService;

    private static final int LOOKBACK_DAYS = 14;

    private static final List<String> UNDERSTAND_STAGES = List.of("MIND", "CAUSE");

    private static final List<String> EXPRESS_TYPES = List.of("EXPRESSION", "GESTURE", "VOICE");

    private static final Map<String, String> EMOTION_LABEL = Map.of(
            "sad", "슬픔", "happy", "기쁨", "angry", "화남", "surprise", "놀람");

    private static boolean isEmotion(String targetValue) {
        return targetValue != null && EMOTION_LABEL.containsKey(targetValue);
    }

    @Override
    @Transactional(readOnly = true)
    public ReportDTO of(Long childId) {

        LocalDateTime now = LocalDateTime.now();
        List<MissionLogDTO> logs =
                missionLogMapper.selectForReport(childId, now.minusDays(LOOKBACK_DAYS));

        LocalDateTime weekStart = now.minusDays(7);

        List<MissionLogDTO> thisWeek = new ArrayList<>();
        List<MissionLogDTO> lastWeek = new ArrayList<>();

        for (MissionLogDTO l : logs) {
            if (l.getStartedAt() == null) {
                continue;
            }
            (l.getStartedAt().isAfter(weekStart) ? thisWeek : lastWeek).add(l);
        }

        int baseline = checklistIndex(childId);
        int understandBase = domainIndex(childId, "감정 이해");
        int expressBase = domainIndex(childId, "감정 표현");

        return ReportDTO.builder()
                .understand(rateMetric(understandBase, thisWeek, lastWeek, this::isUnderstand, "마음 읽기"))
                .express(rateMetric(expressBase, thisWeek, lastWeek, this::isExpress, "표정·동작"))
                .social(socialMetric(baseline, thisWeek, lastWeek))
                .emotions(emotionRates(thisWeek))
                .expressEmotions(expressCounts(thisWeek))
                .methodsNow(methodShares(thisWeek))
                .methodsPrev(methodShares(lastWeek))
                .domains(domainIndexes(childId))
                .weekNo(roadmapService.currentWeek(roadmapService.get(childId)))
                .hasData(!logs.isEmpty())
                .build();
    }

    private boolean isUnderstand(MissionLogDTO l) {
        return "CHOICE".equals(l.getMissionType())
                && l.getStageType() != null
                && UNDERSTAND_STAGES.contains(l.getStageType());
    }

    private boolean isExpress(MissionLogDTO l) {
        return EXPRESS_TYPES.contains(l.getMissionType());
    }

    private ReportDTO.Metric rateMetric(int baseline,
                                        List<MissionLogDTO> now,
                                        List<MissionLogDTO> before,
                                        java.util.function.Predicate<MissionLogDTO> pick,
                                        String what) {

        int total = 0, success = 0;
        for (MissionLogDTO l : now) {
            if (pick.test(l)) {
                total += 1;
                if (Boolean.TRUE.equals(l.getIsSuccess())) success += 1;
            }
        }

        int prevTotal = 0, prevSuccess = 0;
        for (MissionLogDTO l : before) {
            if (pick.test(l)) {
                prevTotal += 1;
                if (Boolean.TRUE.equals(l.getIsSuccess())) prevSuccess += 1;
            }
        }

        int score = total == 0 ? baseline : percent(success, total);
        int prev = prevTotal == 0 ? baseline : percent(prevSuccess, prevTotal);

        String basis = total == 0
                ? ""
                : "이번 주 %s 미션 %d회 중 %d회 성공".formatted(what, total, success)
                  + (prevTotal == 0 ? "" : " (지난주 %d회 중 %d회) · %d%% → %d%%"
                        .formatted(prevTotal, prevSuccess, prev, score));

        return ReportDTO.Metric.builder()
                .score(score)
                .delta(prevTotal == 0 ? 0 : score - prev)
                .total(total)
                .success(success)
                .basis(basis)
                .build();
    }

    private List<ReportDTO.EmotionRate> emotionRates(List<MissionLogDTO> now) {

        Map<String, int[]> acc = new LinkedHashMap<>();

        for (MissionLogDTO l : now) {
            if (!isUnderstand(l) || !isEmotion(l.getTargetValue())) {
                continue;
            }
            int[] a = acc.computeIfAbsent(l.getTargetValue(), k -> new int[2]);
            a[1] += 1;
            if (Boolean.TRUE.equals(l.getIsSuccess())) a[0] += 1;
        }

        List<ReportDTO.EmotionRate> out = new ArrayList<>();

        acc.forEach((key, a) -> out.add(ReportDTO.EmotionRate.builder()
                .key(key)
                .label(EMOTION_LABEL.getOrDefault(key, key))
                .rate(percent(a[0], a[1]))
                .total(a[1])
                .build()));

        out.sort((x, y) -> Integer.compare(y.rate(), x.rate()));

        return out;
    }

    private List<ReportDTO.EmotionRate> expressCounts(List<MissionLogDTO> now) {

        Map<String, int[]> acc = new LinkedHashMap<>();

        for (MissionLogDTO l : now) {
            if (!isExpress(l) || !isEmotion(l.getTargetValue())) {
                continue;
            }
            int[] a = acc.computeIfAbsent(l.getTargetValue(), k -> new int[2]);
            a[1] += 1;
            if (Boolean.TRUE.equals(l.getIsSuccess())) a[0] += 1;
        }

        List<ReportDTO.EmotionRate> out = new ArrayList<>();

        acc.forEach((key, a) -> out.add(ReportDTO.EmotionRate.builder()
                .key(key)
                .label(EMOTION_LABEL.getOrDefault(key, key))
                .rate(percent(a[0], a[1]))
                .total(a[1])
                .build()));

        out.sort((x, y) -> Integer.compare(y.total(), x.total()));

        return out;
    }

    private static final Map<String, String> METHOD_LABEL = new LinkedHashMap<>(Map.of(
            "VOICE", "말", "EXPRESSION", "표정", "GESTURE", "몸짓"));

    private List<ReportDTO.MethodShare> methodShares(List<MissionLogDTO> logs) {

        Map<String, Integer> count = new LinkedHashMap<>();
        int total = 0;

        for (MissionLogDTO l : logs) {
            if (!isExpress(l)) {
                continue;
            }
            count.merge(l.getMissionType(), 1, Integer::sum);
            total += 1;
        }

        List<ReportDTO.MethodShare> out = new ArrayList<>();

        for (Map.Entry<String, String> e : METHOD_LABEL.entrySet()) {
            int n = count.getOrDefault(e.getKey(), 0);
            out.add(ReportDTO.MethodShare.builder()
                    .key(e.getKey())
                    .label(e.getValue())
                    .percent(percent(n, total))
                    .count(n)
                    .build());
        }

        return out;
    }

    private static final List<String> SOCIAL_DOMAINS =
            List.of("사회적 상호작용", "감정 표현", "감정 이해");

    private static final int SCALE_MIN = 1;
    private static final int SCALE_MAX = 7;
    private static final float MISSION_HALF_WEIGHT = 20f;

    private static int weightPercent(int total) {
        return Math.round(total / (total + MISSION_HALF_WEIGHT) * 100);
    }

    private static int blend(int baseline, int success, int total) {

        if (total == 0) {
            return baseline;
        }

        float w = total / (total + MISSION_HALF_WEIGHT);

        return Math.round(baseline * (1 - w) + percent(success, total) * w);
    }

    private ReportDTO.Metric socialMetric(int baseline,
                                          List<MissionLogDTO> now,
                                          List<MissionLogDTO> before) {

        int total = 0, success = 0;
        for (MissionLogDTO l : now) {
            if (isExpress(l) || isUnderstand(l)) {
                total += 1;
                if (Boolean.TRUE.equals(l.getIsSuccess())) success += 1;
            }
        }

        int score = blend(baseline, success, total);

        String basis = total == 0 ? ""
                : "체크리스트 지수 %d점과 이번 주 미션 %d회 중 %d회 성공을 %d:%d 로 본 값이에요"
                        .formatted(baseline, total, success,
                                100 - weightPercent(total), weightPercent(total));

        int prevTotal = 0, prevSuccess = 0;
        for (MissionLogDTO l : before) {
            if (isExpress(l) || isUnderstand(l)) {
                prevTotal += 1;
                if (Boolean.TRUE.equals(l.getIsSuccess())) prevSuccess += 1;
            }
        }

        int prev = prevTotal == 0 ? score : blend(baseline, prevSuccess, prevTotal);

        return ReportDTO.Metric.builder()
                .score(score)
                .delta(score - prev)
                .total(total)
                .success(success)
                .basis(basis)
                .build();
    }

    private int checklistIndex(Long childId) {

        List<ChecklistAnswerDTO> rows = onboardingMapper.selectChecklist(childId);

        if (rows == null || rows.isEmpty()) {
            return 50;
        }

        int sum = 0, n = 0;

        for (ChecklistAnswerDTO r : rows) {
            if (r.getDomain() == null || r.getScore() == null
                    || !SOCIAL_DOMAINS.contains(r.getDomain())) {
                continue;
            }
            sum += r.getScore();
            n += 1;
        }

        if (n == 0) {
            return 50;
        }

        return invert((double) sum / n);
    }

    private int domainIndex(Long childId, String domain) {

        List<ChecklistAnswerDTO> rows = onboardingMapper.selectChecklist(childId);

        if (rows == null || rows.isEmpty()) {
            return 50;
        }

        int sum = 0, n = 0;

        for (ChecklistAnswerDTO r : rows) {
            if (domain.equals(r.getDomain()) && r.getScore() != null) {
                sum += r.getScore();
                n += 1;
            }
        }

        return n == 0 ? 50 : invert((double) sum / n);
    }

    private static final List<String> RADAR_DOMAINS =
            List.of("감정 이해", "감정 표현", "사회적 상호작용");

    private List<ReportDTO.DomainIndex> domainIndexes(Long childId) {

        List<ChecklistAnswerDTO> rows = onboardingMapper.selectChecklist(childId);

        Map<String, int[]> acc = new LinkedHashMap<>();

        if (rows != null) {
            for (ChecklistAnswerDTO r : rows) {
                if (r.getDomain() == null || r.getScore() == null) {
                    continue;
                }
                int[] a = acc.computeIfAbsent(r.getDomain(), k -> new int[2]);
                a[0] += r.getScore();
                a[1] += 1;
            }
        }

        List<ReportDTO.DomainIndex> out = new ArrayList<>(RADAR_DOMAINS.size());

        for (String domain : RADAR_DOMAINS) {
            int[] a = acc.get(domain);

            int score = (a == null || a[1] == 0) ? 50 : invert((double) a[0] / a[1]);

            out.add(ReportDTO.DomainIndex.builder().label(domain).score(score).build());
        }

        return out;
    }

    private static int invert(double avg) {
        return (int) Math.round((SCALE_MAX - avg) / (SCALE_MAX - SCALE_MIN) * 100);
    }

    private static int percent(int part, int whole) {
        return whole == 0 ? 0 : (int) Math.round(part * 100.0 / whole);
    }
}
