package kopo.kkeudeok.mapper;

import kopo.kkeudeok.dto.ChildDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IChildMapper {

    ChildDTO selectChild(@Param("childId") Long childId);

    ChildDTO selectChildByMember(@Param("memberId") Long memberId);

    int updateProfile(ChildDTO pDTO);

    int updateCharacter(ChildDTO pDTO);
}
