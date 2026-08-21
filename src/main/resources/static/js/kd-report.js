// 성장 리포트
(function () {
    'use strict';

    var path = location.pathname.replace(/\/$/, '');
    if (path.indexOf('/report') !== 0 && path !== '/dashboard') return;

    fetch('/api/report')
        .then(function (r) { return r.ok ? r.json() : null; })
        .then(function (d) {
            if (!d) return;
            document.documentElement.dataset.kdReport = d.hasData ? 'ready' : 'empty';

            paintMetric('understand', d.understand);
            paintMetric('express', d.express);
            paintMetric('social', d.social);
            paintEmotions(d.emotions || []);
            paintExpressBars(d.expressEmotions || []);
            paintMethods('rptMethodNow', d.methodsNow || []);
            paintMethods('rptMethodPrev', d.methodsPrev || []);
            paintRadar(d.domains || []);
        })
        .catch(function (e) {
            console.warn('[kkeudeok] 성장 리포트를 받지 못했습니다', e);
            document.documentElement.dataset.kdReport = 'empty';
        });

    function paintMetric(key, m) {
        if (!m) return;

        var el = document.querySelector('[data-rpt="' + key + '"]');

        if (el) {
            if (el.firstChild && el.firstChild.nodeType === 3) {
                el.firstChild.nodeValue = m.score;
            }

            var delta = el.querySelector('.delta');
            if (delta) paintDelta(delta, m.delta);
        }

        var basis = document.querySelector('[data-rpt-basis="' + key + '"]');
        if (basis) basis.textContent = m.basis || '';

        var bar = document.querySelector('[data-rpt-bar="' + key + '"]');
        if (bar) bar.style.width = m.score + '%';

        var dl = document.querySelector('[data-rpt-delta="' + key + '"]');
        if (dl) {
            dl.classList.remove('dl-up', 'dl-down');

            if (!m.delta) {
                dl.textContent = '변화 없음';
            } else {
                dl.classList.add(m.delta > 0 ? 'dl-up' : 'dl-down');
                dl.textContent = (m.delta > 0 ? '+' : '') + m.delta + '%p';
            }
        }
    }

    function paintDelta(el, delta) {
        el.classList.remove('rpt-up', 'rpt-down');

        if (!delta) {
            el.textContent = '';
            el.hidden = true;
            return;
        }

        el.hidden = false;
        el.classList.add(delta > 0 ? 'rpt-up' : 'rpt-down');
        el.textContent = (delta > 0 ? '▲' : '▼') + Math.abs(delta) + ' · 지난주 대비';
    }

    var BAR = { happy: 'em-joy', sad: 'em-sad', surprise: 'em-sup', angry: 'em-mad' };

    function emptyNote(wrap, msg) {
        var p = document.createElement('p');
        p.className = 'rpt-basis';
        p.textContent = msg;
        wrap.appendChild(p);
    }

    function paintEmotions(rows) {
        var wrap = document.getElementById('rptEmotionBars');
        if (!wrap) return;

        wrap.textContent = '';

        if (!rows.length) {
            emptyNote(wrap, '아직 마음 읽기 기록이 없어요. 이야기를 한 편 마치면 여기에 쌓여요');
            return;
        }

        rows.forEach(function (r) {
            var row = document.createElement('div');
            row.className = 'rpt-hbar';
            row.innerHTML = '<span class="lb"></span>'
                + '<div class="track"><span></span></div>'
                + '<span class="pct"></span>';

            row.querySelector('.lb').textContent = r.label;
            row.querySelector('.pct').textContent = r.rate + '%';

            var fill = row.querySelector('.track span');
            fill.className = BAR[r.key] || 'em-help';
            fill.style.width = r.rate + '%';

            wrap.appendChild(row);
        });
    }

    var MAX_H = 144;

    function paintExpressBars(rows) {
        var wrap = document.getElementById('rptExpressBars');
        if (!wrap) return;

        wrap.textContent = '';

        if (!rows.length) {
            emptyNote(wrap, '아직 표정·동작 기록이 없어요');
            return;
        }

        var top = 1;
        rows.forEach(function (r) { top = Math.max(top, r.total || 0); });

        rows.forEach(function (r) {
            var box = document.createElement('div');
            box.className = 'rpt-bar';
            box.innerHTML = '<b></b><span></span><em></em>';

            box.querySelector('b').textContent = r.total;
            box.querySelector('em').textContent = r.label;

            var bar = box.querySelector('span');
            bar.className = BAR[r.key] || 'em-help';
            bar.style.height = Math.max(8, Math.round((r.total || 0) / top * MAX_H)) + 'px';

            wrap.appendChild(box);
        });
    }

    var METHOD_COLOR = {
        VOICE: '#34a36a',        /* 말 */
        EXPRESSION: '#3d8fe0',   /* 표정 */
        GESTURE: '#0fa394'       /* 몸짓 */
    };

    function paintMethods(id, rows) {
        var wrap = document.getElementById(id);
        if (!wrap) return;

        wrap.textContent = '';

        var any = rows.some(function (r) { return r.percent; });

        wrap.classList.toggle('is-empty', !any);

        if (!any) {
            var none = document.createElement('span');
            none.className = 'none';
            none.textContent = '데이터가 없습니다';
            wrap.appendChild(none);
            return;
        }

        rows.forEach(function (r) {
            if (!r.percent) return;

            var span = document.createElement('span');
            span.style.width = r.percent + '%';

            span.dataset.kdMethod = r.key;
            span.style.background = METHOD_COLOR[r.key] || '#9aa3a0';
            span.textContent = r.percent + '%';
            span.title = r.label + ' ' + r.count + '회';

            wrap.appendChild(span);
        });
    }

    var CX = 220, CY = 200, R = 160;
    var NS = 'http://www.w3.org/2000/svg';
    var LABEL_GAP = 34;

    function angleAt(i, n) {
        return -Math.PI / 2 + i * (2 * Math.PI / n);
    }

    function pointAt(i, ratio, n) {
        var ang = angleAt(i, n);
        return [CX + R * ratio * Math.cos(ang), CY + R * ratio * Math.sin(ang)];
    }

    function polyPoints(ratios) {
        var n = ratios.length;
        return ratios.map(function (v, i) {
            var p = pointAt(i, v, n);
            return p[0].toFixed(1) + ',' + p[1].toFixed(1);
        }).join(' ');
    }

    function ring(r, n) {
        var ratios = [];
        for (var i = 0; i < n; i++) ratios.push(r);
        return polyPoints(ratios);
    }

    function el(name, attrs) {
        var e = document.createElementNS(NS, name);
        Object.keys(attrs).forEach(function (k) { e.setAttribute(k, attrs[k]); });
        return e;
    }

    function paintRadar(domains) {
        var g = document.getElementById('rptRadarBody');
        if (!g) return;

        g.textContent = '';

        var n = domains.length;

        if (n < 3) return;      /* 축이 셋 미만이면 면이 안 생겨 선 하나만 남는다 */

        var grid = el('g', {
            fill: 'none', stroke: '#e6ebe8', 'stroke-width': '1', 'stroke-dasharray': '4 4'
        });

        [1, 0.75, 0.5, 0.25].forEach(function (r) {
            grid.appendChild(el('polygon', { points: ring(r, n) }));
        });

        for (var i = 0; i < n; i++) {
            var end = pointAt(i, 1, n);
            grid.appendChild(el('line', {
                x1: CX, y1: CY, x2: end[0].toFixed(1), y2: end[1].toFixed(1)
            }));
        }

        g.appendChild(grid);

        g.appendChild(el('polygon', {
            points: polyPoints(domains.map(function (d) {
                return Math.max(0, Math.min(100, d.score)) / 100;
            })),
            fill: 'rgba(52,163,106,0.18)',
            stroke: '#34a36a',
            'stroke-width': '2.5',
            'stroke-linejoin': 'round'
        }));

        var text = el('g', { 'font-family': 'Noto Sans KR, sans-serif', 'text-anchor': 'middle' });

        domains.forEach(function (d, i) {
            var p = pointAt(i, 1, n);
            var ang = angleAt(i, n);
            var x = p[0] + LABEL_GAP * Math.cos(ang);
            var y = p[1] + LABEL_GAP * Math.sin(ang);

            var name = el('text', {
                x: x.toFixed(1), y: y.toFixed(1),
                'font-size': '16', 'font-weight': '700', fill: '#2b3a33'
            });
            name.textContent = d.label;

            var val = el('text', {
                x: x.toFixed(1), y: (y + 22).toFixed(1),
                'font-size': '14', fill: '#6b7a72'
            });
            val.textContent = d.score;

            text.appendChild(name);
            text.appendChild(val);
        });

        g.appendChild(text);

        var svg = document.getElementById('rptRadar');
        if (svg) {
            svg.setAttribute('aria-label', '체크리스트 영역별 지수 — '
                + domains.map(function (d) { return d.label + ' ' + d.score; }).join(', '));
        }
    }
}());
