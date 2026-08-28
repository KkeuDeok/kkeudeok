package kopo.kkeudeok.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "kkeudeok.gemini")
public class GeminiProperties {

    private String apiKey = "";

    private String model = "gemini-3.5-flash-lite";

    private String roadmapModel = "gemini-3.6-flash";

    private float temperature = 0.9f;

    private int timeoutSeconds = 12;

    private int roadmapTimeoutSeconds = 60;

    private boolean polishNodes = false;
}
