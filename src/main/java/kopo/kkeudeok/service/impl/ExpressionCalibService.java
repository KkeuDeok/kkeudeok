package kopo.kkeudeok.service.impl;

import kopo.kkeudeok.dto.ChildDTO;
import kopo.kkeudeok.dto.EmotionType;
import kopo.kkeudeok.dto.ExpressionCalibDTO;
import kopo.kkeudeok.mapper.IExpressionCalibMapper;
import kopo.kkeudeok.service.IChildService;
import kopo.kkeudeok.service.IExpressionCalibService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpressionCalibService implements IExpressionCalibService {

    private final IExpressionCalibMapper calibMapper;
    private final IChildService childService;
    private final ObjectMapper objectMapper;

    private static final int MAX_SHAPES = 100;

    @Override
    @Transactional
    public ExpressionCalibDTO save(Long childId, String emotion, Map<String, Double> shapes) {

        ChildDTO child = childService.getChild(childId);

        EmotionType type = EmotionType.of(emotion)
                .orElseThrow(() -> new IllegalArgumentException("알 수 없는 감정입니다: " + emotion));

        if (shapes == null || shapes.isEmpty()) {
            throw new IllegalArgumentException("표정 특징값이 비어 있습니다");
        }
        if (shapes.size() > MAX_SHAPES) {
            throw new IllegalArgumentException("표정 특징값이 너무 많습니다: " + shapes.size());
        }

        ExpressionCalibDTO calib = ExpressionCalibDTO.builder()
                .childId(child.getChildId())
                .emotionType(type.name())
                .landmarkData(toJson(shapes))
                .mediaUrl(null)
                .build();

        calibMapper.insertCalib(calib);

        log.info("표정 등록 — child={}, {}, 특징값 {}개",
                child.getChildId(), type.name(), shapes.size());

        return calib;
    }

    @Override
    public List<ExpressionCalibDTO> findByChild(Long childId) {
        return calibMapper.selectByChild(childService.getChild(childId).getChildId());
    }

    @Override
    @Transactional
    public int deleteAll(Long childId) {

        int n = calibMapper.deleteByChild(childService.getChild(childId).getChildId());
        log.info("표정 등록 삭제 — child={}, {}건", childId, n);
        return n;
    }

    private String toJson(Map<String, Double> shapes) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("v", 1);
        body.put("shapes", shapes);

        try {
            return objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new IllegalArgumentException("표정 특징값을 저장할 수 없습니다", e);
        }
    }
}
