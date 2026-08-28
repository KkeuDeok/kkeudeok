package kopo.kkeudeok.util;

public final class CmmUtil {

    private CmmUtil() {
    }

    public static String nvl(String str, String chgStr) {
        return (str == null || str.isEmpty()) ? chgStr : str;
    }

    public static String nvl(String str) {
        return nvl(str, "");
    }
}
