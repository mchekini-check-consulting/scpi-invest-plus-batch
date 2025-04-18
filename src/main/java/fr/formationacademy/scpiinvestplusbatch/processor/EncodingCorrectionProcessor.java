package fr.formationacademy.scpiinvestplusbatch.processor;

import lombok.NonNull;
import org.springframework.batch.item.ItemProcessor;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class EncodingCorrectionProcessor<T> implements ItemProcessor<T, T> {

    private final Map<String, String> replacementMap;

    public EncodingCorrectionProcessor() {
        replacementMap = new HashMap<>();
        replacementMap.put("advertising", "é");
    }

    @Override
    public T process(@NonNull T item) throws Exception {
        for (Field field : item.getClass().getDeclaredFields()) {
            if (field.getType() == String.class) {
                field.setAccessible(true);
                String value = (String) field.get(item);
                if (value != null && value.contains("�")) {
                    String replacement = replacementMap.getOrDefault(field.getName(), " ");
                    field.set(item, value.replace("�", replacement));
                }
            }
        }
        return item;
    }
}
