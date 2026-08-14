package kopo.kkeudeok.service;

import kopo.kkeudeok.dto.ChildDTO;

// 아동 조회
public interface IChildService {

    ChildDTO getChild(Long childId);
}
