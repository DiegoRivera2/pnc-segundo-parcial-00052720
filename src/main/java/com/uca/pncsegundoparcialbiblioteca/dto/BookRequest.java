package com.uca.pncsegundoparcialbiblioteca.dto;

import com.uca.pncsegundoparcialbiblioteca.entity.Genre;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookRequest {

    @NotBlank(message = "El título es obligatorio")
    private String title;

    @NotBlank(message = "El autor es obligatorio")
    private String author;

    @NotBlank(message = "El ISBN es obligatorio")
    private String isbn;

    @NotNull(message = "El género es obligatorio")
    private Genre genre;

    @NotNull(message = "El total de copias es obligatorio")
    @Min(value = 1, message = "El total de copias debe ser al menos 1")
    private Integer totalCopies;

    private LocalDate publishedDate;

    private String description;
}
