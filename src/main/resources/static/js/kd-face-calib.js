/* 온보딩 표정 등록 — 아이가 지은 표정 5개를 그 아이 기준값으로 저장한다.
 *
 * 왜 필요한가
 *   아이마다 웃는 얼굴의 모양이 다르다. 범용 기준값만 쓰면 어떤 아이는 아무리 웃어도
 *   학습4(표정 따라하기)를 통과하지 못한다. 그건 학습이 아니라 좌절이다.
 *   여기서 등록한 값을 kd-mediapipe.js 가 판정 기준으로 쓴다.
 *
 * ⚠ 얼굴 사진은 서버로 보내지 않는다
 *   화면이 "원본 사진은 등록이 끝나면 바로 지워져요" 라고 약속했다. 그 약속을 지키는
 *   가장 확실한 방법은 애초에 안 보내는 것이다. 브라우저에서 표정 특징값(블렌드셰이프)만
 *   뽑아 보내고, 영상은 이 페이지를 떠나는 순간 사라진다.
 *
 * ⚠ 못 켜도 온보딩은 끝까지 간다
 *   카메라를 거부했거나 모델을 못 받으면 기존 흐름([다음 표정] 클릭으로 넘어가기)이
 *   그대로 살아 있다. 등록이 없으면 학습4 가 범용 기준값으로 판정한다.
 */
(function () {
    'use strict';

    if (!/\/onboarding\/face-capture/.test(location.pathname)) return;

    var VISION_CDN = 'https://cdn.jsdelivr.net/npm/@mediapipe/tasks-vision@0.10.14';
    var MODEL = 'https://storage.googleapis.com/mediapipe-models/face_landmarker/face_landmarker/float16/1/face_landmarker.task';

    /* 화면의 카드 순서와 같아야 한다 — 기쁨 · 슬픔 · 화남 · 놀람 · 무표정 */
    var EMOTIONS = ['happy', 'sad', 'angry', 'surprise', 'neutral'];

    var view = document.querySelector('.onb-cam .view');
    var hint = document.querySelector('.onb-cam .hint');
    if (!view) return;

    var video = null;
    var landmarker = null;
    var latest = null;      /* 마지막 프레임의 블렌드셰이프 — [다음 표정] 누를 때 이걸 저장한다 */
    var raf = null;

    start();

    function start() {
        if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
            return;                                   /* 오래된 브라우저 — 기존 흐름 유지 */
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
                video.srcObject = stream;
                /* 페이지를 떠나면 카메라를 끈다 — 영상이 남지 않는다 */
                window.addEventListener('pagehide', function () {
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
        if (video && video.readyState >= 2 && landmarker) {
            try {
                var res = landmarker.detectForVideo(video, performance.now());
                var shapes = res && res.faceBlendshapes && res.faceBlendshapes[0];

                if (shapes) {
                    var v = {};
                    shapes.categories.forEach(function (c) {
                        /* 소수점 세 자리면 충분하다 — 그대로 넣으면 JSON 이 쓸데없이 커진다 */
                        v[c.categoryName] = Math.round(c.score * 1000) / 1000;
                    });
                    latest = v;
                    face(true);
                } else {
                    latest = null;
                    face(false);
                }
            } catch (e) { /* 한 프레임 실패로 멈추지 않는다 */ }
        }
        raf = requestAnimationFrame(tick);
    }

    var faceOk = null;

    function face(ok) {
        if (faceOk === ok) return;                 /* 문구가 깜빡이지 않게 바뀔 때만 건드린다 */
        faceOk = ok;
        if (hint) hint.textContent = ok ? '좋아요! 그 표정 그대로 있어 주세요' : '얼굴을 화면 안에 맞춰 주세요';
        view.classList.toggle('kd-face-ok', ok);
    }

    /* ---------- [다음 표정] 가로채기 ----------
       auth-validate.js 의 kdOnbFaceNext() 가 화면을 한 칸 넘긴다. 넘기기 전에
       지금 표정을 저장한다. 저장은 기다리지 않는다 — 아이를 기다리게 하지 않으려고
       보내 놓고 바로 넘어간다(실패해도 온보딩은 계속된다). */
    var origNext = window.kdOnbFaceNext;
    var idx = 0;

    window.kdOnbFaceNext = function () {
        var emotion = EMOTIONS[idx];

        if (emotion && latest) {
            save(emotion, latest);
        } else if (emotion) {
            console.warn('[kkeudeok] ' + emotion + ' 표정을 잡지 못해 등록을 건너뜁니다');
        }

        idx += 1;
        latest = null;
        faceOk = null;

        if (typeof origNext === 'function') origNext();
    };

    /* '다시 찍기'로 뒤로 가면 순서도 되돌린다 — 안 그러면 엉뚱한 감정에 저장된다 */
    var origRetry = window.kdOnbFaceRetry;

    window.kdOnbFaceRetry = function () {
        idx = Math.max(0, idx - 1);
        latest = null;
        faceOk = null;
        if (typeof origRetry === 'function') origRetry();
    };

    function save(emotion, shapes) {
        fetch('/api/calib', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ emotion: emotion, shapes: shapes })
        }).then(function (res) {
            if (!res.ok) throw new Error('HTTP ' + res.status);
        }).catch(function (e) {
            /* 등록이 없으면 학습4 가 범용 기준값으로 판정한다 — 막지 않는다 */
            console.warn('[kkeudeok] ' + emotion + ' 표정을 저장하지 못했습니다', e);
        });
    }

    function say(msg) {
        if (hint) hint.textContent = msg;
    }

    /* 온보딩 CSS 를 건드리지 않으려고 여기서 넣는다(건드리면 head.jsp 의 ?v= 를 다 올려야 한다) */
    function injectStyle() {
        if (document.getElementById('kdOnbCamStyle')) return;

        var st = document.createElement('style');
        st.id = 'kdOnbCamStyle';
        st.textContent =
            '.onb-cam .view{position:relative;overflow:hidden}' +
            '.kd-onb-cam{position:absolute;inset:0;width:100%;height:100%;object-fit:cover;' +
            /* 거울처럼 뒤집는다 — 안 뒤집으면 아이가 자기 얼굴을 낯설게 느낀다 */
            'transform:scaleX(-1);border-radius:inherit;opacity:0;transition:opacity .4s}' +
            '.onb-cam .view.kd-cam-on .kd-onb-cam{opacity:1}' +
            '.onb-cam .view.kd-face-ok{outline:6px solid #34a36a;outline-offset:-6px}' +
            '.onb-cam .view .guide,.onb-cam .view .hint{z-index:1}';

        document.head.appendChild(st);
    }
}());
