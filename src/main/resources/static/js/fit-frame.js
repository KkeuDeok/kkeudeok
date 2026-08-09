/* 우측 폼 패널의 내부 세로 좌표가 항상 디자인 기준(1024px)이 되도록
   창 높이 대비 배율(--fit-scale)을 계산한다. 좌측 일러스트는 CSS(cover)가 유동 대응.
   → 창모드·전체화면·모니터 해상도와 무관하게 비율이 동일하고 스크롤이 생기지 않는다. */
(function () {
    var DESIGN_H = 1024;

    function fit() {
        var scale = window.innerHeight / DESIGN_H;
        /* 창 높이가 0 으로 잡히는 순간(탭 비활성·패널 접힘 등)이 있다. 그대로 두면 --fit-scale 이 0 이 되고
           모달의 zoom: calc(1 / var(--fit-scale)) 이 infinity 가 되어 레이아웃이 통째로 깨진다. */
        if (!(scale > 0.05)) scale = 0.05;
        document.documentElement.style.setProperty('--fit-scale', scale.toFixed(4));
    }

    window.addEventListener('resize', fit);
    if (window.visualViewport) {
        window.visualViewport.addEventListener('resize', fit);
    }
    fit();
})();
