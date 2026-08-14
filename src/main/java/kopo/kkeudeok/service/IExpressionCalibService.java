package kopo.kkeudeok.service;

import kopo.kkeudeok.dto.ExpressionCalibDTO;

import java.util.List;
import java.util.Map;

// 표정 캘리브레이션
public interface IExpressionCalibService {

    ExpressionCalibDTO save(Long childId, String emotion, Map<String, Double> shapes);

    List<ExpressionCalibDTO> findByChild(Long childId);

    int deleteAll(Long childId);
}
