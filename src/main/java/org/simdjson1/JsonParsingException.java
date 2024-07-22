package org.simdjson1;

public class JsonParsingException extends RuntimeException {

    JsonParsingException(String message) {
        super(message);
    }

    JsonParsingException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
