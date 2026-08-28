(function () {
    'use strict';

    var VISION_CDN = 'https://cdn.jsdelivr.net/npm/@mediapipe/tasks-vision@0.10.14';
    var MODEL_BASE = 'https://storage.googleapis.com/mediapipe-models';

    var stage = (window.kdStory && window.kdStory.stage) || null;
    var isFace = stage === 'EXPRESSION';
    var isAct = stage === 'ACTION';

    var HOLD_FRAMES = 12;
    var HOLD_FRAMES_POSE = 6;
    var GIVE_UP_MS = 25000;
    var TRY_INTERVAL_MS = 4000;

    if (!isFace && !isAct) {
        watchVoice();
        return;
    }

    var box = document.querySelector('.cam-box');
    var cta = document.querySelector('.cam-actions .kd-cta');
    if (!box || !cta) return;

    var target = readTarget();
    var passed = false;
    var calib = null;
    var recognizing = false;

    var tries = 0;
    var lastTry = 0;
    var sawSubject = false;
    var recognized = null;
    var hold = 0;
    var video = null;
    var raf = null;

    start();

    function start() {
        if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
            return;
        }

        injectStyle();

        var modelReady = loadModel();

        video = document.createElement('video');
        video.className = 'kd-cam';
        video.autoplay = true;
        video.playsInline = true;
        video.muted = true;
        box.insertBefore(video, box.firstChild);

        var wanted = window.kdCam
            || navigator.mediaDevices.getUserMedia({ video: { facingMode: 'user' }, audio: false });

        wanted
            .then(function (stream) {
                if (!stream) throw (window.kdCamFail || new Error('카메라를 열지 못했습니다'));

                video.srcObject = stream;
                window.addEventListener('pagehide', function () {
                    stream.getTracks().forEach(function (t) { t.stop(); });
                });
                return video.play();
            })
            .then(function () {
                box.classList.add('kd-cam-on');
                return modelReady;
            })
            .then(function (read) {
                if (read) loop(read);
            })
            .catch(function (e) {
                console.warn('[kkeudeok] 카메라를 켜지 못해 기존 흐름으로 진행합니다', e);
                if (video) video.remove();
            });

        if (isFace) loadCalib();

        setTimeout(function () {
            if (!passed) giveUp();
        }, GIVE_UP_MS);
    }

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
            .catch(function () { });
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
                        outputFaceBlendshapes: true
                    }).then(function (fl) {
                        return function (t) { return readFace(fl, t); };
                    });
                }

                if (needsHand()) loadHand(vision, files);

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
            console.warn('[kkeudeok] 인식 모델을 받지 못해 기존 흐름으로 진행합니다', e);
            return null;
        });
    }

    function loop(read) {
        recognizing = true;

        function tick() {
            if (passed) return;

            if (video.readyState >= 2) {
                var got = null;
                try {
                    got = read(performance.now());
                } catch (e) { }

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

    var hand = null;
    var handAt = 0;
    var handSeenAt = 0;
    var openAt = 0;
    var sawHand = false;
    var fistAt = 0;

    var HAND_EVERY_MS = 100;
    var OPEN_KEEP_MS = 700;
    var HAND_GONE_MS = 500;

    function needsHand() {
        return target === 'wave' || target === 'celebrate';
    }

    function loadHand(vision, files) {
        vision.GestureRecognizer.createFromOptions(files, {
            baseOptions: {
                modelAssetPath: MODEL_BASE + '/gesture_recognizer/gesture_recognizer/float16/1/gesture_recognizer.task',
                delegate: 'GPU'
            },
            runningMode: 'VIDEO',
            numHands: 2
        }).then(function (gr) {
            hand = gr;
            console.info('[kkeudeok] 손 모양 모델 준비됨 — 편 손이어야 통과합니다(' + target + ')');
        }).catch(function (e) {
            console.warn('[kkeudeok] 손 모양 모델을 받지 못해 손목 움직임만으로 판정합니다', e);
        });
    }

    function watchHand(now) {
        if (!hand || now - handAt < HAND_EVERY_MS) return;
        handAt = now;

        var res;
        try {
            res = hand.recognizeForVideo(video, now);
        } catch (e) {
            return;
        }

        var hands = (res && res.landmarks) || [];
        if (!hands.length) return;

        sawHand = true;
        handSeenAt = now;

        var names = (res.gestures || []).map(function (g) {
            return (g && g[0] && g[0].categoryName) || '';
        });

        for (var i = 0; i < hands.length; i++) {
            if (names[i] === 'Open_Palm' || openHand(hands[i])) {
                openAt = now;
                return;
            }
        }
    }

    function openHand(pts) {
        if (!pts || pts.length < 21) return false;

        var wrist = pts[0];
        var tips = [8, 12, 16, 20];
        var pips = [6, 10, 14, 18];
        var open = 0;

        for (var i = 0; i < tips.length; i++) {
            if (far(wrist, pts[tips[i]]) > far(wrist, pts[pips[i]]) * 1.15) open += 1;
        }

        return open >= 3;
    }

    function far(a, b) {
        var dx = a.x - b.x, dy = a.y - b.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    function palmOpen(now) {
        if (!hand || !sawHand) return true;
        if (now - openAt < OPEN_KEEP_MS) return true;
        if (now - handSeenAt > HAND_GONE_MS) return true;

        if (now - fistAt > 2000) {
            fistAt = now;
            console.info('[kkeudeok] 손을 활짝 펴지 않았다 — 손은 잡히는데 편 손이 아니다');
        }

        return false;
    }

    function readFace(landmarker, now) {
        var res = landmarker.detectForVideo(video, now);
        var shapes = res && res.faceBlendshapes && res.faceBlendshapes[0];
        if (!shapes) return null;

        sawSubject = true;

        var v = {};
        shapes.categories.forEach(function (c) { v[c.categoryName] = c.score; });

        return calib ? matchCalib(v) : matchGeneric(v);
    }

    var MIN_STRENGTH = 0.35;
    var MIN_SIM = 0.60;
    var MIN_MARGIN = 0.10;

    function matchCalib(v) {

        var base = calib.neutral;

        if (!base || !hasAnyEmotion()) {
            return matchRaw(v);
        }

        var cur = delta(v, base);

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
        if (secondSim >= 0 && bestSim - secondSim < MIN_MARGIN) return null;

        return best;
    }

    function hasAnyEmotion() {
        return Object.keys(calib).some(function (k) { return k !== 'neutral'; });
    }

    function matchRaw(v) {
        var best = null, bestSim = 0.97;

        Object.keys(calib).forEach(function (key) {
            var sim = cosine(v, calib[key]);
            if (sim > bestSim) { bestSim = sim; best = key; }
        });

        return best === 'neutral' ? null : best;
    }

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

    function matchGeneric(v) {

        var smile = avg(v.mouthSmileLeft, v.mouthSmileRight);
        var frown = avg(v.mouthFrownLeft, v.mouthFrownRight);
        var browDown = avg(v.browDownLeft, v.browDownRight);
        var browUp = v.browInnerUp || 0;
        var eyeWide = avg(v.eyeWideLeft, v.eyeWideRight);
        var jawOpen = v.jawOpen || 0;
        var browOuterUp = avg(v.browOuterUpLeft, v.browOuterUpRight);

        var scores = {
            happy: smile,
            sad: frown * 0.7 + browUp * 0.3,
            angry: browDown,
            surprise: eyeWide * 0.5 + jawOpen * 0.3 + browOuterUp * 0.2
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

    function readPose(landmarker, now) {

        watchHand(now);

        var res = landmarker.detectForVideo(video, now);
        var lm = res && res.landmarks && res.landmarks[0];
        if (!lm) return null;

        sawSubject = true;

        var nose = lm[0];
        var lShoulder = lm[11], rShoulder = lm[12];
        var lWrist = lm[15], rWrist = lm[16];

        if (!nose || !lShoulder || !rShoulder) return null;
        if (low(lShoulder) || low(rShoulder)) return null;

        var lOk = lWrist && !low(lWrist);
        var rOk = rWrist && !low(rWrist);

        if (!lOk && !rOk) return null;

        var hidden = { x: nose.x, y: 9, visibility: 0 };
        if (!lOk) lWrist = hidden;
        if (!rOk) rWrist = hidden;

        var shoulderY = (lShoulder.y + rShoulder.y) / 2;
        var shoulderW = Math.abs(lShoulder.x - rShoulder.x) || 0.2;

        if (lOk) track('L', lWrist);
        if (rOk) track('R', rWrist);

        var shoulderH = shoulderW * aspect();

        function swung(axis, big) {
            var span = big ? BIG_SPAN : WAVE_MIN_SPAN;
            var turns = big ? BIG_TURNS : WAVE_MIN_TURNS;

            return (lOk && waving('L', shoulderW, axis, span, turns))
                || (rOk && waving('R', shoulderW, axis, span, turns));
        }

        switch (target) {
            case 'wave': {
                var greetLine = shoulderY + shoulderH * 0.35;
                var raised = (lOk && lWrist.y < greetLine) || (rOk && rWrist.y < greetLine);

                if (!raised) return null;

                return (swung('x', true) && palmOpen(now)) ? 'wave' : null;
            }

            case 'celebrate': {
                var upL = lOk && lWrist.y < nose.y;
                var upR = rOk && rWrist.y < nose.y;

                if (!upL && !upR) return null;
                if (!palmOpen(now)) return null;

                if (upL && upR) return 'celebrate';

                var headLine = nose.y - shoulderH * 0.35;
                var high = (upL && lWrist.y < headLine) || (upR && rWrist.y < headLine);

                return (high && swung(null, true)) ? 'celebrate' : null;
            }

            case 'sorry':
                return (handsTogether(lWrist, rWrist, shoulderW)
                        || bowed(nose, shoulderY, shoulderW)) ? 'sorry' : null;
            default: {
                var chestLine = shoulderY + shoulderW * 0.9;

                if (lWrist.y >= chestLine && rWrist.y >= chestLine) return null;

                var patting = (lOk && waving('L', shoulderW, 'y') && nearFriend(lWrist))
                        || (rOk && waving('R', shoulderW, 'y') && nearFriend(rWrist));

                return patting ? 'comfort' : null;
            }
        }
    }

    function handsTogether(a, b, shoulderW) {
        return Math.abs(a.x - b.x) < shoulderW * 0.7
                && Math.abs(a.y - b.y) < shoulderW * 0.8;
    }

    function bowed(nose, shoulderY, shoulderW) {
        return nose.y > shoulderY - shoulderW * 0.35;
    }

    function low(p) {
        return p.visibility !== undefined && p.visibility < 0.5;
    }

    var trail = { L: [], R: [] };
    var WAVE_WINDOW_MS = 1200;

    var WAVE_MIN_SPAN = 0.06;
    var WAVE_MIN_TURNS = 2;

    var BIG_SPAN = 0.35;
    var BIG_TURNS = 3;

    var AXIS_DOMINANCE = 1.2;
    var axisAt = 0;

    function track(side, wrist) {
        var now = performance.now();
        var t = trail[side];

        t.push({ x: wrist.x, y: wrist.y, at: now });

        while (t.length && now - t[0].at > WAVE_WINDOW_MS) {
            t.shift();
        }
    }

    function aspect() {
        return (video && video.videoWidth && video.videoHeight)
                ? video.videoWidth / video.videoHeight : 1;
    }

    function waving(side, shoulderW, axis, minSpan, minTurns) {

        var wantSpan = minSpan === undefined ? WAVE_MIN_SPAN : minSpan;
        var wantTurns = minTurns === undefined ? WAVE_MIN_TURNS : minTurns;

        var t = trail[side];
        if (t.length < 4) return false;

        var minX = t[0].x, maxX = t[0].x;
        var minY = t[0].y, maxY = t[0].y;

        for (var i = 1; i < t.length; i++) {
            if (t[i].x < minX) minX = t[i].x;
            if (t[i].x > maxX) maxX = t[i].x;
            if (t[i].y < minY) minY = t[i].y;
            if (t[i].y > maxY) maxY = t[i].y;
        }

        var ar = aspect();
        var spanY = maxY - minY;
        var wideX = (maxX - minX) * ar;

        var vertical = axis ? axis === 'y' : spanY > wideX;
        var span = vertical ? spanY : wideX;

        if (span <= shoulderW * ar * wantSpan) {
            return false;
        }

        if (axis) {
            var other = vertical ? wideX : spanY;

            if (span < other * AXIS_DOMINANCE) {
                if (performance.now() - axisAt > 2000) {
                    axisAt = performance.now();
                    console.info('[kkeudeok] 흔든 방향이 ' + (axis === 'x' ? '좌우' : '위아래')
                            + ' 가 아니다 — 가로 ' + wideX.toFixed(3)
                            + ' vs 세로 ' + spanY.toFixed(3));
                }
                return false;
            }
        }

        var noise = span * 0.15;
        var turns = 0;
        var dir = 0;

        for (var j = 1; j < t.length; j++) {
            var d = vertical ? t[j].y - t[j - 1].y : (t[j].x - t[j - 1].x) * ar;
            if (Math.abs(d) < noise) continue;

            var way = d > 0 ? 1 : -1;
            if (dir !== 0 && way !== dir) turns += 1;
            dir = way;
        }

        return turns >= wantTurns;
    }

    var ZONE_HALF_W = 0.31;
    var ZONE_TOP = -0.12;
    var ZONE_BOTTOM = 0.62;

    var warnedNoMeasure = false;
    var nearLogged = false;
    var missAt = 0;

    function screenPoint(p) {

        if (!video || !video.videoWidth || !video.videoHeight) {
            return null;
        }

        var b = box.getBoundingClientRect();
        if (!b.width || !b.height) return null;

        var scale = Math.max(b.width / video.videoWidth, b.height / video.videoHeight);
        var shownW = video.videoWidth * scale;
        var shownH = video.videoHeight * scale;

        var x = p.x * shownW - (shownW - b.width) / 2;
        var y = p.y * shownH - (shownH - b.height) / 2;

        return { box: b, x: b.width - x, y: y };
    }

    function nearFriend(wrist) {

        var demo = box.querySelector('.demo');
        var at = demo ? screenPoint(wrist) : null;

        if (!at) {
            if (!warnedNoMeasure) {
                warnedNoMeasure = true;
                console.warn('[kkeudeok] 손 위치를 재지 못해 자리 조건 없이 판정합니다'
                        + ' (그림=' + !!demo + ', 영상크기=' + (video && video.videoWidth) + ')');
            }
            return true;
        }

        var b = at.box;
        var d = demo.getBoundingClientRect();

        var cx = (d.left + d.right) / 2 - b.left;
        var top = d.top - b.top;
        var half = d.height * ZONE_HALF_W;
        var zTop = top + d.height * ZONE_TOP;
        var zBottom = top + d.height * ZONE_BOTTOM;

        var ok = Math.abs(at.x - cx) < half && at.y > zTop && at.y < zBottom;

        var where = '손(' + Math.round(at.x) + ',' + Math.round(at.y) + ')'
                + ' 토닥칸 x ' + Math.round(cx - half) + '~' + Math.round(cx + half)
                + ', y ' + Math.round(zTop) + '~' + Math.round(zBottom);

        if (ok) {
            if (!nearLogged) {
                nearLogged = true;
                console.info('[kkeudeok] 친구를 토닥이는 자리 — ' + where);
            }
        } else if (performance.now() - missAt > 2000) {
            missAt = performance.now();
            console.info('[kkeudeok] 손이 친구에게서 멀다 — ' + where);
        }

        return ok;
    }

    function pass(value) {
        passed = true;
        if (raf) cancelAnimationFrame(raf);

        console.info('[kkeudeok] 통과 — 시킨 것=' + target + ', 알아본 것=' + value);
        box.classList.add('kd-cam-ok');
        say(isFace ? '잘했어! 똑같아' : '잘했어! 그거야');

        try {
            record(value, true);
        } catch (e) {
            console.warn('[kkeudeok] 결과를 남기지 못했습니다 — 학습은 계속합니다', e);
        }

        setTimeout(function () { location.href = cta.getAttribute('href'); }, 1800);
    }

    function giveUp() {
        if (passed) return;
        say(coachTip());
    }

    function coachTip() {

        if (!sawSubject) {
            return isFace ? '얼굴이 잘 안 보여. 화면 가운데로 와 볼까?'
                          : '몸이 잘 안 보여. 조금 뒤로 가 볼까?';
        }
        return (window.kdStory && window.kdStory.coachText)
            || (isFace ? '거울을 보고 코너에 있는 얼굴을 따라 해 볼까?'
                       : '옆에 적힌 대로 손을 흔들어 볼까?');
    }

    cta.addEventListener('click', function (e) {
        if (passed || !recognizing) {
            if (!passed) record(recognized || '(인식 안 됨)', false);
            return;
        }

        e.preventDefault();
        e.stopImmediatePropagation();

        var now = Date.now();
        if (lastTry && now - lastTry < TRY_INTERVAL_MS) return;

        lastTry = now;
        tries += 1;

        say(tries === 1 ? coachTip() : '조금만 더! ' + coachTip());
    }, true);

    function record(value, ok) {
        if (window.kdStory && window.kdStory.record) {
            window.kdStory.record(stage, isFace ? 'EXPRESSION' : 'GESTURE', value, ok);
        }
    }

    function watchVoice() {
        if (stage !== 'CAUSE') return;

        var Rec = window.SpeechRecognition || window.webkitSpeechRecognition;
        var mic = document.querySelector('.why-mic');
        var listen = document.querySelector('.why-listen');
        if (!Rec || !mic) return;

        function answerText() {
            var el = document.querySelector('.why-card[data-right] .nm');
            return el ? el.textContent.trim() : '';
        }

        var MAX_TRIES = 3;

        var rec = null;
        var tries = 0;
        var listening = false;

        function stop() {
            listening = false;
            if (rec) {
                try { rec.stop(); } catch (e) { }
            }
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

                var answer = answerText();

                var ok = answer.split(/\s+/).some(function (w) {
                    return w.length > 1 && said.indexOf(w) >= 0;
                });

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

                say('잘 들었어! 그런데 조금 다른 것 같아');
            };

            rec.onend = function () {
                if (!listening) return;
                setTimeout(function () {
                    if (!listening) return;
                    try { rec.start(); } catch (e) { }
                }, 400);
            };

            rec.onerror = function (e) {
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

    function avg(a, b) {
        return ((a || 0) + (b || 0)) / 2;
    }

    function say(msg) {
        var toast = document.querySelector('.child-toast');

        if (!toast) {
            console.warn('[kkeudeok] 띄울 자리(.child-toast)가 없어 그냥 넘어갑니다 — ' + msg);
            return;
        }

        toast.textContent = msg;
        toast.hidden = false;
        toast.classList.remove('is-on');
        void toast.offsetWidth;
        toast.classList.add('is-on');
        setTimeout(function () { toast.classList.remove('is-on'); }, 2800);
    }

    function injectStyle() {
        if (document.getElementById('kdCamStyle')) return;

        var st = document.createElement('style');
        st.id = 'kdCamStyle';
        st.textContent =
            '.cam-box{position:relative;overflow:hidden}' +
            '.kd-cam{position:absolute;inset:0;width:100%;height:100%;object-fit:cover;' +
            'transform:scaleX(-1);border-radius:inherit;opacity:0;transition:opacity .4s}' +
            '.cam-box.kd-cam-on .kd-cam{opacity:1}' +
            '.cam-box.kd-cam-ok{outline:6px solid #34a36a;outline-offset:-6px}' +
            '.cam-box .cam-hint,.cam-box .demo{z-index:1}';

        document.head.appendChild(st);
    }

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
