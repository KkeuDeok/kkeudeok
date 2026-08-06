/* 1440×1024 고정 디자인 캔버스를 브라우저 창 크기에 맞춰 배율 조정.
   Figma 프레젠테이션 모드처럼 어떤 해상도에서도 전체 화면이 스크롤 없이 보인다. */
(function () {
    var DESIGN_W = 1440;
    var DESIGN_H = 1024;
    var MARGIN = 26; /* 프레임 주변 최소 여백 */

    function fit() {
        var scale = Math.min(
            (window.innerWidth - MARGIN) / DESIGN_W,
            (window.innerHeight - MARGIN) / DESIGN_H
        );
        document.documentElement.style.setProperty('--fit-scale', scale.toFixed(4));
    }

    window.addEventListener('resize', fit);
    if (window.visualViewport) {
        window.visualViewport.addEventListener('resize', fit);
    }
    fit();
})();
