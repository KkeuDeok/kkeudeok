package kopo.kkeudeok.mapper;

import kopo.kkeudeok.dto.ChildDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

// 아동 조회
@Mapper
public interface ChildMapper {

    ChildDTO selectChild(@Param("childId") Long childId);
    ChildDTO selectFirstChild();

    /**
     * 이 보호자의 아이. 두 곳이 같이 쓴다.
     *  - 온보딩을 마쳤는지 판단 (null 이면 아직)
     *  - 화면에 이름·나이 표시 (예전에는 예시값 '지우'가 그대로 남았다)
     */
    ChildDTO selectChildByMember(@Param("memberId") Long memberId);
}
