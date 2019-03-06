package chain.fxgj.core.jpa.model;

import java.lang.reflect.Field;

public class ReflectEntity {

    public static String toStr(Object o) {

        try {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            Class cls = o.getClass();
            Field[] fields = cls.getDeclaredFields();
            for (Field f : fields) {
                f.setAccessible(true);
                sb.append("\"").append(f.getName()).append("\":\"").append(f.get(o)).append("\",");
            }

            return String.format("%s}", sb.substring(0, sb.length() - 1));
        } catch (Exception e) {
            return null;
        }

    }
}
