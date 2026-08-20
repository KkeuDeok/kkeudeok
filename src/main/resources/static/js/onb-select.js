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

        sel.__kdSync = function () { renderList(); sync(); };
    }

    document.addEventListener('click', function (e) {
        if (openWrap && !openWrap.contains(e.target)) closeOpen();
    });
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') closeOpen();
    });

    function buildAll() {
        document.querySelectorAll('select.onb-select').forEach(function (sel) {
            if (sel.__kdSync) sel.__kdSync(); else build(sel);
        });
    }

    buildAll();

    window.kdBuildSelects = buildAll;
})();
