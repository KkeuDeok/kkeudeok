package kopo.kkeudeok.mapper;

import kopo.kkeudeok.dto.ChecklistAnswerDTO;
import kopo.kkeudeok.dto.ChildDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IOnboardingMapper {

    int insertChild(ChildDTO child);

    int updateChild(ChildDTO child);

    int deleteChecklist(@Param("childId") Long childId);

    int insertChecklist(@Param("answers") List<ChecklistAnswerDTO> answers);

    List<ChecklistAnswerDTO> selectChecklist(@Param("childId") Long childId);
}
