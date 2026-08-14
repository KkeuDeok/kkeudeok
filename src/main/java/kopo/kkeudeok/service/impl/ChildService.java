package kopo.kkeudeok.service.impl;

import kopo.kkeudeok.dto.ChildDTO;
import kopo.kkeudeok.mapper.ChildMapper;
import kopo.kkeudeok.service.IChildService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

// 아동 조회
@Slf4j
@Service
@RequiredArgsConstructor
public class ChildService implements IChildService {

    private final ChildMapper childMapper;

    @Override
    public ChildDTO getChild(Long childId) {

        ChildDTO child = (childId == null) ? null : childMapper.selectChild(childId);

        if (child == null) {
            if (childId != null) {
                log.warn("아동 {} 번을 찾지 못해 첫 아이로 진행합니다 — 로그인이 붙으면 이 되돌림을 지울 것", childId);
            }
            child = childMapper.selectFirstChild();
        }

        if (child == null) {
            throw new IllegalStateException("등록된 아이가 없습니다 — child 테이블을 먼저 채우세요");
        }

        return child;
    }
}
