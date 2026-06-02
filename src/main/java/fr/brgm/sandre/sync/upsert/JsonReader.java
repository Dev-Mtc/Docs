package fr.brgm.sandre.sync.upsert;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Helpers de lecture des nœuds JSON SANDRE.
 *
 * <p>L'API SANDRE renvoie tantôt une date courte ({@code yyyy-MM-dd}), tantôt une date+heure
 * ({@code yyyy-MM-dd'T'HH:mm:ss}). On tolère les deux formats.</p>
 */
final class JsonReader {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private JsonReader() {}

    static String text(JsonNode node, String field) {
        if (node == null) return null;
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) return null;
        String s = v.asText(null);
        return (s == null || s.isBlank()) ? null : s;
    }

    static LocalDateTime dateTime(JsonNode node, String field) {
        String s = text(node, field);
        if (s == null) return null;
        try {
            return LocalDateTime.parse(s, DATE_TIME);
        } catch (DateTimeParseException ignored) {
            try {
                return DATE.parse(s, java.time.LocalDate::from).atStartOfDay();
            } catch (DateTimeParseException e) {
                return null;
            }
        }
    }
}
