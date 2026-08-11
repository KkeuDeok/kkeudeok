/* 드롭다운 목록 통일
   브라우저가 그리는 <select> 기본 목록은 CSS 로 꾸밀 수 없어서
   년(38개)·월(12개)·일(31개) 목록의 높이·스크롤바가 제각각으로 보였다.
   원래 <select> 는 지우지 않고 화면에서만 감춘 뒤 같은 모양의 목록을 씌운다
   → 값·폼 전송·auth-validate.js 의 검사는 그대로 동작한다. */
(function () {
    'use strict';

    var openWrap = null;

    function closeOpen() {
        if (!openWrap) return;
        openWrap.classList.remove('is-open');
        openWrap.querySelector('.kd-select-btn').setAttribute('aria-expanded', 'false');
        openWrap = null;
    }

    function build(sel) {
        var wrap = document.createElement('div');
        wrap.className = 'kd-select';
        sel.parentNode.insertBefore(wrap, sel);
        wrap.appendChild(sel);

        var btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'kd-select-btn kd-input onb-select';
        btn.setAttribute('aria-haspopup', 'listbox');
        btn.setAttribute('aria-expanded', 'false');
        if (sel.id) btn.id = sel.id + 'Btn';
        wrap.appendChild(btn);

        var list = document.createElement('ul');
        list.className = 'kd-select-list';
        list.setAttribute('role', 'listbox');
        wrap.appendChild(list);

        /* 옵션이 통째로 바뀌는 select 가 있다 (장애 정도 — 유형에 따라 목록이 다르다) */
        function renderList() {
            list.innerHTML = '';
            [].forEach.call(sel.options, function (o, i) {
                var li = document.createElement('li');
                li.setAttribute('role', 'option');
                li.textContent = o.textContent;
                li.dataset.index = i;
                list.appendChild(li);
            });
        }

        function sync() {
            var o = sel.options[sel.selectedIndex];
            btn.textContent = o ? o.textContent : '';
            btn.classList.toggle('is-placeholder', !sel.value);
            [].forEach.call(list.children, function (li, i) {
                var on = i === sel.selectedIndex;
                li.classList.toggle('is-on', on);
                li.setAttribute('aria-selected', on ? 'true' : 'false');
            });
        }

        function pick(i) {
            if (i < 0 || i >= sel.options.length) return;
            sel.selectedIndex = i;
            sync();
            /* input 은 실시간 오류 해제용, change 는 일반 폼 이벤트용 */
            sel.dispatchEvent(new Event('input', { bubbles: true }));
            sel.dispatchEvent(new Event('change', { bubbles: true }));
        }

        btn.addEventListener('click', function () {
            var wasOpen = wrap.classList.contains('is-open');
            closeOpen();
            if (wasOpen) return;
            wrap.classList.add('is-open');
            btn.setAttribute('aria-expanded', 'true');
            openWrap = wrap;
            var on = list.querySelector('.is-on');
            if (on) list.scrollTop = on.offsetTop - list.clientHeight / 2;
        });

        /* 목록을 열지 않고도 위·아래 키로 고를 수 있게 — 기본 select 와 같은 감각 */
        btn.addEventListener('keydown', function (e) {
            if (e.key !== 'ArrowDown' && e.key !== 'ArrowUp') return;
            e.preventDefault();
            pick(sel.selectedIndex + (e.key === 'ArrowDown' ? 1 : -1));
        });

        list.addEventListener('click', function (e) {
            var li = e.target.closest('li');
            if (!li) return;
            pick(+li.dataset.index);
            closeOpen();
        });

        renderList();
        sync();
        /* 값·옵션이 코드로 바뀐 뒤 목록과 버튼 글씨를 다시 맞출 수 있게 붙여 둔다
           (kdBuildSelects 와 auth-validate.js 의 syncDisLevels 가 쓴다).
           ⚠ 목록까지 다시 그리므로 pick() 안에서는 부르지 말 것 — 열려 있는 목록이 통째로 갈린다 */
        sel.__kdSync = function () { renderList(); sync(); };
    }

    document.addEventListener('click', function (e) {
        if (openWrap && !openWrap.contains(e.target)) closeOpen();
    });
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') closeOpen();
    });

    /* 이미 감싼 select 는 다시 감싸면 래퍼가 겹친다 — 버튼 글씨만 다시 맞춘다 */
    function buildAll() {
        document.querySelectorAll('select.onb-select').forEach(function (sel) {
            if (sel.__kdSync) sel.__kdSync(); else build(sel);
        });
    }

    buildAll();

    /* 값을 코드로 바꾸면 이미 만들어진 버튼 글씨가 안 따라온다 —
       로드맵 편집기처럼 select 값을 통째로 갈아끼우는 화면이 다시 맞출 수 있게 내보낸다 */
    window.kdBuildSelects = buildAll;
})();
