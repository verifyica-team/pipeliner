package org.verifyica.pipeline.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Timestamp {

    private Timestamp() {
        // INTENTIONALLY BLANK
    }

    public static String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME).replace('T', ' ').substring(0, 23);
    }
}
