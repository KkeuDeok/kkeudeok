package kopo.kkeudeok.mapper;

import kopo.kkeudeok.dto.MissionLogDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

// 미션 수행 결과
@Mapper
public interface MissionLogMapper {

    int deleteByNodes(@Param("sessionId") Long sessionId,
                      @Param("nodeIds") List<Long> nodeIds);

    int insertLogs(@Param("logs") List<MissionLogDTO> logs);

    List<MissionLogDTO> selectBySession(@Param("sessionId") Long sessionId);

    /**
     * 아이가 실제로 답한 가장 마지막 단계(stage_type). 아직 아무것도 안 했으면 null.
     *
     * 이어하기 화면을 정할 때 쓴다. "마지막으로 만들어진 노드"를 쓰면 안 된다 —
     * 화면이 다음 노드를 미리 받아 두기 때문에(prefetch) 아이가 보지도 않은 단계까지
     * 진행한 것으로 쳐서 이야기 화면을 건너뛴다.
     */
    String selectLastAnsweredStage(@Param("sessionId") Long sessionId);
}
