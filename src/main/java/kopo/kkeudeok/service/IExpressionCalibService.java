package kopo.kkeudeok.service;

import kopo.kkeudeok.dto.ExpressionCalibDTO;

import java.util.List;
import java.util.Map;

public interface IExpressionCalibService {

    ExpressionCalibDTO save(Long childId, String emotion, Map<String, Double> shapes);

    List<ExpressionCalibDTO> findByChild(Long childId);

    int deleteAll(Long childId);
}
