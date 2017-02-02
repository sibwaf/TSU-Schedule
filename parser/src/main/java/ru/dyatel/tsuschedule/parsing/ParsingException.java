package ru.dyatel.tsuschedule.parsing;

public class ParsingException extends RuntimeException {

	public ParsingException() {
	}

	public ParsingException(String message) {
		super(message);
	}

	public ParsingException(String message, Throwable cause) {
		super(message, cause);
	}

	public ParsingException(Throwable cause) {
		super(cause);
	}

}
