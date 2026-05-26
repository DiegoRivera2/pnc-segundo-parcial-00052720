package com.uca.pncsegundoparcialbiblioteca.service;

import com.uca.pncsegundoparcialbiblioteca.dto.BookRequest;
import com.uca.pncsegundoparcialbiblioteca.dto.BookResponse;
import com.uca.pncsegundoparcialbiblioteca.entity.Book;
import com.uca.pncsegundoparcialbiblioteca.entity.Genre;
import com.uca.pncsegundoparcialbiblioteca.exception.BusinessRuleException;
import com.uca.pncsegundoparcialbiblioteca.exception.ResourceNotFoundException;
import com.uca.pncsegundoparcialbiblioteca.repository.BookRepository;
import com.uca.pncsegundoparcialbiblioteca.repository.BookSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private static final String ISBN_PATTERN = "^(?:97[89])?\\d{9}[\\dX]$";

    private final BookRepository bookRepository;

    @Transactional
    public BookResponse create(BookRequest request) {
        validateRequest(request);
        String normalizedIsbn = normalizeIsbn(request.getIsbn());

        if (bookRepository.existsByTitleIgnoreCase(request.getTitle().trim())) {
            throw new BusinessRuleException("Ya existe un libro con el título: " + request.getTitle());
        }
        if (bookRepository.existsByIsbn(normalizedIsbn)) {
            throw new BusinessRuleException("Ya existe un libro con el ISBN: " + normalizedIsbn);
        }

        Book book = Book.builder()
                .title(request.getTitle().trim())
                .author(request.getAuthor().trim())
                .isbn(normalizedIsbn)
                .genre(request.getGenre())
                .totalCopies(request.getTotalCopies())
                .availableCopies(request.getTotalCopies())
                .available(request.getTotalCopies() > 0)
                .publishedDate(request.getPublishedDate())
                .description(request.getDescription())
                .build();

        return BookResponse.fromEntity(bookRepository.save(book));
    }

    @Transactional(readOnly = true)
    public List<BookResponse> findAll(Genre genre, Boolean available) {
        return bookRepository.findAll(BookSpecification.withFilters(genre, available))
                .stream()
                .map(BookResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookResponse findById(Long id) {
        return BookResponse.fromEntity(getBookOrThrow(id));
    }

    @Transactional
    public BookResponse update(Long id, BookRequest request) {
        validateRequest(request);
        Book book = getBookOrThrow(id);
        String normalizedIsbn = normalizeIsbn(request.getIsbn());

        if (bookRepository.existsByTitleIgnoreCaseAndIdNot(request.getTitle().trim(), id)) {
            throw new BusinessRuleException("Ya existe un libro con el título: " + request.getTitle());
        }
        if (bookRepository.existsByIsbnAndIdNot(normalizedIsbn, id)) {
            throw new BusinessRuleException("Ya existe un libro con el ISBN: " + normalizedIsbn);
        }

        int lentCopies = book.getTotalCopies() - book.getAvailableCopies();
        if (request.getTotalCopies() < lentCopies) {
            throw new BusinessRuleException(
                    "El total de copias no puede ser menor que las copias prestadas (" + lentCopies + ")");
        }

        int newAvailableCopies = request.getTotalCopies() - lentCopies;
        if (newAvailableCopies > request.getTotalCopies()) {
            throw new BusinessRuleException("Las copias disponibles no pueden ser mayores que el total de copias");
        }

        book.setTitle(request.getTitle().trim());
        book.setAuthor(request.getAuthor().trim());
        book.setIsbn(normalizedIsbn);
        book.setGenre(request.getGenre());
        book.setTotalCopies(request.getTotalCopies());
        book.setAvailableCopies(newAvailableCopies);
        book.setAvailable(newAvailableCopies > 0);
        book.setPublishedDate(request.getPublishedDate());
        book.setDescription(request.getDescription());

        return BookResponse.fromEntity(bookRepository.save(book));
    }

    @Transactional
    public void delete(Long id) {
        Book book = getBookOrThrow(id);
        if (book.getAvailableCopies() < book.getTotalCopies()) {
            throw new BusinessRuleException(
                    "No se puede eliminar el libro porque tiene copias prestadas");
        }
        bookRepository.delete(book);
    }

    private Book getBookOrThrow(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado con id: " + id));
    }

    private void validateRequest(BookRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new BusinessRuleException("El título es obligatorio");
        }
        if (request.getGenre() == null) {
            throw new BusinessRuleException("El género es obligatorio");
        }
        if (!isValidIsbn(request.getIsbn())) {
            throw new BusinessRuleException("El ISBN no tiene un formato válido (ISBN-10 o ISBN-13)");
        }
        if (request.getPublishedDate() != null && request.getPublishedDate().isAfter(LocalDate.now())) {
            throw new BusinessRuleException("La fecha de publicación no puede ser futura");
        }
    }

    private boolean isValidIsbn(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            return false;
        }
        String normalized = normalizeIsbn(isbn);
        return normalized.matches(ISBN_PATTERN);
    }

    private String normalizeIsbn(String isbn) {
        return isbn.replaceAll("[\\s-]", "").toUpperCase();
    }
}
