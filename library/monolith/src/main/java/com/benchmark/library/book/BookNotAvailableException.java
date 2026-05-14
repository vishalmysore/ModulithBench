package com.benchmark.library.book;

public class BookNotAvailableException extends RuntimeException {
    public BookNotAvailableException(Long bookId) {
        super("Book with id " + bookId + " has no available copies");
    }
}
