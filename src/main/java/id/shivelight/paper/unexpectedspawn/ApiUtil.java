package id.shivelight.paper.unexpectedspawn;

import java.util.HashMap;

public class ApiUtil {

    private static final HashMap<String, Boolean> availableApiCache = new HashMap<>();

    public static boolean isAvailable(Class<?> klass, String methodName) {
        String method = klass.getName() + "#" + methodName;
        if (availableApiCache.containsKey(method)) {
            return availableApiCache.get(method);
        }

        try {
            klass.getMethod(methodName);
            availableApiCache.put(method, true);
        } catch (NoSuchMethodException e) {
            availableApiCache.put(method, false);
        }

        return availableApiCache.get(method);
    }

    public static void clearCache() {
        availableApiCache.clear();
    }

}
