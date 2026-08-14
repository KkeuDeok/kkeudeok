package kopo.kkeudeok.util;

import jakarta.servlet.http.HttpSession;

public final class SessionKeys {

    private SessionKeys() {
    }

    public static final String MEMBER_ID = "SS_MEMBER_ID";

    public static final String CHILD_ID = "SS_CHILD_ID";

    public static Long longOf(HttpSession session, String key) {

        if (session == null) {
            return null;
        }

        Object v = session.getAttribute(key);

        if (v instanceof Long l) {
            return l;
        }
        if (v instanceof Number n) {
            return n.longValue();
        }
        if (v instanceof String s && !s.isBlank()) {
            try {
                return Long.valueOf(s.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        return null;
    }

    public static Long childId(HttpSession session, Long fromRequest) {
        Long fromSession = longOf(session, CHILD_ID);
        return fromSession != null ? fromSession : fromRequest;
    }
}
