package kopo.kkeudeok.service;

import kopo.kkeudeok.dto.ChildDTO;

// 아동 조회·수정
public interface IChildService {

    ChildDTO getChild(Long childId);

    ChildDTO getChildByMember(Long memberId);

    int updateProfile(ChildDTO pDTO);

    int updateCharacter(ChildDTO pDTO);
}
