package kopo.kkeudeok.mapper;

import kopo.kkeudeok.dto.ChildDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

// 아동 조회·수정
@Mapper
public interface IChildMapper {

    ChildDTO selectChild(@Param("childId") Long childId);

    ChildDTO selectChildByMember(@Param("memberId") Long memberId);

    int updateProfile(ChildDTO pDTO);

    int updateCharacter(ChildDTO pDTO);
}
