package kopo.kkeudeok.mapper;

import kopo.kkeudeok.dto.ChildDTO;
import kopo.kkeudeok.dto.ChecklistAnswerDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

// 온보딩
@Mapper
public interface OnboardingMapper {

    int insertPlaceholderMember(@Param("loginId") String loginId,
                                @Param("name") String name,
                                @Param("holder") java.util.Map<String, Object> holder);

    int insertChild(ChildDTO child);

    ChildDTO selectChildByMember(@Param("memberId") Long memberId);

    int updateChild(ChildDTO child);

    int deleteChecklist(@Param("childId") Long childId);

    int insertChecklist(@Param("answers") List<ChecklistAnswerDTO> answers);

    List<ChecklistAnswerDTO> selectChecklist(@Param("childId") Long childId);
}
