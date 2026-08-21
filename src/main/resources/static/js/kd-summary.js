(function () {
    'use strict';

    var PATHS = ['/dashboard', '/learn'];
    if (PATHS.indexOf(location.pathname.replace(/\/$/, '')) < 0) return;

    fetch('/api/story/summary')
        .then(function (r) { return r.ok ? r.json() : null; })
        .then(function (d) {
            if (!d) return;
            paintStreak(d);
            paintRecent(d.recent || []);
        })
        .catch(function (e) {
            console.warn('[kkeudeok] 학습 요약을 받지 못했습니다', e);
            paintRecent([]);
        });

    function paintStreak(d) {
        var el = document.getElementById('dashStreak');
        if (!el) return;

        var n = d.streakDays || 0;

        el.querySelector('b').textContent = n + '일';
        el.querySelector('span').textContent = n > 0 ? '연속 이용 중' : '오늘 시작해 볼까요?';

        paintWeek(d.activeDays || []);
    }

    function paintWeek(activeDays) {
        var wrap = document.getElementById('dashWeek');
        if (!wrap) return;

        var done = {};
        activeDays.forEach(function (s) { done[s] = true; });
        var today = new Date();
        var monday = new Date(today);
        monday.setDate(today.getDate() - ((today.getDay() + 6) % 7));

        var cells = wrap.children;

        for (var i = 0; i < cells.length; i++) {
            var day = new Date(monday);
            day.setDate(monday.getDate() + i);

            var mark = cells[i].querySelector('.mk');
            if (mark) mark.className = 'mk ' + (done[iso(day)] ? 'on' : 'off');

            cells[i].title = (day.getMonth() + 1) + '.' + day.getDate();
        }
    }

    function iso(d) {
        var m = String(d.getMonth() + 1).padStart(2, '0');
        var day = String(d.getDate()).padStart(2, '0');
        return d.getFullYear() + '-' + m + '-' + day;
    }

    function paintRecent(rows) {
        var table = document.getElementById('learnRecentTable');
        var body = document.getElementById('learnRecentBody');
        if (!table || !body) return;

        var empty = document.querySelector('.learn-recent .kd-empty');

        if (empty) {
            empty.classList.remove('kd-no-data');
            empty.hidden = rows.length > 0;
        }

        if (!rows.length) {
            table.hidden = true;
            return;
        }

        body.textContent = '';

        rows.forEach(function (r) {
            var tr = document.createElement('tr');
            tr.innerHTML = '<td class="c-no"></td><td class="c-date"></td>'
                + '<td class="c-story"></td>'
                + '<td class="c-state"><span class="chip"></span></td>';

            tr.querySelector('.c-no').textContent = r.no;
            tr.querySelector('.c-date').textContent = r.date;
            tr.querySelector('.c-story').textContent = r.title || '-';

            var chip = tr.querySelector('.chip');
            chip.classList.add(r.completed ? 'chip-done' : 'chip-cont');
            chip.textContent = r.completed ? '완료' : '미완료';

            body.appendChild(tr);
        });

        table.hidden = false;
    }
}());
