package ru.dyatel.tsuschedule.parsing;

public class BadGroupException extends IllegalArgumentException {

	public BadGroupException() {
	}

	public BadGroupException(String message) {
		super(message);
	}

	public BadGroupException(String message, Throwable cause) {
		super(message, cause);
	}

	public BadGroupException(Throwable cause) {
		super(cause);
	}

}
