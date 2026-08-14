package kopo.kkeudeok.service.impl;

import kopo.kkeudeok.dto.ProfileDTO;
import kopo.kkeudeok.mapper.IProfileMapper;
import kopo.kkeudeok.service.IProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProfileService implements IProfileService {

    private final IProfileMapper profileMapper;

    @Override
    public ProfileDTO getProfile(ProfileDTO pDTO) throws Exception {
        log.info("{}.getProfile Start!", this.getClass().getName());

        // DB에서 조회 후 null 일 경우 빈 DTO 객체 반환 (NullPointerException 방지)
        ProfileDTO rDTO = Optional.ofNullable(profileMapper.getProfile(pDTO))
                .orElseGet(ProfileDTO::new);

        log.info("{}.getProfile End!", this.getClass().getName());
        return rDTO;
    }

    @Transactional
    @Override
    public int updateProfile(ProfileDTO pDTO) throws Exception {
        log.info("{}.updateProfile Start!", this.getClass().getName());

        int res = profileMapper.updateProfile(pDTO);

        log.info("updateProfile res : {}", res);
        log.info("{}.updateProfile End!", this.getClass().getName());
        return res;
    }

    @Transactional
    @Override
    public int updateCharacter(ProfileDTO pDTO) throws Exception {
        log.info("{}.updateCharacter Start!", this.getClass().getName());

        int res = profileMapper.updateCharacter(pDTO);

        log.info("updateCharacter res : {}", res);
        log.info("{}.updateCharacter End!", this.getClass().getName());
        return res;
    }
}