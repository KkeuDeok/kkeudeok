package kopo.kkeudeok.controller;

import jakarta.servlet.http.HttpSession;
import kopo.kkeudeok.dto.ReportDTO;
import kopo.kkeudeok.service.IReportService;
import kopo.kkeudeok.util.SessionKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final IReportService reportService;

    @GetMapping
    public ResponseEntity<ReportDTO> get(@RequestParam(required = false) Long childId,
                                         HttpSession session) {

        return ResponseEntity.ok(reportService.of(SessionKeys.childId(session, childId)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ReportDTO> empty(Exception e) {

        log.warn("성장 리포트를 만들지 못했습니다: {}", e.getMessage());

        return ResponseEntity.ok(ReportDTO.builder().hasData(false).build());
    }
}
