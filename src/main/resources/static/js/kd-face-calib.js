// 온보딩 표정 등록
(function () {
    'use strict';

    if (!/\/onboarding\/face-capture/.test(location.pathname)) return;

    var VISION_CDN = 'https://cdn.jsdelivr.net/npm/@mediapipe/tasks-vision@0.10.14';
    var MODEL = 'https://storage.googleapis.com/mediapipe-models/face_landmarker/face_landmarker/float16/1/face_landmarker.task';

    /* 화면의 카드 순서와 같아야 한다 */
    var EMOTIONS = ['happy', 'sad', 'angry', 'surprise', 'neutral'];

    var view = document.querySelector('.onb-cam .view');
    var hint = document.querySelector('.onb-cam .hint');
    if (!view) return;

    var video = null;
    var landmarker = null;
    var latest = null;
    var raf = null;
    var stopped = false;

    start();

    function start() {
        if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
            return;
        }

        injectStyle();

        /* 모델은 카메라 권한과 동시에 받는다 — 순서대로 하면 검은 화면을 한참 본다 */
        var modelReady = loadModel();

        video = document.createElement('video');
        video.className = 'kd-onb-cam';
        video.autoplay = true;
        video.playsInline = true;
        video.muted = true;
        view.insertBefore(video, view.firstChild);

        navigator.mediaDevices.getUserMedia({ video: { facingMode: 'user' }, audio: false })
            .then(function (stream) {
                /* 페이지를 떠나면 카메라도 판정도 멈춘다 — 영상이 남지 않는다 */
                video.srcObject = stream;
                window.addEventListener('pagehide', function () {
                    stopped = true;
                    if (raf) cancelAnimationFrame(raf);
                    stream.getTracks().forEach(function (t) { t.stop(); });
                });
                return video.play();
            })
            .then(function () {
                view.classList.add('kd-cam-on');   /* 인식 준비 전에도 얼굴은 보인다 */
                return modelReady;
            })
            .then(function (fl) {
                if (!fl) return;
                landmarker = fl;
                tick();
            })
            .catch(function (e) {
                console.warn('[kkeudeok] 카메라를 켜지 못해 표정 등록을 건너뜁니다', e);
                if (video) video.remove();
                say('카메라 없이도 다음으로 넘어갈 수 있어요');
            });
    }

    function loadModel() {
        return import(VISION_CDN + '/vision_bundle.mjs').then(function (vision) {
            return vision.FilesetResolver.forVisionTasks(VISION_CDN + '/wasm').then(function (files) {
                return vision.FaceLandmarker.createFromOptions(files, {
                    baseOptions: { modelAssetPath: MODEL, delegate: 'GPU' },
                    runningMode: 'VIDEO',
                    numFaces: 1,
                    /* 좌표가 아니라 이걸 쓴다 — 판정에 필요한 건 표정의 강도다 */
                    outputFaceBlendshapes: true
                });
            });
        }).catch(function (e) {
            console.warn('[kkeudeok] 인식 모델을 받지 못해 표정 등록을 건너뜁니다', e);
            return null;
        });
    }

    function tick() {
        if (stopped) return;

        if (video && video.readyState >= 2 && landmarker) {
            try {
                var res = landmarker.detectForVideo(video, performance.now());
                var shapes = res && res.faceBlendshapes && res.faceBlendshapes[0];

                if (shapes) {
                    var v = {};

                    /* 소수점 세 자리면 충분하다 — 그대로 넣으면 JSON 이 쓸데없이 커진다 */
                    shapes.categories.forEach(function (c) {
                        v[c.categoryName] = Math.round(c.score * 1000) / 1000;
                    });

                    latest = v;
                    remember(v);
                    face(true);
                } else {
                    latest = null;
                    face(false);
                }
            } catch (e) { }
        }

        raf = requestAnimationFrame(tick);
    }

    var KEEP_MS = 300;
    var frames = [];

    function remember(v) {
        var now = performance.now();
        frames.push({ v: v, at: now });
        while (frames.length && now - frames[0].at > KEEP_MS) frames.shift();
    }

    function pick() {
        if (!frames.length) return latest;

        var sum = {};
        var keys = Object.keys(frames[0].v);

        frames.forEach(function (f) {
            keys.forEach(function (k) { sum[k] = (sum[k] || 0) + (f.v[k] || 0); });
        });

        var avg = {};
        keys.forEach(function (k) {
            avg[k] = Math.round(sum[k] / frames.length * 1000) / 1000;
        });

        return avg;
    }

    var faceOk = null;

    function face(ok) {
        if (faceOk === ok) return;      /* 문구가 깜빡이지 않게 바뀔 때만 건드린다 */
        faceOk = ok;

        if (hint) {
            hint.textContent = ok ? '좋아요! 그 표정 그대로 있어 주세요'
                                  : '얼굴을 화면 안에 맞춰 주세요';
        }
        view.classList.toggle('kd-face-ok', ok);
    }

    function reset() {
        latest = null;
        faceOk = null;
        frames = [];        /* 다음 감정에 앞 표정이 섞이면 안 된다 */
    }

    var origNext = window.kdOnbFaceNext;
    var origRetry = window.kdOnbFaceRetry;
    var idx = 0;

    window.kdOnbFaceNext = function () {
        var emotion = EMOTIONS[idx];

        if (emotion && latest) {
            save(emotion, pick());
        } else if (emotion) {
            console.warn('[kkeudeok] ' + emotion + ' 표정을 잡지 못해 등록을 건너뜁니다');
        }

        idx += 1;
        reset();

        if (typeof origNext === 'function') origNext();
    };

    /* 뒤로 가면 순서도 되돌린다 — 안 그러면 엉뚱한 감정 자리에 저장된다 */
    window.kdOnbFaceRetry = function () {
        idx = Math.max(0, idx - 1);
        reset();
        if (typeof origRetry === 'function') origRetry();
    };

    var PENDING = 'kdCalibPending';

    function pending() {
        try {
            return JSON.parse(sessionStorage.getItem(PENDING)) || [];
        } catch (e) {
            return [];
        }
    }

    function keep(rows) {
        try {
            sessionStorage.setItem(PENDING, JSON.stringify(rows));
        } catch (e) { }
    }

    function save(emotion, shapes) {
        var rows = pending().filter(function (r) { return r.emotion !== emotion; });
        rows.push({ emotion: emotion, shapes: shapes });
        keep(rows);

        send(emotion, shapes).then(function (ok) {
            if (ok) drop(emotion);
        });
    }

    function send(emotion, shapes) {
        return fetch('/api/calib', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ emotion: emotion, shapes: shapes })
        }).then(function (res) {
            return res.status === 200;
        }).catch(function () {
            return false;
        });
    }

    function drop(emotion) {
        keep(pending().filter(function (r) { return r.emotion !== emotion; }));
    }

    function say(msg) {
        if (hint) hint.textContent = msg;
    }

    function injectStyle() {
        if (document.getElementById('kdOnbCamStyle')) return;

        var st = document.createElement('style');
        st.id = 'kdOnbCamStyle';
        st.textContent =
            '.onb-cam .view{position:relative;overflow:hidden}' +
            '.kd-onb-cam{position:absolute;inset:0;width:100%;height:100%;object-fit:cover;' +
            'transform:scaleX(-1);border-radius:inherit;opacity:0;transition:opacity .4s}' +
            '.onb-cam .view.kd-cam-on .kd-onb-cam{opacity:1}' +
            '.onb-cam .view.kd-face-ok{outline:6px solid #34a36a;outline-offset:-6px}' +
            '.onb-cam .view .hint{z-index:1}';

        document.head.appendChild(st);
    }
}());
