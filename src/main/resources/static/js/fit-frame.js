(function () {
    var DESIGN_H = 1024;

    function fit() {
        var scale = window.innerHeight / DESIGN_H;
        if (!(scale > 0.05)) scale = 0.05;
        document.documentElement.style.setProperty('--fit-scale', scale.toFixed(4));
    }

    window.addEventListener('resize', fit);
    if (window.visualViewport) {
        window.visualViewport.addEventListener('resize', fit);
    }
    fit();
})();
