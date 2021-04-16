package com.cvieira.libraryapi.model.repository;

import com.cvieira.libraryapi.model.entity.Book;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    BookRepository repository;

    @Test
    @DisplayName("Deve retornar verdadeiro quando existir um livro na base com o isbn informado")
    public void returnTrueWhenISBNExists() {
        //Cenário
        String isbn = "123";
        Book book = createNewBook();
        book.setIsbn(isbn);
        entityManager.persist(book);
        //Execução
        boolean exists = repository.existsByIsbn(isbn);
        //Verificação
        Assertions.assertTrue(exists);
    }

    @Test
    @DisplayName("Deve obter um livro por id")
    public void findByIdTest() {
        //Cenário
        Book book = createNewBook();
        entityManager.persist(book);

        //Repository
        Optional<Book> foundBook = repository.findById(book.getId());

        //Verificação
        Assertions.assertTrue(foundBook.isPresent());
    }

    @Test
    @DisplayName("Deve salvar um livro")
    public void saveBook() {
        Book book = createNewBook();
        Book savedBook = repository.save(book);

        Assertions.assertTrue(savedBook.getId() != null);
    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void deleteBook() {
        //Cenário
        Book book = createNewBook();
        entityManager.persist(book);

        Book foundBook = entityManager.find(Book.class, book.getId());

        repository.delete(foundBook);
        Book deletedBook = entityManager.find(Book.class, book.getId());

        //Verificação
        Assertions.assertTrue(deletedBook == null);
    }

    private Book createNewBook() {
        return Book.builder().title("Aventuras").author("Fulano").isbn("123").build();
    }
}
