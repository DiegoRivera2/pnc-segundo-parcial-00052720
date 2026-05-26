package com.uca.pncsegundoparcialbiblioteca.dto;

import com.uca.pncsegundoparcialbiblioteca.entity.Book;
import com.uca.pncsegundoparcialbiblioteca.entity.Genre;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookResponse {

    private Long id;
    private String title;
    private String author;
    private String isbn;
    private Genre genre;
    private Integer totalCopies;
    private Integer availableCopies;
    private Boolean available;
    private LocalDate publishedDate;
    private String description;

    public static BookResponse fromEntity(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .genre(book.getGenre())
                .totalCopies(book.getTotalCopies())
                .availableCopies(book.getAvailableCopies())
                .available(book.getAvailable())
                .publishedDate(book.getPublishedDate())
                .description(book.getDescription())
                .build();
    }
}
