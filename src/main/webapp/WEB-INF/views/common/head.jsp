<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<%-- 300(Light)은 온보딩 생년월일의 년·월·일 글자에만 쓰인다 --%>
<link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;400;500;700&display=swap" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
<%-- ?v= 는 캐시 무효화용 — 정적 파일 수정 시 숫자를 올릴 것 --%>
<link href="/css/kkeudeok.css?v=123" rel="stylesheet">
<link href="/css/auth.css?v=123" rel="stylesheet">
<link href="/css/onboarding.css?v=123" rel="stylesheet">
<link href="/css/app.css?v=123" rel="stylesheet">
<link href="/css/report.css?v=123" rel="stylesheet">
<link href="/css/mypage.css?v=123" rel="stylesheet">
<link href="/css/child.css?v=123" rel="stylesheet">
<%-- 신규 가입 직후(데이터 0) 화면 스위치.
     ?empty=1 켜기 · ?empty=0 끄기 — 켠 뒤에는 탭 단위로 유지돼 화면을 옮겨도 따라간다.
     블록이 깜빡이지 않게 defer 없이 <head> 에서 즉시 실행한다(fit-frame.js 와 같은 이유). --%>
<script>
    (function () {
        var q = new URLSearchParams(location.search);
        if (q.has('empty')) {
            try { sessionStorage.setItem('kdEmpty', q.get('empty') === '0' ? '' : '1'); } catch (e) { }
        }
        try {
            if (sessionStorage.getItem('kdEmpty')) document.documentElement.dataset.kdEmpty = '1';
        } catch (e) { }
    })();
</script>
<script src="/js/fit-frame.js?v=123"></script>
<script src="/js/auth-validate.js?v=123" defer></script>
<script src="/js/onb-select.js?v=123" defer></script>
<%-- kd-roadmap.js 는 onb-select.js 가 노출하는 kdBuildSelects 를 쓰므로 반드시 뒤에 둔다 --%>
<script src="/js/kd-roadmap.js?v=123" defer></script>
