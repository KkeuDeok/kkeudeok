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

        if (key.isEmpty()) {
            log.warn("""
                    GEMINI_API_KEY 가 없어 AI 스토리 생성을 끕니다.
                    학습은 내장 시나리오(FALLBACK)로 정상 동작합니다.
                    켜려면 환경변수를 넣고 다시 띄우세요:  $env:GEMINI_API_KEY = "키" """);
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

    public boolean isEnabled() {
        return client != null;
    }

    /**
     * @param systemPrompt 역할·말투·금지사항 등 매번 같은 지시
     * @param userPrompt   이번 요청의 입력값
     * @return 모델이 준 JSON 문자열. 키가 없거나, 시간을 넘겼거나, 오류가 나면 {@link Optional#empty()}
     */
    public Optional<String> generateJson(String systemPrompt, String userPrompt) {
        return generateJson(systemPrompt, userPrompt, props.getTimeoutSeconds());
    }

    public Optional<String> generateJson(String systemPrompt, String userPrompt, int timeoutSeconds) {

        if (!isEnabled()) {
            return Optional.empty();
        }

        String prompt = systemPrompt + "\n\n---\n\n" + userPrompt;

        GenerateContentConfig config = GenerateContentConfig.builder()
                .temperature(props.getTemperature())
                .responseMimeType("application/json")
                .build();

        long startedAt = System.currentTimeMillis();
        Future<String> task = pool.submit(() -> {
            GenerateContentResponse res = client.models.generateContent(props.getModel(), prompt, config);
            return res.text();
        });

        try {
            String text = task.get(timeoutSeconds, TimeUnit.SECONDS);
            log.debug("Gemini 응답 {}ms", System.currentTimeMillis() - startedAt);
            return Optional.ofNullable(text).map(String::trim).filter(s -> !s.isEmpty());

        } catch (TimeoutException e) {
            task.cancel(true);
            log.warn("Gemini 응답이 {}초를 넘겨 끊었습니다 — 내장 시나리오로 진행합니다", timeoutSeconds);

        } catch (InterruptedException e) {
            task.cancel(true);
            Thread.currentThread().interrupt();
            log.warn("Gemini 호출이 중단됐습니다 — 내장 시나리오로 진행합니다");

        } catch (Exception e) {
            log.warn("Gemini 호출 실패 — 내장 시나리오로 진행합니다: {}", e.getMessage());
        }

        return Optional.empty();
    }
}
