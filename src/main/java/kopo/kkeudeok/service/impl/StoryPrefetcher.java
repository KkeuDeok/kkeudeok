package kopo.kkeudeok.service.impl;

import kopo.kkeudeok.service.IStoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoryPrefetcher {

    private final IStoryService storyService;

    @Async
    @EventListener
    public void onNodeReady(StoryService.NodeReady e) {
        storyService.prefetchNext(e.sessionId(), e.stageType());
    }
}
