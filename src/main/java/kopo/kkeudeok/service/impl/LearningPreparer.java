package kopo.kkeudeok.service.impl;

import kopo.kkeudeok.dto.RoadmapDTO;
import kopo.kkeudeok.dto.StoryRequestDTO;
import kopo.kkeudeok.mapper.RoadmapMapper;
import kopo.kkeudeok.mapper.StorySessionMapper;
import kopo.kkeudeok.service.IRoadmapService;
import kopo.kkeudeok.service.IStoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class LearningPreparer {

    private final IRoadmapService roadmapService;
    private final IStoryService storyService;
    private final RoadmapMapper roadmapMapper;
    private final StorySessionMapper sessionMapper;
    private final kopo.kkeudeok.mapper.StoryMapper storyMapper;

    private final kopo.kkeudeok.config.GeminiClient gemini;

    private final Set<Long> preparing = ConcurrentHashMap.newKeySet();
    private final Map<Long, Integer> failures = new ConcurrentHashMap<>();
    private final Map<Long, Long> nextTryAt = new ConcurrentHashMap<>();

    private static final long BACKOFF_BASE_MS = 30_000;
    private static final long BACKOFF_MAX_MS = 600_000;

    private boolean restingAfterFailure(Long childId) {
        Long at = nextTryAt.get(childId);
        return at != null && System.currentTimeMillis() < at;
    }

    private void noteFailure(Long childId) {
        int n = failures.merge(childId, 1, Integer::sum);
        long wait = Math.min(BACKOFF_BASE_MS * (1L << Math.min(n - 1, 5)), BACKOFF_MAX_MS);
        nextTryAt.put(childId, System.currentTimeMillis() + wait);

        log.info("준비가 {}번 연달아 실패해 {}초 뒤에 다시 해 봅니다 — child={}",
                n, wait / 1000, childId);
    }

    private void noteSuccess(Long childId) {
        failures.remove(childId);
        nextTryAt.remove(childId);
    }

    @Async
    public void prepareAsync(Long childId) {

        if (childId == null || !preparing.add(childId)) {
            return;
        }

        if (!gemini.isReady() || restingAfterFailure(childId)) {
            preparing.remove(childId);
            return;
        }

        try {
            RoadmapDTO roadmap = roadmapMapper.selectActive(childId);

            if (roadmap == null) {
                log.info("아이 {} 의 로드맵을 뒤에서 만듭니다", childId);
                roadmap = roadmapService.regenerate(childId);
            }

            if (roadmap == null) {
                log.warn("로드맵을 만들지 못해 첫 이야기도 건너뜁니다 — child={}", childId);
                noteFailure(childId);
                return;
            }

            prepareFirstStory(childId);
            noteSuccess(childId);

        } catch (Exception e) {
            log.warn("학습 준비 실패 — child={}: {}", childId, e.getMessage());
            noteFailure(childId);

        } finally {
            preparing.remove(childId);
        }
    }

    private void prepareFirstStory(Long childId) {

        if (sessionMapper.selectResumable(childId) != null) {
            log.info("이어할 학습이 있어 첫 이야기는 건너뜁니다 — child={}", childId);
            return;
        }

        if (sessionMapper.selectPrepared(childId) != null) {
            log.info("미리 만들어 둔 이야기가 이미 있습니다 — child={}", childId);
            return;
        }

        log.info("아이 {} 의 첫 이야기를 미리 만듭니다", childId);

        StoryRequestDTO.Start req = new StoryRequestDTO.Start();
        req.setChildId(childId);
        req.setPrepare(true);

        storyService.start(req);
    }

    private boolean firstStoryDone(Long childId) {
        return storyMapper.countStories(childId) > 0
                || sessionMapper.selectPrepared(childId) != null;
    }

    public IRoadmapService.Prep status(Long childId) {

        if (childId == null) {
            return IRoadmapService.Prep.NO_CHILD;
        }

        if (roadmapMapper.selectActive(childId) != null && firstStoryDone(childId)) {
            return IRoadmapService.Prep.READY;
        }
        if (preparing.contains(childId) || !gemini.isReady() || restingAfterFailure(childId)) {
            return IRoadmapService.Prep.GENERATING;
        }

        return IRoadmapService.Prep.FAILED;
    }
}
