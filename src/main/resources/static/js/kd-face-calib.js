(function () {
    'use strict';

    if (!/\/onboarding\/face-capture/.test(location.pathname)) return;

    var VISION_CDN = 'https://cdn.jsdelivr.net/npm/@mediapipe/tasks-vision@0.10.14';
    var MODEL = 'https://storage.googleapis.com/mediapipe-models/face_landmarker/face_landmarker/float16/1/face_landmarker.task';

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

        var modelReady = loadModel();

        video = document.createElement('video');
        video.className = 'kd-onb-cam';
        video.autoplay = true;
        video.playsInline = true;
        video.muted = true;
        view.insertBefore(video, view.firstChild);

        navigator.mediaDevices.getUserMedia({ video: { facingMode: 'user' }, audio: false })
            .then(function (stream) {
                video.srcObject = stream;
                window.addEventListener('pagehide', function () {
                    stopped = true;
                    if (raf) cancelAnimationFrame(raf);
                    stream.getTracks().forEach(function (t) { t.stop(); });
                });
                return video.play();
            })
            .then(function () {
                view.classList.add('kd-cam-on');
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
        if (faceOk === ok) return;
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
        frames = [];
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
