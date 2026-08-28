package kopo.kkeudeok.mapper;

import kopo.kkeudeok.dto.RoadmapDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IRoadmapMapper {

    int insertRoadmap(RoadmapDTO roadmap);

    RoadmapDTO selectActive(@Param("childId") Long childId);

    int deactivateAll(@Param("childId") Long childId);
}
