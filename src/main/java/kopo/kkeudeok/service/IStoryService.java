package kopo.kkeudeok.service;

import kopo.kkeudeok.dto.StoryRequestDTO;
import kopo.kkeudeok.dto.StoryResponseDTO;

public interface IStoryService {

    StoryResponseDTO.Start start(StoryRequestDTO.Start req);

    StoryResponseDTO.Resume resume(Long childId);

    StoryResponseDTO.Next next(Long sessionId, Long childId, StoryRequestDTO.Next req);

    StoryResponseDTO.Finish finish(Long sessionId, Long childId, StoryRequestDTO.Finish req);

    void prefetchNext(Long sessionId, String fromStage);

    StoryResponseDTO.Summary summary(Long childId);
}
