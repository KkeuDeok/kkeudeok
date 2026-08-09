/* 아동 학습 흐름(스토리) 전용 동작.
   원칙: 화면을 새로 띄우지 않는다 — 아이 입장에서 페이지가 통째로 바뀌면 '새로고침'처럼 보여
   방금 뭘 눌렀는지 알 수 없다. 눌린 자리에서 바로 반응이 보여야 한다.
   JS 가 없으면 링크·주소가 그대로 동작하므로(점진적 향상) 화면이 깨지지는 않는다. */
(function () {
    'use strict';

    /* ---------- 오답 카드 → 그 자리에서 모달 ----------
       예전엔 ?wrong= 을 붙여 서버가 다시 그렸는데, 화면이 깜빡여서 아이가
       "졸려서를 눌렀더니 새로고침만 됐다"고 느꼈다. 이제 클릭을 가로채 모달만 띄운다. */
    var dim = document.querySelector('.story-dim');

    function openWrong(pickEl) {
        if (!dim) return false;
        document.querySelectorAll('.feel-card.is-pick, .why-card.is-pick')
            .forEach(function (el) { el.classList.remove('is-pick'); });
        if (pickEl) pickEl.classList.add('is-pick');
        dim.hidden = false;
        /* 다시 열 때도 등장 동작이 걸리게 클래스를 한 번 뗐다 붙인다 */
        dim.classList.remove('is-on');
        void dim.offsetWidth;
        dim.classList.add('is-on');
        var btn = dim.querySelector('.ft a');
        if (btn) btn.focus();
        return true;
    }

    function closeWrong() {
        if (!dim) return;
        dim.classList.remove('is-on');
        dim.hidden = true;
        document.querySelectorAll('.feel-card.is-pick, .why-card.is-pick')
            .forEach(function (el) { el.classList.remove('is-pick'); });
    }

    document.querySelectorAll('[data-wrong]').forEach(function (card) {
        card.addEventListener('click', function (e) {
            if (openWrong(card)) e.preventDefault();
        });
    });

    if (dim) {
        dim.querySelectorAll('[data-close]').forEach(function (b) {
            b.addEventListener('click', function (e) { e.preventDefault(); closeWrong(); });
        });
        /* 바깥을 눌러도 닫힌다 — 아이가 모달에 갇히면 안 된다 */
        dim.addEventListener('click', function (e) { if (e.target === dim) closeWrong(); });
        document.addEventListener('keydown', function (e) {
            if (e.key === 'Escape' && !dim.hidden) closeWrong();
        });
    }

    /* ---------- [말로 알려줄래!] → 그 자리에서 듣는 중 ----------
       화면을 새로 띄우지 않고 버튼만 파형으로 바꾼다. 다시 누르면 되돌아온다. */
    var mic = document.querySelector('.why-mic');
    var listen = document.querySelector('.why-listen');
    if (mic && listen) {
        mic.addEventListener('click', function (e) {
            e.preventDefault();
            var on = listen.hidden;
            listen.hidden = !on;
            mic.hidden = on;
            /* TODO: 실제 음성 인식(STT)이 붙으면 여기서 시작·중지를 건다 */
        });
        listen.addEventListener('click', function () {
            listen.hidden = true;
            mic.hidden = false;
        });
    }

    /* ---------- 세션 결과 — 아무 데나 누르면 아동홈으로 ----------
       Figma 캔버스에 "3초 있다가 홈 화면으로" 메모가 있지만, 아이가 칭찬을 다 보기 전에
       넘어가면 안 되므로 자동 이동 대신 터치로 넘긴다. */
    var done = document.querySelector('.done-stage');
    if (done) {
        done.addEventListener('click', function () {
            location.href = done.getAttribute('data-home');
        });
    }
}());
