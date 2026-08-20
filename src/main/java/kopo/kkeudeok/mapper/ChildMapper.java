package kopo.kkeudeok.mapper;

import kopo.kkeudeok.dto.ChildDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

// 아동 조회
@Mapper
public interface ChildMapper {

    ChildDTO selectChild(@Param("childId") Long childId);

    ChildDTO selectChildByMember(@Param("memberId") Long memberId);
}
