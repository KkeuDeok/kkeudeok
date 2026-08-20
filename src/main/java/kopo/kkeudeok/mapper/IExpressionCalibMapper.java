package kopo.kkeudeok.mapper;

import kopo.kkeudeok.dto.ExpressionCalibDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

// 표정 캘리브레이션
@Mapper
public interface IExpressionCalibMapper {

    int deleteByEmotion(@Param("childId") Long childId,
                        @Param("emotionType") String emotionType);

    int insertCalib(ExpressionCalibDTO calib);

    List<ExpressionCalibDTO> selectByChild(@Param("childId") Long childId);

    int deleteByChild(@Param("childId") Long childId);
}
