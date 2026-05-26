package com.uca.pncsegundoparcialbiblioteca.repository;

import com.uca.pncsegundoparcialbiblioteca.entity.Book;
import com.uca.pncsegundoparcialbiblioteca.entity.Genre;
import org.springframework.data.jpa.domain.Specification;

public final class BookSpecification {

    private BookSpecification() {
    }

    public static Specification<Book> withFilters(Genre genre, Boolean available) {
        return Specification.allOf(
                genre != null ? hasGenre(genre) : null,
                available != null ? isAvailable(available) : null
        );
    }

    private static Specification<Book> hasGenre(Genre genre) {
        return (root, query, cb) -> cb.equal(root.get("genre"), genre);
    }

    private static Specification<Book> isAvailable(Boolean available) {
        return (root, query, cb) -> cb.equal(root.get("available"), available);
    }
}
