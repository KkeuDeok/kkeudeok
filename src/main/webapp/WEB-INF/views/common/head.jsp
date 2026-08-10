<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<%-- 300(Light)은 온보딩 생년월일의 년·월·일 글자에만 쓰인다 --%>
<link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;400;500;700&display=swap" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
<%-- ?v= 는 캐시 무효화용 — 정적 파일 수정 시 숫자를 올릴 것 --%>
<link href="/css/kkeudeok.css?v=175" rel="stylesheet">
<link href="/css/auth.css?v=175" rel="stylesheet">
<link href="/css/onboarding.css?v=175" rel="stylesheet">
<link href="/css/app.css?v=175" rel="stylesheet">
<link href="/css/report.css?v=175" rel="stylesheet">
<link href="/css/mypage.css?v=175" rel="stylesheet">
<link href="/css/child.css?v=175" rel="stylesheet">
<%-- 사용 단계 스위치 — 0 신규(아무것도 없음) · 1 시작함(일상 1건+이야기 1편) · 2 익숙함(기본).
     ?stage=0|1|2 로 바꾸면 탭 단위로 유지돼 화면을 옮겨도 따라간다.
     ?empty=1 → 0 · ?empty=0 → 2 별칭은 예전 주소를 안 깨려고 남겨 둔다.
     블록이 깜빡이지 않게 defer 없이 <head> 에서 즉시 실행한다(fit-frame.js 와 같은 이유). --%>
<script>
    (function () {
        var q = new URLSearchParams(location.search), s = null;
        if (q.has('stage')) s = q.get('stage');
        else if (q.has('empty')) s = q.get('empty') === '0' ? '2' : '0';
        try {
            if (s !== null && '012'.indexOf(s) >= 0) sessionStorage.setItem('kdStage', s);
            s = sessionStorage.getItem('kdStage');
        } catch (e) { }
        /* ?stage= 로 직접 정한 게 없으면 **실제 학습 횟수**로 정한다(2026-08-10 요청:
           "학습을 한 번 하면 그 다음에는 수치가 나오는 대시보드로").
           0회 → 0단계 · 1~2회 → 1단계 · 3회 이상 → 2단계.
           3회 기준은 화면 문구가 이미 "세 번을 채우면 지수가 처음 계산돼요" 라서다.
           ?stage= 를 쓰면 예전처럼 그 값이 이 탭에서 계속 이긴다(시연용). */
        if (!(s !== null && '012'.indexOf(s) >= 0)) {
            var done = 0;
            try { done = +(localStorage.getItem('kdDone') || 0) || 0; } catch (e) { }
            s = done >= 3 ? '2' : (done >= 1 ? '1' : '0');
        }
        document.documentElement.dataset.kdStage = ('012'.indexOf(s) >= 0 && s) ? s : '2';
    })();
</script>
<script src="/js/fit-frame.js?v=175"></script>
<script src="/js/auth-validate.js?v=175" defer></script>
<script src="/js/onb-select.js?v=175" defer></script>
<%-- kd-roadmap.js 는 onb-select.js 가 노출하는 kdBuildSelects 를 쓰므로 반드시 뒤에 둔다 --%>
<script src="/js/kd-roadmap.js?v=175" defer></script>
