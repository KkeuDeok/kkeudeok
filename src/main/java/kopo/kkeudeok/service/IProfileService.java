package kopo.kkeudeok.service;

import kopo.kkeudeok.dto.ProfileDTO;

public interface IProfileService {

    /**
     * 아동 프로필 및 캐릭터 정보 조회
     */
    ProfileDTO getProfile(ProfileDTO pDTO) throws Exception;
    /**
     * 아동 프로필 정보 수정 (프로필 정보 탭)
     */
    int updateProfile(ProfileDTO pDTO) throws Exception;

    /**
     * 캐릭터 및 애칭 수정 (캐릭터 관리 탭)
     */
    int updateCharacter(ProfileDTO pDTO) throws Exception;
}