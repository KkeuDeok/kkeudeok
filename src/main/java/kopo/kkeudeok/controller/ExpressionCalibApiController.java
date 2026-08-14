package kopo.kkeudeok.controller;

import jakarta.servlet.http.HttpSession;
import kopo.kkeudeok.dto.EmotionType;
import kopo.kkeudeok.dto.ExpressionCalibDTO;
import kopo.kkeudeok.dto.StoryResponseDTO;
import kopo.kkeudeok.service.IExpressionCalibService;
import kopo.kkeudeok.util.SessionKeys;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

// 온보딩 표정 등록 API
@Slf4j
@RestController
@RequestMapping("/api/calib")
@RequiredArgsConstructor
public class ExpressionCalibApiController {

    private final IExpressionCalibService calibService;

    @PostMapping
    public ResponseEntity<Saved> save(@RequestBody SaveRequest req, HttpSession session) {

        Long childId = SessionKeys.childId(session, req.getChildId());
        ExpressionCalibDTO saved = calibService.save(childId, req.getEmotion(), req.getShapes());

        return ResponseEntity.ok(Saved.builder()
                .calibId(saved.getCalibId())
                .emotionType(saved.getEmotionType())
                .build());
    }

    // 표정 등록본 조회
    @GetMapping
    public ResponseEntity<Map<String, Object>> list(@RequestParam(required = false) Long childId,
                                                   HttpSession session) {

        List<ExpressionCalibDTO> rows = calibService.findByChild(SessionKeys.childId(session, childId));

        Map<String, Object> out = new java.util.LinkedHashMap<>();

        for (ExpressionCalibDTO row : rows) {
            String key = EmotionType.of(row.getEmotionType())
                    .map(EmotionType::screenKey)
                    .orElse(row.getEmotionType());

            out.put(key, row.getLandmarkData());
        }

        return ResponseEntity.ok(Map.of("count", rows.size(), "calib", out));
    }

    @DeleteMapping
    public ResponseEntity<Map<String, Integer>> delete(@RequestParam(required = false) Long childId,
                                                      HttpSession session) {
        return ResponseEntity.ok(Map.of("deleted",
                calibService.deleteAll(SessionKeys.childId(session, childId))));
    }

    // 등록 요청
    @Data
    public static class SaveRequest {

        private Long childId;

        private String emotion;

        private Map<String, Double> shapes;
    }

    @Builder
    public record Saved(Long calibId, String emotionType) {
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<StoryResponseDTO.Fail> badRequest(IllegalArgumentException e) {

        log.warn("표정 등록 잘못된 요청: {}", e.getMessage());

        return ResponseEntity.badRequest().body(StoryResponseDTO.Fail.builder()
                .code("BAD_REQUEST")
                .message(e.getMessage())
                .build());
    }
}
