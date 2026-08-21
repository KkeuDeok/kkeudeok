package kopo.kkeudeok.service;

import kopo.kkeudeok.dto.ReportDTO;

// 성장 리포트
public interface IReportService {

    ReportDTO of(Long childId);
}
