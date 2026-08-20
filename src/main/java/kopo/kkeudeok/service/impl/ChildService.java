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

        if (childId == null) {
            throw new IllegalStateException("아이를 찾을 수 없습니다. 다시 로그인해 주세요");
        }

        ChildDTO child = childMapper.selectChild(childId);

        if (child == null) {
            throw new IllegalStateException("아동 " + childId + " 번을 찾을 수 없습니다");
        }

        return child;
    }
}
