package kopo.kkeudeok.service.impl;

import kopo.kkeudeok.dto.ChecklistAnswerDTO;
import kopo.kkeudeok.dto.ChildDTO;
import kopo.kkeudeok.dto.OnboardingRequestDTO;
import kopo.kkeudeok.mapper.OnboardingMapper;
import kopo.kkeudeok.service.IOnboardingService;
import kopo.kkeudeok.service.IRoadmapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 온보딩
@Slf4j
@Service
@RequiredArgsConstructor
public class OnboardingService implements IOnboardingService {

    private final OnboardingMapper onboardingMapper;
    private final IRoadmapService roadmapService;

    private static final Map<String, String> DISORDER = Map.of(
            "자폐 장애", "자폐",
            "지적 장애", "지적",
            "발달 장애", "발달"
    );

    /** 아이 이름 규칙 — 보호자 이름(auth-validate.js 의 NAME_RE)과 같다. */
    private static final java.util.regex.Pattern CHILD_NAME =
            java.util.regex.Pattern.compile("^[가-힣a-zA-Z]{2,20}$");

    @Override
    @Transactional
    public ChildDTO register(Long memberId, OnboardingRequestDTO req) {

        String name = req.getName() == null ? "" : req.getName().trim();

        if (name.isEmpty()) {
            throw new IllegalArgumentException("아이 이름이 없습니다");
        }

        /* 화면에서도 같은 규칙으로 막지만(auth-validate.js) 그건 개발자도구로 우회된다.
           이름은 학습 화면 곳곳에 "안녕 {이름}야!" 로 박히므로 여기서 한 번 더 본다 —
           숫자·기호가 들어와 "안녕 이1야!" 가 되던 것을 막는다(2026-08-14). */
        if (!CHILD_NAME.matcher(name).matches()) {
            throw new IllegalArgumentException("아이 이름은 한글 또는 영문 2~20자여야 합니다");
        }

        LocalDate birth = birthDateOf(req);

        if (birth == null) {
            throw new IllegalArgumentException("생년월일이 없습니다");
        }

        Long owner = (memberId != null) ? memberId : createPlaceholderMember(name);
        ChildDTO child = onboardingMapper.selectChildByMember(owner);
        boolean isNew = (child == null);

        ChildDTO row = ChildDTO.builder()
                .childId(isNew ? null : child.getChildId())
                .memberId(owner)
                .name(name)
                .birthDate(birth)
                .gender(blankToNull(req.getGender()))
                .disorderType(disorderOf(req.getDisType()))
                .severity(blankToNull(req.getDisLevel()))
                .characterType(req.getCharKey() == null || req.getCharKey().isBlank()
                        ? "tori" : req.getCharKey().trim())
                .characterNickname(blankToNull(req.getNickname()))
                .build();

        if (isNew) {
            onboardingMapper.insertChild(row);
        } else {
            onboardingMapper.updateChild(row);
        }

        saveChecklist(row.getChildId(), req.getChecklist());
        roadmapService.regenerate(row.getChildId());

        log.info("온보딩 완료 — child={}, member={}, {} ({}세), {}/{}, 친구={}, 체크리스트 {}문항",
                row.getChildId(), owner, row.getName(), row.getAge(),
                row.getDisorderType(), row.getSeverity(), row.getCharacterType(),
                req.getChecklist() == null ? 0 : req.getChecklist().size());

        return row;
    }

    /**
     * 자리를 채우는 회원.
     *
     * <p>⚠ 임시 — 로그인/회원가입이 붙으면 이 메서드와 매퍼의 짝을 지우고,
     * 세션에서 꺼낸 memberId 를 그대로 쓰면 된다.
     */
    private Long createPlaceholderMember(String childName) {

        String loginId = "onboarding+" + System.currentTimeMillis() + "@kkeudeok.local";
        Map<String, Object> holder = new HashMap<>();

        onboardingMapper.insertPlaceholderMember(loginId, childName + " 보호자", holder);

        Object id = holder.get("memberId");
        Long memberId = (id instanceof Number n) ? n.longValue() : null;

        if (memberId == null) {
            throw new IllegalStateException("회원을 만들지 못했습니다");
        }

        log.warn("로그인이 없어 자리를 채우는 회원 {} 을 만들었습니다 — 로그인이 붙으면 이 경로를 지울 것",
                memberId);

        return memberId;
    }

    private void saveChecklist(Long childId, List<OnboardingRequestDTO.Answer> answers) {

        onboardingMapper.deleteChecklist(childId);

        if (answers == null || answers.isEmpty()) {
            log.info("체크리스트 응답이 없어 프로필만으로 로드맵을 짭니다 — child={}", childId);
            return;
        }

        List<ChecklistAnswerDTO> rows = new ArrayList<>(answers.size());

        for (OnboardingRequestDTO.Answer a : answers) {

            if (a == null || a.getDomain() == null || a.getScore() == null) {
                continue;
            }

            rows.add(ChecklistAnswerDTO.builder()
                    .childId(childId)
                    .domain(a.getDomain().trim())
                    .questionNo(a.getQuestionNo() == null ? 1 : a.getQuestionNo())
                    .score(a.getScore())
                    .build());
        }

        if (!rows.isEmpty()) {
            onboardingMapper.insertChecklist(rows);
        }
    }

    private LocalDate birthDateOf(OnboardingRequestDTO req) {

        if (req.getBirthY() == null || req.getBirthM() == null || req.getBirthD() == null) {
            return null;
        }

        try {
            return LocalDate.of(req.getBirthY(), req.getBirthM(), req.getBirthD());
        } catch (Exception e) {
            log.warn("생년월일이 올바르지 않습니다: {}-{}-{}",
                    req.getBirthY(), req.getBirthM(), req.getBirthD());
            return null;
        }
    }

    private String disorderOf(String raw) {

        if (raw == null || raw.isBlank()) {
            return null;
        }

        String v = raw.trim();
        String mapped = DISORDER.get(v);

        if (mapped != null) {
            return mapped;
        }

        return v.length() <= 20 ? v : v.substring(0, 20);
    }

    private static String blankToNull(String v) {
        return (v == null || v.isBlank()) ? null : v.trim();
    }
}
