// CopyUtils.java
import sun.misc.Unsafe;
import java.lang.reflect.*;
import java.util.*;

public class CopyUtils {

    private static final Unsafe unsafe;

    static {
        try {
            // Obtain the Unsafe instance
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);
        } catch (Exception e) {
            throw new RuntimeException("Could not obtain Unsafe instance", e);
        }
    }

    public static Object deepCopy(Object obj) throws Exception {
        Map<Object, Object> visited = new IdentityHashMap<>();
        return deepCopy(obj, visited);
    }

    private static Object deepCopy(Object obj, Map<Object, Object> visited) throws Exception {
        if (obj == null) {
            return null;
        }

        // Check if we have already copied this object (handle cyclic references)
        if (visited.containsKey(obj)) {
            return visited.get(obj);
        }

        Class<?> clazz = obj.getClass();

        // Handle immutable objects (primitives, wrappers, Strings, etc.)
        if (clazz.isPrimitive() || clazz == String.class || Number.class.isAssignableFrom(clazz)
                || clazz == Boolean.class || clazz == Character.class || clazz.isEnum()
                || clazz == Class.class) {
            return obj;
        }

        // Handle arrays
        if (clazz.isArray()) {
            int length = Array.getLength(obj);
            Class<?> componentType = clazz.getComponentType();
            Object newArray = Array.newInstance(componentType, length);
            visited.put(obj, newArray);
            for (int i = 0; i < length; i++) {
                Object element = Array.get(obj, i);
                Object copiedElement = deepCopy(element, visited);
                Array.set(newArray, i, copiedElement);
            }
            return newArray;
        }

        // Handle collections
        if (Collection.class.isAssignableFrom(clazz)) {
            Collection<?> collection = (Collection<?>) obj;
            Collection<Object> newCollection = createCollectionInstance(clazz);
            visited.put(obj, newCollection);
            for (Object item : collection) {
                Object copiedItem = deepCopy(item, visited);
                newCollection.add(copiedItem);
            }
            return newCollection;
        }

        // Handle maps
        if (Map.class.isAssignableFrom(clazz)) {
            Map<?, ?> map = (Map<?, ?>) obj;
            Map<Object, Object> newMap = createMapInstance(clazz);
            visited.put(obj, newMap);
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Object copiedKey = deepCopy(entry.getKey(), visited);
                Object copiedValue = deepCopy(entry.getValue(), visited);
                newMap.put(copiedKey, copiedValue);
            }
            return newMap;
        }

        // For other objects, create a new instance without invoking any constructor
        Object newObj = unsafe.allocateInstance(clazz);
        visited.put(obj, newObj);

        // Copy all fields (including private and inherited fields)
        for (Field field : getAllFields(clazz)) {
            field.setAccessible(true);
            Object fieldValue = field.get(obj);
            Object copiedValue = deepCopy(fieldValue, visited);
            field.set(newObj, copiedValue);
        }

        return newObj;
    }

    // Helper method to create a new collection instance
    private static Collection<Object> createCollectionInstance(Class<?> clazz) throws Exception {
        if (!clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())) {
            return (Collection<Object>) clazz.getDeclaredConstructor().newInstance();
        } else if (List.class.isAssignableFrom(clazz)) {
            return new ArrayList<>();
        } else if (Set.class.isAssignableFrom(clazz)) {
            return new HashSet<>();
        } else if (Queue.class.isAssignableFrom(clazz)) {
            return new LinkedList<>();
        } else {
            return new ArrayList<>();
        }
    }

    // Helper method to create a new map instance
    private static Map<Object, Object> createMapInstance(Class<?> clazz) throws Exception {
        if (!clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())) {
            return (Map<Object, Object>) clazz.getDeclaredConstructor().newInstance();
        } else if (SortedMap.class.isAssignableFrom(clazz)) {
            return new TreeMap<>();
        } else {
            return new HashMap<>();
        }
    }

    // Helper method to get all fields from a class hierarchy
    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            Field[] declaredFields = clazz.getDeclaredFields();
            fields.addAll(Arrays.asList(declaredFields));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }
}
