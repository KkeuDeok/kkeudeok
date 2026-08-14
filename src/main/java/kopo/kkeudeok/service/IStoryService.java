package kopo.kkeudeok.service;

import kopo.kkeudeok.dto.StoryRequestDTO;
import kopo.kkeudeok.dto.StoryResponseDTO;

// 학습 세션 진행
public interface IStoryService {

    StoryResponseDTO.Start start(StoryRequestDTO.Start req);

    StoryResponseDTO.Resume resume(Long childId);

    StoryResponseDTO.Next next(Long sessionId, StoryRequestDTO.Next req);

    StoryResponseDTO.Finish finish(Long sessionId, StoryRequestDTO.Finish req);
}
