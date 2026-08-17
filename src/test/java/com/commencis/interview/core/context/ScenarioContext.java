package com.commencis.interview.core.context;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Bir senaryo boyunca tasinan degerler. PicoContainer her senaryo icin yeni ornek uretir,
 * bu yuzden static alan tutulmaz ve senaryolar birbirinin verisini gormez.
 *
 * <p>Tipik kullanim: bir istegin yanitindan alinan id saklanir, sonraki adimda
 * {@code ${ctx:postId}} olarak okunur.
 */
public class ScenarioContext {

    private final Map<String, Object> values = new LinkedHashMap<>();

    public void put(String key, Object value) {
        values.put(key, value);
    }

    public boolean has(String key) {
        return values.containsKey(key);
    }

    public Object get(String key) {
        if (!values.containsKey(key)) {
            throw new IllegalStateException("Context'te '" + key + "' yok. Once bir adimda kaydedin. "
                    + "Mevcut anahtarlar: " + values.keySet());
        }
        return values.get(key);
    }

    public String getString(String key) {
        return String.valueOf(get(key));
    }

    public Map<String, Object> all() {
        return Map.copyOf(values);
    }
}
