/* 우측 폼 패널의 내부 세로 좌표가 항상 디자인 기준(1024px)이 되도록
   창 높이 대비 배율(--fit-scale)을 계산한다. 좌측 일러스트는 CSS(cover)가 유동 대응.
   → 창모드·전체화면·모니터 해상도와 무관하게 비율이 동일하고 스크롤이 생기지 않는다. */
(function () {
    var DESIGN_H = 1024;

    function fit() {
        var scale = window.innerHeight / DESIGN_H;
        document.documentElement.style.setProperty('--fit-scale', scale.toFixed(4));
    }

    window.addEventListener('resize', fit);
    if (window.visualViewport) {
        window.visualViewport.addEventListener('resize', fit);
    }
    fit();
})();
