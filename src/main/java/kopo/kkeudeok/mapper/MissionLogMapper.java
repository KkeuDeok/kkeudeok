package kopo.kkeudeok.mapper;

import kopo.kkeudeok.dto.MissionLogDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

// 미션 수행 결과
@Mapper
public interface MissionLogMapper {

    int deleteByNodes(@Param("sessionId") Long sessionId,
                      @Param("nodeIds") List<Long> nodeIds);

    int insertLogs(@Param("logs") List<MissionLogDTO> logs);

    List<MissionLogDTO> selectBySession(@Param("sessionId") Long sessionId);

    String selectLastAnsweredStage(@Param("sessionId") Long sessionId);

    List<MissionLogDTO> selectForReport(@Param("childId") Long childId,
                                        @Param("from") LocalDateTime from);
}
