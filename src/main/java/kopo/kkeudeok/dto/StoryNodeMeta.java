package kopo.kkeudeok.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public final class StoryNodeMeta {

    private StoryNodeMeta() {
    }

    public static void pack(StoryNodeDTO node, ObjectMapper mapper) {

        Map<String, Object> meta = new LinkedHashMap<>();

        putIfSet(meta, "title", node.getTitle());
        putIfSet(meta, "mission", node.getMissionType());
        putIfSet(meta, "target", node.getTargetValue());
        putIfSet(meta, "coach", node.getCoachText());
        putIfSet(meta, "hint", node.getHintText());
        putIfSet(meta, "pose", node.getCharPose());

        if (node.getOptions() != null && !node.getOptions().isEmpty()) {
            meta.put("options", node.getOptions());
        }
        if (node.getScenario() != null) {
            meta.put("scenario", node.getScenario());
        }

        if (meta.isEmpty()) {
            node.setChoiceData(null);
            return;
        }

        try {
            node.setChoiceData(mapper.writeValueAsString(meta));
        } catch (Exception e) {
            log.warn("노드 부가정보를 JSON 으로 바꾸지 못했습니다: {}", e.getMessage());
            node.setChoiceData(null);
        }
    }

    public static void unpack(StoryNodeDTO node, ObjectMapper mapper) {

        String json = node.getChoiceData();

        if (json == null || json.isBlank()) {
            return;
        }

        try {
            Meta meta = mapper.readValue(json, Meta.class);

            node.setTitle(meta.title);
            node.setMissionType(meta.mission);
            node.setTargetValue(meta.target);
            node.setCoachText(meta.coach);
            node.setHintText(meta.hint);
            node.setCharPose(meta.pose);
            node.setScenario(meta.scenario);
            node.setOptions(meta.options == null ? new ArrayList<>() : new ArrayList<>(meta.options));

        } catch (Exception e) {
            log.warn("노드 {} 의 choice_data 를 읽지 못했습니다: {}", node.getNodeId(), e.getMessage());
        }
    }

    private static void putIfSet(Map<String, Object> map, String key, String value) {
        if (value != null && !value.isBlank()) {
            map.put(key, value);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Meta {
        public String title;
        public String mission;
        public String target;
        public String coach;
        public String hint;
        public String pose;
        public List<StoryOptionDTO> options;
        public StoryScenarioDTO scenario;
    }
}
