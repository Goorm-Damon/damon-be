package damon.backend.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Log {

    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final ObjectWriter prettyPrinter = objectMapper.writerWithDefaultPrettyPrinter();

    public static <T> void trace(T object) {
        try {
            String jsonString = prettyPrinter.writeValueAsString(object);
            log.trace("{} as json\n{}", object.getClass().getSimpleName(), jsonString);
        } catch (JsonProcessingException e) {
            log.error("Error converting object to json", e);
        }
    }

    public static <T> void debug(T object) {
        try {
            String jsonString = prettyPrinter.writeValueAsString(object);
            log.debug("{} as json\n{}", object.getClass().getSimpleName(), jsonString);
        } catch (JsonProcessingException e) {
            log.error("Error converting object to json", e);
        }
    }

    public static <T> void info(T object) {
        try {
            String jsonString = prettyPrinter.writeValueAsString(object);
            log.info("{} as json\n{}", object.getClass().getSimpleName(), jsonString);
        } catch (JsonProcessingException e) {
            log.error("Error converting object to json", e);
        }
    }

    public static <T> void warn(T object) {
        try {
            String jsonString = prettyPrinter.writeValueAsString(object);
            log.warn("{} as json\n{}", object.getClass().getSimpleName(), jsonString);
        } catch (JsonProcessingException e) {
            log.error("Error converting object to json", e);
        }
    }

    public static <T> void error(T object) {
        try {
            String jsonString = prettyPrinter.writeValueAsString(object);
            log.error("{} as json\n{}", object.getClass().getSimpleName(), jsonString);
        } catch (JsonProcessingException e) {
            log.error("Error converting object to json", e);
        }
    }
}