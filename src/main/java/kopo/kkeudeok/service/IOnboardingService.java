package kopo.kkeudeok.service;

import kopo.kkeudeok.dto.ChildDTO;
import kopo.kkeudeok.dto.OnboardingRequestDTO;

// 온보딩
public interface IOnboardingService {

    ChildDTO register(Long memberId, OnboardingRequestDTO req);
}
