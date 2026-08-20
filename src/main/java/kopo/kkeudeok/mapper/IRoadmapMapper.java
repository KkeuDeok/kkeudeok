package kopo.kkeudeok.mapper;

import kopo.kkeudeok.dto.RoadmapDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

// 학습 로드맵
@Mapper
public interface IRoadmapMapper {

    int insertRoadmap(RoadmapDTO roadmap);

    RoadmapDTO selectActive(@Param("childId") Long childId);

    int deactivateAll(@Param("childId") Long childId);
}
