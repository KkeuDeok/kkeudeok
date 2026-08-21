package kopo.kkeudeok.service.impl;

import kopo.kkeudeok.dto.ChildDTO;
import kopo.kkeudeok.mapper.IChildMapper;
import kopo.kkeudeok.service.IChildService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 아동 조회·수정
@Slf4j
@Service
@RequiredArgsConstructor
public class ChildService implements IChildService {

    private final IChildMapper childMapper;

    @Override
    public ChildDTO getChild(Long childId) {

        if (childId == null) {
            throw new IllegalStateException("아이를 찾을 수 없습니다. 다시 로그인해 주세요");
        }

        ChildDTO child = childMapper.selectChild(childId);

        if (child == null) {
            throw new IllegalStateException("아동 " + childId + " 번을 찾을 수 없습니다");
        }

        return child;
    }

    @Override
    public ChildDTO getChildByMember(Long memberId) {

        if (memberId == null) {
            return null;
        }

        return childMapper.selectChildByMember(memberId);
    }

    @Transactional
    @Override
    public int updateProfile(ChildDTO pDTO) {

        int res = childMapper.updateProfile(pDTO);
        log.info("아동 프로필 수정 — childId={}, 반영={}", pDTO.getChildId(), res);

        return res;
    }

    @Transactional
    @Override
    public int updateCharacter(ChildDTO pDTO) {

        int res = childMapper.updateCharacter(pDTO);
        log.info("아동 캐릭터 수정 — childId={}, 반영={}", pDTO.getChildId(), res);

        return res;
    }
}
