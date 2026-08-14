package kopo.kkeudeok.mapper;

import kopo.kkeudeok.dto.ProfileDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IProfileMapper {

    // 아동 프로필 조회
    ProfileDTO getProfile(ProfileDTO pDTO) throws Exception;
    // 아동 프로필 정보 수정
    int updateProfile(ProfileDTO pDTO) throws Exception;
    // 캐릭터 및 애칭수정
    int updateCharacter(ProfileDTO pDTO) throws Exception;
}