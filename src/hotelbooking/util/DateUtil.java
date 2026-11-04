package hotelbooking.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Locale;

/** Parses common Vietnamese date input and displays dates consistently as dd/MM/yyyy. */
public final class DateUtil {
    private static final DateTimeFormatter DISPLAY =
            DateTimeFormatter.ofPattern("dd/MM/uuuu", Locale.ROOT);
    private static final List<DateTimeFormatter> INPUT_FORMATS = List.of(
            DateTimeFormatter.ofPattern("d/M/uuuu", Locale.ROOT).withResolverStyle(ResolverStyle.STRICT),
            DateTimeFormatter.ofPattern("d/M/uu", Locale.ROOT).withResolverStyle(ResolverStyle.STRICT),
            DateTimeFormatter.ofPattern("d-M-uuuu", Locale.ROOT).withResolverStyle(ResolverStyle.STRICT),
            DateTimeFormatter.ofPattern("d-M-uu", Locale.ROOT).withResolverStyle(ResolverStyle.STRICT),
            DateTimeFormatter.ISO_LOCAL_DATE
    );

    private DateUtil() {}

    public static LocalDate parse(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập ngày theo dạng dd/MM/yyyy.");
        }
        String value = input.trim();
        for (DateTimeFormatter formatter : INPUT_FORMATS) {
            try {
                return LocalDate.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
                // Try the next supported input format.
            }
        }
        throw new IllegalArgumentException("Ngày không hợp lệ. Hãy nhập dd/MM/yyyy, ví dụ 09/01/2026.");
    }

    public static String format(LocalDate date) {
        return date == null ? "" : DISPLAY.format(date);
    }

    public static String normalize(String input) {
        return format(parse(input));
    }
}
