package com.x00179223.librarybackend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue
    private Long id;
    private String title;
    private String author;
    private String ISBN;
    private String genre;
    private int publicationYear;
    private int quantityAvailable;
    private double rating;
    private int ratingCount;
    private double ratingTotal;
}
