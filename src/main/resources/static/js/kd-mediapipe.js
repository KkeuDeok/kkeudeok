/* 표정·동작·음성 인식 — 학습4(표정)·학습5(동작)·학습3(이유 찾기, 말로 답하기).
 *
 * ⚠ 인식은 전부 이 브라우저 안에서 끝난다. 영상과 음성은 서버로 보내지 않는다.
 *   서버에 남는 건 판정 결과(어떤 표정이었나 · 맞았나)뿐이다. 아이 얼굴이 네트워크를
 *   타지 않아야 한다는 건 타협할 수 없는 선이다.
 *
 * ⚠ 못 켜도 학습은 끝까지 돌아간다
 *   카메라를 거부했거나, 모델을 못 받았거나, 브라우저가 지원하지 않으면 조용히 물러난다.
 *   그러면 story.js 의 기존 흐름([다 했어요!] 를 눌러 넘어가는 방식)이 그대로 살아 있다.
 *   교실 노트북이나 오래된 태블릿에서 카메라가 안 되는 일은 실제로 흔하다.
 *
 * 결과는 window.kdStory.record() 로 넘긴다 — 로컬에 쌓였다가 학습이 끝날 때 한 번에 간다.
 */
(function () {
    'use strict';

    var VISION_CDN = 'https://cdn.jsdelivr.net/npm/@mediapipe/tasks-vision@0.10.14';
    var MODEL_BASE = 'https://storage.googleapis.com/mediapipe-models';

    var stage = (window.kdStory && window.kdStory.stage) || null;
    var isFace = stage === 'EXPRESSION';
    var isAct = stage === 'ACTION';

    /* 몇 프레임 연속으로 맞아야 통과인가.
       한 프레임만 보고 통과시키면 지나가던 표정에 걸려 아이가 "안 했는데 넘어갔다"고 느낀다.
       반대로 너무 길면 6세가 그 표정을 유지하지 못한다. 0.6초 정도가 적당했다. */
    var HOLD_FRAMES = 12;

    /* 동작은 손이 지나가다 잠깐 걸리는 일이 잦다 — 표정보다 오래 유지해야 통과시킨다 */
    var HOLD_FRAMES_POSE = 24;

    /* 아이가 못 맞혀도 학습이 막히면 안 된다. 이 시간이 지나면 코칭을 띄우고
       [다 했어요!] 버튼으로 넘어갈 수 있게 둔다. */
    var GIVE_UP_MS = 25000;

    if (!isFace && !isAct) {
        watchVoice();      /* 이유 찾기의 '말로 알려줄래!' 는 카메라와 무관하다 */
        return;
    }

    var box = document.querySelector('.cam-box');
    var cta = document.querySelector('.cam-actions .kd-cta');
    if (!box || !cta) return;

    var target = readTarget();
    var passed = false;

    /* 온보딩에서 등록한 그 아이의 표정. 못 받아 오면 null 이고, 그때는 범용 기준을 쓴다. */
    var calib = null;

    /* 인식이 실제로 돌고 있는가 — 카메라와 모델이 다 붙어야 true.
       true 일 때만 [다 했어요!] 를 막는다. 카메라가 없는 기기에서까지 막으면 학습이 끊긴다. */
    var recognizing = false;

    /* 못 맞혀도 학습이 막히면 안 된다 — 풀리면 [다 했어요!] 가 예전처럼 넘어간다 */
    var unlocked = false;
    var tries = 0;
    var recognized = null;     /* 마지막으로 알아본 값 — 못 맞히고 넘어갈 때 기록에 쓴다 */
    var hold = 0;
    var video = null;
    var raf = null;

    start();

    /* ------------------------------------------------------------
       카메라 + 모델
       ------------------------------------------------------------ */
    function start() {
        if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
            return;                                  /* 오래된 브라우저 — 기존 흐름 유지 */
        }

        injectStyle();

        /* 모델을 미리 받기 시작한다.
           ⚠ 예전엔 카메라 권한 → 스트림 → 그다음에 모델을 받았다. 모델 번들이 몇 MB라
             그 순서면 아이가 검은 화면을 한참 본다(2026-08-13 "카메라가 너무 늦게 켜짐").
             둘은 서로 기다릴 이유가 없어 같이 시작한다. */
        var modelReady = loadModel();

        video = document.createElement('video');
        video.className = 'kd-cam';
        video.autoplay = true;
        video.playsInline = true;
        video.muted = true;
        box.insertBefore(video, box.firstChild);

        navigator.mediaDevices.getUserMedia({ video: { facingMode: 'user' }, audio: false })
            .then(function (stream) {
                video.srcObject = stream;
                window.addEventListener('pagehide', function () {
                    stream.getTracks().forEach(function (t) { t.stop(); });
                });
                return video.play();
            })
            .then(function () {
                /* 여기서 바로 드러낸다 — 인식이 준비되기 전에도 아이는 자기 얼굴을 본다.
                   판정은 모델이 도착하면 그때부터 돈다. */
                box.classList.add('kd-cam-on');
                return modelReady;
            })
            .then(function (read) {
                if (read) loop(read);
            })
            .catch(function (e) {
                /* 권한 거부가 대부분이다. 아이에게 오류를 띄우지 않는다 — 그냥 예전처럼 진행한다. */
                console.warn('[kkeudeok] 카메라를 켜지 못해 기존 흐름으로 진행합니다', e);
                if (video) video.remove();
            });

        if (isFace) loadCalib();

        setTimeout(function () {
            if (!passed) giveUp();
        }, GIVE_UP_MS);
    }

    /* 등록해 둔 표정을 받아 온다.
       ⚠ 기다리지 않는다. 아직 안 왔으면 그 사이에는 범용 기준으로 재고, 도착하면 그때부터
         그 아이 기준으로 바뀐다. 카메라가 켜지기 전에 끝나는 게 보통이다. */
    function loadCalib() {
        fetch('/api/calib')
            .then(function (r) { return r.ok ? r.json() : null; })
            .then(function (d) {
                if (!d || !d.count || !d.calib) return;

                var out = {};
                Object.keys(d.calib).forEach(function (k) {
                    try {
                        var body = JSON.parse(d.calib[k]);
                        if (body && body.shapes) out[k] = body.shapes;
                    } catch (e) { }
                });

                if (Object.keys(out).length) {
                    calib = out;
                    console.info('[kkeudeok] 등록된 표정 ' + Object.keys(out).length + '벌로 판정합니다');
                }
            })
            .catch(function () { /* 범용 기준으로 계속한다 */ });
    }

    function loadModel() {
        return import(VISION_CDN + '/vision_bundle.mjs').then(function (vision) {
            return vision.FilesetResolver.forVisionTasks(VISION_CDN + '/wasm').then(function (files) {

                if (isFace) {
                    return vision.FaceLandmarker.createFromOptions(files, {
                        baseOptions: {
                            modelAssetPath: MODEL_BASE + '/face_landmarker/face_landmarker/float16/1/face_landmarker.task',
                            delegate: 'GPU'
                        },
                        runningMode: 'VIDEO',
                        numFaces: 1,
                        /* 표정 판정에 쓰는 값이다. 켜지 않으면 랜드마크 좌표만 와서
                           웃는지 찡그리는지 직접 계산해야 한다. */
                        outputFaceBlendshapes: true
                    }).then(function (fl) {
                        return function (t) { return readFace(fl, t); };
                    });
                }

                return vision.PoseLandmarker.createFromOptions(files, {
                    baseOptions: {
                        modelAssetPath: MODEL_BASE + '/pose_landmarker/pose_landmarker_lite/float16/1/pose_landmarker_lite.task',
                        delegate: 'GPU'
                    },
                    runningMode: 'VIDEO',
                    numPoses: 1
                }).then(function (pl) {
                    return function (t) { return readPose(pl, t); };
                });
            });
        }).catch(function (e) {
            /* 모델이 없어도 카메라는 이미 켜져 있다. 인식만 없이 진행한다. */
            console.warn('[kkeudeok] 인식 모델을 받지 못해 기존 흐름으로 진행합니다', e);
            return null;
        });
    }

    /* 매 프레임 판정한다. read() 는 알아본 값(문자열)이나 null 을 준다. */
    function loop(read) {
        recognizing = true;

        function tick() {
            if (passed) return;

            if (video.readyState >= 2) {
                var got = null;
                try {
                    got = read(performance.now());
                } catch (e) {
                    /* 한 프레임 실패로 멈추지 않는다 */
                }

                if (got) recognized = got;

                if (got && got === target) {
                    hold += 1;
                    if (hold >= (isFace ? HOLD_FRAMES : HOLD_FRAMES_POSE)) return pass(got);
                } else {
                    hold = 0;
                }
            }

            raf = requestAnimationFrame(tick);
        }

        tick();
    }

    /* ------------------------------------------------------------
       표정 — 블렌드셰이프 점수로 고른다
       ------------------------------------------------------------ */
    function readFace(landmarker, now) {
        var res = landmarker.detectForVideo(video, now);
        var shapes = res && res.faceBlendshapes && res.faceBlendshapes[0];
        if (!shapes) return null;

        var v = {};
        shapes.categories.forEach(function (c) { v[c.categoryName] = c.score; });

        /* 온보딩에서 등록해 둔 그 아이의 표정이 있으면 그걸 기준으로 재고,
           없으면 범용 기준값으로 떨어진다. */
        return calib ? matchCalib(v) : matchGeneric(v);
    }

    /* ---------- 그 아이 기준으로 판정 ----------
       ⚠ 예전에는 지금 표정과 등록값을 **그대로** 견줬는데, 그러면 아무 표정도 안 지어도
         통과됐다(2026-08-13 지적: "입꼬리를 내리지 않아도 통과"). 얼굴에는 늘 잡히는
         공통 성분이 많아서, 원본끼리 재면 무표정과 슬픔이 둘 다 0.9 를 넘긴다.

       그래서 **무표정을 기준선으로 빼고** 견준다.
         지금 표정 − 무표정  vs  등록한 슬픔 − 무표정
       이러면 '평소 얼굴에서 무엇이 얼마나 달라졌는가' 만 남아 표정끼리 갈린다.

       통과 조건 세 가지를 모두 만족해야 한다.
         1) 표정을 실제로 지었다      — 달라진 정도가 MIN_STRENGTH 이상
         2) 목표 표정과 닮았다        — 코사인 MIN_SIM 이상
         3) 다른 표정보다 뚜렷하게 닮았다 — 2등과 MIN_MARGIN 이상 차이 */

    var MIN_STRENGTH = 0.35;   /* 무표정 대비 얼마나 달라졌나(합) */
    var MIN_SIM      = 0.60;   /* 목표와의 닮은 정도 */
    var MIN_MARGIN   = 0.10;   /* 2등과의 차이 */

    function matchCalib(now) {

        var base = calib.neutral;
        if (!base) {
            /* 무표정을 등록 안 했으면 기준선이 없다 — 예전 방식으로 재되 문턱을 높인다 */
            return matchRaw(now);
        }

        var cur = delta(now, base);

        /* 표정을 아예 안 지었으면 여기서 끝 — 가만히 있는 걸 통과시키면 안 된다 */
        if (strength(cur) < MIN_STRENGTH) return null;

        var best = null, bestSim = -1, secondSim = -1;

        Object.keys(calib).forEach(function (key) {
            if (key === 'neutral') return;

            var sim = cosine(cur, delta(calib[key], base));

            if (sim > bestSim) {
                secondSim = bestSim;
                bestSim = sim;
                best = key;
            } else if (sim > secondSim) {
                secondSim = sim;
            }
        });

        if (bestSim < MIN_SIM) return null;
        if (secondSim >= 0 && bestSim - secondSim < MIN_MARGIN) return null;   /* 어느 것인지 애매하다 */

        return best;
    }

    /* 무표정 등록이 없을 때 — 원본끼리 재되 문턱을 크게 올린다 */
    function matchRaw(now) {
        var best = null, bestSim = 0.97;

        Object.keys(calib).forEach(function (key) {
            var sim = cosine(now, calib[key]);
            if (sim > bestSim) { bestSim = sim; best = key; }
        });

        return best === 'neutral' ? null : best;
    }

    /* 무표정 대비 늘어난 만큼만 남긴다. 줄어든 값(0 미만)은 버린다 —
       '무엇을 더 했나' 가 표정이고, '무엇을 덜 했나' 는 잡음에 가깝다. */
    function delta(v, base) {
        var out = {};
        Object.keys(v).forEach(function (k) {
            var d = (v[k] || 0) - (base[k] || 0);
            if (d > 0) out[k] = d;
        });
        return out;
    }

    function strength(v) {
        var sum = 0;
        Object.keys(v).forEach(function (k) { sum += v[k]; });
        return sum;
    }

    /* 두 표정의 닮은 정도(0~1).
       ⚠ 코사인을 쓰는 이유 — 아이가 카메라에서 멀거나 표정을 작게 지으면 값이 통째로
         작아진다. 거리로 재면 그것만으로 '다른 표정'이 되어 버린다. 방향만 본다. */
    function cosine(a, b) {
        var dot = 0, na = 0, nb = 0;
        var keys = {};
        Object.keys(a).forEach(function (k) { keys[k] = 1; });
        Object.keys(b).forEach(function (k) { keys[k] = 1; });

        Object.keys(keys).forEach(function (k) {
            var x = a[k] || 0, y = b[k] || 0;
            dot += x * y;
            na += x * x;
            nb += y * y;
        });

        return (na && nb) ? dot / Math.sqrt(na * nb) : 0;
    }

    /* ---------- 범용 기준 (등록이 없을 때) ----------
       기준값(0.3)은 아이 얼굴이 작게 잡히는 노트북 캠에서도 반응하도록 낮춰 잡았다. */
    function matchGeneric(v) {

        var smile = avg(v.mouthSmileLeft, v.mouthSmileRight);
        var frown = avg(v.mouthFrownLeft, v.mouthFrownRight);
        var browDown = avg(v.browDownLeft, v.browDownRight);
        var browUp = v.browInnerUp || 0;

        var scores = {
            happy: smile,
            sad: frown * 0.7 + browUp * 0.3,
            angry: browDown
        };

        var best = null;
        var bestScore = 0.3;

        Object.keys(scores).forEach(function (k) {
            if (scores[k] > bestScore) {
                bestScore = scores[k];
                best = k;
            }
        });

        return best;
    }

    /* ------------------------------------------------------------
       동작 — 관절 위치로 고른다
       ------------------------------------------------------------
       comfort   토닥토닥 — 한 손을 어깨 위로 들어 머리 옆에 둔다
       celebrate 축하     — 두 손을 눈높이 위로 든다
       sorry     미안해   — 두 손을 가슴 앞에 모은다

       ⚠ y 는 위로 갈수록 작다(화면 좌표계).
       ⚠ 정밀한 동작 인식이 아니다. 아이가 '팔을 들었다' 정도를 알아보는 수준이고,
         그 이상은 이 나이대 아이에게 요구할 수도 없다. */
    function readPose(landmarker, now) {
        var res = landmarker.detectForVideo(video, now);
        var lm = res && res.landmarks && res.landmarks[0];
        if (!lm) return null;

        var nose = lm[0];
        var lShoulder = lm[11], rShoulder = lm[12];
        var lElbow = lm[13], rElbow = lm[14];
        var lWrist = lm[15], rWrist = lm[16];

        if (!nose || !lShoulder || !rShoulder || !lWrist || !rWrist) return null;

        /* 보이지 않는 관절은 믿지 않는다. 화면 밖에 있는 손을 '내렸다'고 읽으면 안 된다. */
        if (low(lWrist) || low(rWrist) || low(lShoulder) || low(rShoulder)) return null;

        var shoulderY = (lShoulder.y + rShoulder.y) / 2;
        var shoulderW = Math.abs(lShoulder.x - rShoulder.x) || 0.2;
        var headX = nose.x;

        /* ⚠ 예전엔 '한 손이 어깨보다 위' 만으로 토닥토닥을 통과시켰다. 팔을 안 올려도
             책상에 손을 얹고 있으면 걸렸다(2026-08-13 지적). 조건을 셋으로 늘린다.
               · 손목이 어깨보다 **뚜렷이** 위 (어깨너비의 15% 이상)
               · 손이 얼굴 옆에 있다 (친구를 토닥이는 자리)
               · 팔꿈치가 굽어 있다 (쭉 뻗은 만세와 구분) */
        var raise = shoulderW * 0.15;

        var lUp = lWrist.y < shoulderY - raise;
        var rUp = rWrist.y < shoulderY - raise;

        var lNearHead = Math.abs(lWrist.x - headX) < shoulderW * 0.9;
        var rNearHead = Math.abs(rWrist.x - headX) < shoulderW * 0.9;

        /* 두 손을 눈높이 위로 = 축하 */
        if (lWrist.y < nose.y && rWrist.y < nose.y) return 'celebrate';

        /* 두 손을 가슴 앞에 모음 = 미안해 */
        if (Math.abs(lWrist.x - rWrist.x) < shoulderW * 0.45
                && lWrist.y > shoulderY && rWrist.y > shoulderY
                && Math.abs(lWrist.y - rWrist.y) < shoulderW * 0.5) {
            return 'sorry';
        }

        /* 한 손만 얼굴 옆으로 올림 = 토닥토닥 */
        if ((lUp && lNearHead && bent(lShoulder, lElbow, lWrist))
                || (rUp && rNearHead && bent(rShoulder, rElbow, rWrist))) {
            return 'comfort';
        }

        return null;
    }

    /* 관절이 화면에 잡혔는가. MediaPipe 가 주는 visibility 가 낮으면 추측값이다. */
    function low(p) {
        return p.visibility !== undefined && p.visibility < 0.5;
    }

    /* 팔꿈치가 굽었는가 — 어깨~손목 직선거리가 실제 팔 길이보다 뚜렷이 짧으면 굽은 것이다.
       만세(쭉 뻗음)와 토닥토닥(굽힘)을 가른다. */
    function bent(sh, el, wr) {
        if (!el) return true;                       /* 팔꿈치를 못 잡았으면 따지지 않는다 */
        var upper = dist(sh, el), fore = dist(el, wr), direct = dist(sh, wr);
        return direct < (upper + fore) * 0.85;
    }

    function dist(a, b) {
        var dx = a.x - b.x, dy = a.y - b.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /* ------------------------------------------------------------
       통과 / 포기
       ------------------------------------------------------------ */
    function pass(value) {
        passed = true;
        if (raf) cancelAnimationFrame(raf);

        record(value, true);
        box.classList.add('kd-cam-ok');

        say(isFace ? '잘했어! 똑같아' : '잘했어! 그거야');

        /* 아이가 '해냈다' 를 볼 시간을 준다. 바로 넘기면 뭘 했는지 모른 채 화면이 바뀐다. */
        setTimeout(function () { location.href = cta.getAttribute('href'); }, 1200);
    }

    /* 시간이 지나도 못 맞혔다 — 막지 않는다. 코칭을 한 번 주고 넘어갈 수 있게 둔다. */
    function giveUp() {
        if (passed) return;

        var tip = (window.kdStory && window.kdStory.coachText)
            || (isFace ? '괜찮아. 코너에 있는 얼굴을 따라 해 볼까?' : '괜찮아. 팔을 크게 움직여 볼까?');

        say(tip);
        unlock();
    }

    /* 더 막지 않는다. 오래 걸렸거나 여러 번 눌렀으면 넘어갈 수 있게 둔다 —
       못 하는 아이를 화면에 가두는 건 학습이 아니다. 대신 성공으로 세지 않는다. */
    function unlock() {
        unlocked = true;
    }

    /* [다 했어요!] 로 넘어가는 경우 — 인식이 통과시킨 게 아니므로 실패로 남긴다.
       ⚠ 여기를 성공으로 적으면 리포트의 표정·동작 정답률이 통째로 거짓이 된다. */
    /* ⚠ [다 했어요!] 는 예전에 **무조건 넘어가는 버튼**이었다(story.js: 한 번 누르면 코칭,
         두 번째엔 통과). 그래서 표정을 안 지어도 통과됐다(2026-08-13 지적).
         인식이 돌고 있으면 여기서 가로채 막는다.

       ⚠ capture 단계에 건다 — story.js 가 같은 버튼에 걸어 둔 처리보다 먼저 돌아야
         stopImmediatePropagation 으로 그걸 멈출 수 있다.
       ⚠ 인식이 없거나(카메라 거부·모델 실패) 이미 풀렸으면 건드리지 않는다. */
    cta.addEventListener('click', function (e) {

        if (passed || unlocked || !recognizing) {
            if (!passed) record(recognized || '(인식 안 됨)', false);
            return;
        }

        e.preventDefault();
        e.stopImmediatePropagation();

        tries += 1;

        var tip = (window.kdStory && window.kdStory.coachText)
            || (isFace ? '거울을 보고 코너에 있는 얼굴을 따라 해 볼까?' : '코너에 있는 그림처럼 움직여 볼까?');

        say(tries === 1 ? tip : '조금만 더! ' + tip);

        /* 세 번 눌렀으면 그만 막는다 — 다음 클릭부터는 넘어간다 */
        if (tries >= 3) {
            unlock();
            say('괜찮아. 다음으로 가 볼까?');
        }
    }, true);

    function record(value, ok) {
        if (window.kdStory && window.kdStory.record) {
            window.kdStory.record(stage, isFace ? 'EXPRESSION' : 'GESTURE', value, ok);
        }
    }

    /* ------------------------------------------------------------
       음성 — 이유 찾기에서 '말로 알려줄래!'
       ------------------------------------------------------------
       Web Speech API 는 크롬 계열에서만 돈다. 없으면 조용히 물러난다 —
       카드를 눌러 답하는 길이 그대로 있으므로 아이가 막히지 않는다. */
    function watchVoice() {
        if (stage !== 'CAUSE') return;

        var Rec = window.SpeechRecognition || window.webkitSpeechRecognition;
        var mic = document.querySelector('.why-mic');
        var listen = document.querySelector('.why-listen');
        if (!Rec || !mic) return;

        /* ⚠ 정답은 클릭 시점에 다시 찾는다. story-session.js 가 카드를 늦게 갈아끼우면
             여기서 미리 잡아 둔 값이 옛 카드의 것이 된다. */
        function answerText() {
            var el = document.querySelector('.why-card[data-right] .nm');
            return el ? el.textContent.trim() : '';
        }

        /* 몇 번까지 다시 들어 줄까.
           한 번 틀렸다고 버튼을 다시 누르게 하면 아이가 흐름을 놓친다(2026-08-13 지적).
           그렇다고 끝없이 켜 두면 마이크가 계속 열려 있어 부담스럽다. */
        var MAX_TRIES = 3;

        var rec = null;
        var tries = 0;
        var listening = false;

        function stop() {
            listening = false;
            if (rec) {
                try { rec.stop(); } catch (e) { }
            }
            /* 듣는 중 표시를 되돌린다 — story.js 가 이 클릭으로 버튼을 원래대로 바꾼다 */
            if (listen && !listen.hidden) listen.click();
        }

        function start() {
            rec = new Rec();
            rec.lang = 'ko-KR';
            rec.interimResults = false;
            rec.maxAlternatives = 1;

            rec.onresult = function (e) {
                var said = (e.results[0][0].transcript || '').trim();
                if (!said) return;

                var target = answerText();

                /* 아이 발음을 그대로 맞추길 기대할 수 없다. 정답의 낱말이 하나라도
                   들어 있으면 맞은 것으로 본다 — 여기서 엄격하게 굴 이유가 없다. */
                var ok = target.split(/\s+/).some(function (w) {
                    return w.length > 1 && said.indexOf(w) >= 0;
                });

                /* 첫 시도만 기록된다(story-session.js 가 거른다) */
                if (window.kdStory && window.kdStory.record) {
                    window.kdStory.record('CAUSE', 'VOICE', said, ok);
                }

                if (ok) {
                    listening = false;
                    var right = document.querySelector('.why-card[data-right]');
                    if (right) right.click();
                    return;
                }

                tries += 1;

                if (tries >= MAX_TRIES) {
                    say('괜찮아. 그림을 눌러서 알려 줘도 돼');
                    stop();
                    return;
                }

                /* ⚠ "다시 한 번 말해 줄래?" 는 **못 알아들었을 때** 하는 말로 읽힌다.
                     실제로는 잘 들었는데 답이 다른 것이므로, 그렇게 들리는 말로 바꾼다
                     (2026-08-13 지적). 버튼을 다시 누르지 않아도 이어서 듣는다. */
                say('잘 들었어! 그런데 조금 다른 것 같아');
            };

            /* 말이 끊기면 브라우저가 알아서 인식을 끝낸다. 아직 들을 차례면 다시 켠다.
               ⚠ 곧바로 start() 하면 InvalidStateError 가 난다 — 한 박자 쉬고 켠다. */
            rec.onend = function () {
                if (!listening) return;
                setTimeout(function () {
                    if (!listening) return;
                    try { rec.start(); } catch (e) { }
                }, 400);
            };

            rec.onerror = function (e) {
                /* 권한 거부·마이크 없음이면 더 시도해도 소용없다 */
                if (e && (e.error === 'not-allowed' || e.error === 'service-not-allowed')) {
                    say('마이크를 못 써요. 그림을 눌러서 알려 줘도 돼');
                    stop();
                }
            };

            try {
                rec.start();
            } catch (e) { }
        }

        mic.addEventListener('click', function () {
            tries = 0;
            listening = true;
            start();
        });

        /* 아이가 듣는 중 표시를 눌러 끄면 인식도 같이 멈춘다 */
        if (listen) {
            listen.addEventListener('click', function () {
                listening = false;
                if (rec) {
                    try { rec.stop(); } catch (e) { }
                }
            });
        }

        window.addEventListener('pagehide', function () { listening = false; });
    }

    /* ------------------------------------------------------------
       거들기
       ------------------------------------------------------------ */
    function avg(a, b) {
        return ((a || 0) + (b || 0)) / 2;
    }

    /* 글을 못 읽는 아이가 대상이라 문구는 소리로도 준다. story.js 의 낭독과 같은 방식. */
    function say(msg) {
        var toast = document.querySelector('.child-toast');
        if (toast) {
            toast.textContent = msg;
            toast.hidden = false;
            toast.classList.remove('is-on');
            void toast.offsetWidth;
            toast.classList.add('is-on');
            setTimeout(function () { toast.classList.remove('is-on'); }, 2800);
        }

        if (!('speechSynthesis' in window)) return;
        var u = new SpeechSynthesisUtterance(msg);
        u.lang = 'ko-KR';
        u.rate = 0.95;
        u.pitch = 1.4;
        speechSynthesis.speak(u);
    }

    /* 카메라 화면은 이 파일에서만 쓴다. child.css 를 건드리지 않으려고 여기서 넣는다
       (건드리면 head.jsp 의 ?v= 를 전부 올려야 한다). */
    function injectStyle() {
        if (document.getElementById('kdCamStyle')) return;

        var st = document.createElement('style');
        st.id = 'kdCamStyle';
        st.textContent =
            '.cam-box{position:relative;overflow:hidden}' +
            '.kd-cam{position:absolute;inset:0;width:100%;height:100%;object-fit:cover;' +
            /* 거울처럼 좌우를 뒤집는다 — 안 뒤집으면 아이가 든 손과 화면 속 손이 반대라 헷갈린다 */
            'transform:scaleX(-1);border-radius:inherit;opacity:0;transition:opacity .4s}' +
            '.cam-box.kd-cam-on .kd-cam{opacity:1}' +
            '.cam-box.kd-cam-ok{outline:6px solid #34a36a;outline-offset:-6px}' +
            /* 카메라 위로 올리기만 한다.
               ⚠ position 을 건드리면 안 된다 — child.css 가 absolute 로 자리를 잡아 뒀는데
                 relative 로 덮으면 흐름 배치로 바뀌어 오른쪽 위 카드가 엉뚱한 데로 간다. */
            '.cam-box .guide,.cam-box .cam-hint,.cam-box .demo{z-index:1}';

        document.head.appendChild(st);
    }

    /* 정답값(따라 할 표정·동작)은 서버 노드에 들어 있다. 없으면 주소의 ?emo= 로 되돌린다. */
    function readTarget() {
        try {
            var s = JSON.parse(sessionStorage.getItem('kdStorySession'));
            var node = s && s.nodes && s.nodes[stage];
            if (node && node.targetValue) return node.targetValue;
        } catch (e) { }

        var emo = (location.search.match(/[?&]emo=(\w+)/) || [])[1] || 'sad';

        if (isFace) return emo;

        return { sad: 'comfort', angry: 'sorry', happy: 'celebrate' }[emo] || 'comfort';
    }
}());
