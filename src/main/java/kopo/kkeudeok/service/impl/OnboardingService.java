package kopo.kkeudeok.service.impl;

import kopo.kkeudeok.dto.ChecklistAnswerDTO;
import kopo.kkeudeok.dto.ChildDTO;
import kopo.kkeudeok.dto.OnboardingRequestDTO;
import kopo.kkeudeok.mapper.IChildMapper;
import kopo.kkeudeok.mapper.IOnboardingMapper;
import kopo.kkeudeok.service.IOnboardingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnboardingService implements IOnboardingService {

    private final IOnboardingMapper onboardingMapper;
    private final IChildMapper childMapper;

    private static final Map<String, String> DISORDER = Map.of(
            "자폐 장애", "자폐",
            "지적 장애", "지적",
            "발달 장애", "발달"
    );

    private static final java.util.regex.Pattern CHILD_NAME =
            java.util.regex.Pattern.compile("^[가-힣a-zA-Z]{2,20}$");

    @Override
    @Transactional
    public ChildDTO register(Long memberId, OnboardingRequestDTO req) {

        String name = req.getName() == null ? "" : req.getName().trim();

        if (name.isEmpty()) {
            throw new IllegalArgumentException("아이 이름이 없습니다");
        }

        if (!CHILD_NAME.matcher(name).matches()) {
            throw new IllegalArgumentException("아이 이름은 한글 또는 영문 2~20자여야 합니다");
        }

        LocalDate birth = birthDateOf(req);

        if (birth == null) {
            throw new IllegalArgumentException("생년월일이 없습니다");
        }

        if (memberId == null) {
            throw new IllegalStateException("로그인이 필요합니다");
        }

        Long owner = memberId;
        ChildDTO child = childMapper.selectChildByMember(owner);
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

        log.info("온보딩 완료 — child={}, member={}, {} ({}세), {}/{}, 친구={}, 체크리스트 {}문항",
                row.getChildId(), owner, row.getName(), row.getAge(),
                row.getDisorderType(), row.getSeverity(), row.getCharacterType(),
                req.getChecklist() == null ? 0 : req.getChecklist().size());

        return row;
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
