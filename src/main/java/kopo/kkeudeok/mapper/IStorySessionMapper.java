package kopo.kkeudeok.mapper;

import kopo.kkeudeok.dto.StorySessionDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface IStorySessionMapper {

    int insertSession(StorySessionDTO session);

    StorySessionDTO selectSession(@Param("sessionId") Long sessionId);

    int countTodaySessions(@Param("childId") Long childId);

    java.util.List<java.time.LocalDateTime> selectCompletedStartedAt(@Param("childId") Long childId);

    int countTodayCompleted(@Param("childId") Long childId);

    StorySessionDTO selectResumable(@Param("childId") Long childId);

    StorySessionDTO selectPrepared(@Param("childId") Long childId);

    int markPreparedUsed(@Param("sessionId") Long sessionId);

    List<StorySessionDTO> selectRecent(@Param("childId") Long childId,
                                       @Param("limit") int limit);

    List<LocalDate> selectActiveDays(@Param("childId") Long childId,
                                     @Param("limit") int limit);

    int updateSessionClosed(@Param("sessionId") Long sessionId,
                            @Param("status") String status);
}
