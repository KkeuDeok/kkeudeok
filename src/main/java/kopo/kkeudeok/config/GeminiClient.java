package kopo.kkeudeok.config;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

// Gemini 호출
@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiClient {

    private final GeminiProperties props;

    private Client client;

    private ExecutorService pool;

    @PostConstruct
    void init() {
        String key = props.getApiKey() == null ? "" : props.getApiKey().trim();

        if (key.startsWith("${") && key.endsWith("}")) {
            log.error("""
                    AI 키 자리에 환경변수가 풀리지 않은 채로 들어왔습니다: {}
                    환경변수를 설정하지 않은 것입니다. AI 를 끈 상태로 띄웁니다.
                    PowerShell:  $env:AI_API = "키"     (설정 뒤 다시 띄울 것)""", key);
            return;
        }

        if (key.isEmpty()) {
            log.warn("""
                    AI 키가 없어 스토리·로드맵 생성을 끕니다.
                    만들지 못하므로 [학습 시작하기] 가 잠깁니다.
                    켜려면 환경변수를 넣고 다시 띄우세요:  $env:AI_API = "키" """);
            return;
        }

        this.client = Client.builder().apiKey(key).build();
        this.pool = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "gemini-call");
            t.setDaemon(true);
            return t;
        });

        log.info("Gemini 준비 완료 — model={}, timeout={}s", props.getModel(), props.getTimeoutSeconds());
    }

    @PreDestroy
    void shutdown() {
        if (pool != null) {
            pool.shutdownNow();
        }
    }

    private static final int MAX_ATTEMPTS = 2;

    private static boolean quotaExceeded(String message) {
        return message != null
                && (message.contains("429") || message.contains("RESOURCE_EXHAUSTED"));
    }

    private static boolean modelNotFound(String message) {
        return message != null
                && (message.contains("404") || message.contains("NOT_FOUND")
                    || message.contains("no longer available"));
    }

    private volatile boolean modelBroken = false;

    private static final long MAX_WAIT_MS = 25_000;

    private static long retryAfterMs(String message) {

        if (message == null) {
            return 0;
        }

        boolean retryable = message.contains("429") || message.contains("RESOURCE_EXHAUSTED")
                || message.contains("503") || message.contains("UNAVAILABLE");

        if (!retryable) {
            return 0;
        }

        Matcher m = RETRY_IN.matcher(message);

        long ms = m.find() ? (long) (Double.parseDouble(m.group(1)) * 1000) + 500 : 3_000;

        return Math.min(ms, MAX_WAIT_MS);
    }

    private static final Pattern RETRY_IN = Pattern.compile("retry in ([0-9]+(?:\\.[0-9]+)?)s");

    private volatile long busyUntil = 0L;

    public boolean isReady() {
        return isEnabled() && !modelBroken && System.currentTimeMillis() >= busyUntil;
    }

    public long cooldownSeconds() {
        long left = busyUntil - System.currentTimeMillis();
        return left <= 0 ? 0 : (left + 999) / 1000;
    }

    private void restAfter(long waitMs) {
        long until = System.currentTimeMillis() + waitMs;
        if (until > busyUntil) {
            busyUntil = until;
        }
    }

    public boolean isEnabled() {
        return client != null;
    }

    public Optional<String> generateJson(String systemPrompt, String userPrompt) {
        return generateJson(systemPrompt, userPrompt, props.getTimeoutSeconds());
    }

    public Optional<String> generateJson(String systemPrompt, String userPrompt, int timeoutSeconds) {
        return generateJson(systemPrompt, userPrompt, timeoutSeconds, props.getModel());
    }

    public Optional<String> generateJson(String systemPrompt, String userPrompt,
                                         int timeoutSeconds, String modelName) {

        if (!isEnabled()) {
            return Optional.empty();
        }

        if (!isReady()) {
            log.info("AI 한도가 아직 안 풀려 이번에는 부르지 않습니다 — {}초 남음", cooldownSeconds());
            return Optional.empty();
        }

        String prompt = systemPrompt + "\n\n---\n\n" + userPrompt;

        GenerateContentConfig config = GenerateContentConfig.builder()
                .temperature(props.getTemperature())
                .responseMimeType("application/json")
                .build();

        long startedAt = System.currentTimeMillis();

        for (int attempt = 1; ; attempt++) {

            Future<String> task = pool.submit(() -> {
                GenerateContentResponse res = client.models.generateContent(modelName, prompt, config);
                return res.text();
            });

            try {
                String text = task.get(timeoutSeconds, TimeUnit.SECONDS);
                log.debug("Gemini 응답 {}ms", System.currentTimeMillis() - startedAt);
                return Optional.ofNullable(text).map(String::trim).filter(s -> !s.isEmpty());

            } catch (TimeoutException e) {
                task.cancel(true);
                log.warn("Gemini 응답이 {}초를 넘겨 끊었습니다 — 이번에는 만들지 못합니다", timeoutSeconds);
                break;

            } catch (InterruptedException e) {
                task.cancel(true);
                Thread.currentThread().interrupt();
                log.warn("Gemini 호출이 중단됐습니다");
                break;

            } catch (Exception e) {

                String msg = e.getMessage();

                if (modelNotFound(msg)) {
                    modelBroken = true;
                    log.error("쓸 수 없는 AI 모델입니다 — '{}'. application.properties 의"
                            + " kkeudeok.gemini.model (또는 환경변수 AI_MODEL) 을 고치고"
                            + " 앱을 다시 띄워 주세요. 서버가 알려 준 내용: {}",
                            modelName, msg);
                    break;
                }

                long waitMs = retryAfterMs(msg);

                if (waitMs <= 0) {
                    log.warn("Gemini 호출 실패 — 이번에는 만들지 못합니다: {}", msg);
                    break;
                }

                restAfter(waitMs);

                if (quotaExceeded(msg)) {
                    log.info("AI 한도에 걸렸습니다 — {}초 뒤부터 다시 부릅니다", cooldownSeconds());
                    break;
                }

                if (attempt >= MAX_ATTEMPTS) {
                    log.warn("Gemini 가 계속 바빠 이번에는 만들지 못합니다");
                    break;
                }

                log.info("Gemini 가 바쁩니다 — {}초 뒤 다시 부릅니다 ({}/{})",
                        waitMs / 1000, attempt, MAX_ATTEMPTS);

                try {
                    Thread.sleep(waitMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        return Optional.empty();
    }
}
