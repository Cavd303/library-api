package com.cvieira.libraryapi.service.impl;

import com.cvieira.libraryapi.exception.BusinessException;
import com.cvieira.libraryapi.model.entity.Book;
import com.cvieira.libraryapi.model.repository.BookRepository;
import com.cvieira.libraryapi.service.BookService;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.util.Optional;

@Service
public class BookServiceImpl implements BookService {

    private BookRepository repository;

    public BookServiceImpl(BookRepository bookRepository) {
        this.repository = bookRepository;
    }

    public Book save(Book book) {
        if(repository.existsByIsbn(book.getIsbn())) {
            throw new BusinessException("ISBN já cadastrado");
        }
        return repository.save(book);
    }

    public Optional<Book> getById(Long id) {
        return repository.findById(id);
    }

    @Override
    public void delete(Book book) {
        System.out.println(book.getId());
        if(book == null || book.getId() == null) {
            throw new IllegalArgumentException("Book id cant be null.");
        }
        repository.delete(book);
    }

    @Override
    public Book update(Book book) {
        if(book == null || book.getId() == null) {
            throw new IllegalArgumentException("Book id cant be null.");
        }
        return repository.save(book);
    }

    @Override
    public Page<Book> find(Book filter, Pageable pageRequest) {
        Example<Book> example = Example.of(filter,
                                    ExampleMatcher.matching()
                                    .withIgnoreCase()
                                    .withIgnoreNullValues()
                                    .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING));
        return repository.findAll(example, pageRequest);
    }

    @Override
    public Optional<Book> getBookByIsbn(String isbn) {
        return repository.findByIsbn(isbn);
    }


}
