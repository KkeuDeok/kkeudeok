package kopo.kkeudeok.mapper;

import kopo.kkeudeok.dto.StorySessionDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

// 학습 세션
@Mapper
public interface StorySessionMapper {

    int insertSession(StorySessionDTO session);

    StorySessionDTO selectSession(@Param("sessionId") Long sessionId);

    int countTodaySessions(@Param("childId") Long childId);

    int countTodayCompleted(@Param("childId") Long childId);

    StorySessionDTO selectResumable(@Param("childId") Long childId);

    int updateSessionClosed(@Param("sessionId") Long sessionId,
                            @Param("status") String status);
}
