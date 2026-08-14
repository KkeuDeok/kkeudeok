package kopo.kkeudeok.mapper;

import kopo.kkeudeok.dto.StoryDTO;
import kopo.kkeudeok.dto.StoryNodeDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

// 이야기 & 이야기 노드
@Mapper
public interface StoryMapper {

    int insertStory(StoryDTO story);

    int updateStoryTitle(@Param("storyId") Long storyId, @Param("title") String title);

    StoryDTO selectStory(@Param("storyId") Long storyId);

    int insertNode(StoryNodeDTO node);

    List<StoryNodeDTO> selectNodes(@Param("storyId") Long storyId);

    StoryNodeDTO selectNodeByOrder(@Param("storyId") Long storyId,
                                   @Param("nodeOrder") Integer nodeOrder);
}
