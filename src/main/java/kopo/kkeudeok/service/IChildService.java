package kopo.kkeudeok.service;

import kopo.kkeudeok.dto.ChildDTO;

public interface IChildService {

    ChildDTO getChild(Long childId);

    ChildDTO getChildByMember(Long memberId);

    int updateProfile(ChildDTO pDTO);

    int updateCharacter(ChildDTO pDTO);
}
