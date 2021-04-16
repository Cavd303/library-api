package com.cvieira.libraryapi.service;

import com.cvieira.libraryapi.exception.BusinessException;
import com.cvieira.libraryapi.model.entity.Book;
import com.cvieira.libraryapi.model.repository.BookRepository;
import com.cvieira.libraryapi.service.impl.BookServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

    BookService bookService;

    @MockBean
    BookRepository repository;

    @BeforeEach
    public void setUp() {
        this.bookService = new BookServiceImpl(repository);
    }

    @Test
    @DisplayName("Deve salvar um livro")
    public void saveBookTest() {
        //Cenário
        Book book = createValidBook();
        Mockito.when(repository.save(book)).thenReturn(
                Book.builder().id(1l).isbn(book.getIsbn()).title(book.getTitle()).author(book.getAuthor()).build());

        //Execução
        Book savedBook = bookService.save(book);

        //Verificação
        Assertions.assertTrue(savedBook.getId() != null);
        Assertions.assertEquals(book.getIsbn(), savedBook.getIsbn());
        Assertions.assertEquals(book.getTitle(), savedBook.getTitle());
        Assertions.assertEquals(book.getAuthor(), savedBook.getAuthor());
    }

    @Test
    @DisplayName("Deve lançar erro de negócio ao tentar salvar um livro com isbn duplicado")
    public void shouldNotSaveABookWithDuplicatedISBN() {
        //Cenário
        Book book = createValidBook();
        Mockito.when(repository.existsByIsbn(book.getIsbn())).thenReturn(true);

        //execução //Verificação
        Assertions.assertThrows(BusinessException.class, () -> bookService.save(book));
        Mockito.verify(repository, Mockito.never()).save(book);
    }

    @Test
    @DisplayName("Deve obter um livro por id")
    public void getById() {
        //Cenário
        Long id = 1l;
        Book book = createValidBook();
        book.setId(id);
        Mockito.when(repository.findById(id)).thenReturn(Optional.of(book));

        //Execução
        Optional<Book> foundBook = bookService.getById(id);

        //Verificações
        Assertions.assertTrue(foundBook.isPresent());
        Assertions.assertEquals(id, foundBook.get().getId());
        Assertions.assertEquals(book.getAuthor(), foundBook.get().getAuthor());
        Assertions.assertEquals(book.getTitle(), foundBook.get().getTitle());
        Assertions.assertEquals(book.getIsbn(), foundBook.get().getIsbn());
    }

    @Test
    @DisplayName("Deve retornar vazio ao obter um livro por id, quando ele não existe na base")
    public void getBookNotFoundById() {
        //Cenário
        Long id = 1l;
        Mockito.when(repository.findById(id)).thenReturn(Optional.empty());

        //Execução
        Optional<Book> foundBook = bookService.getById(id);

        //Verificações
        Assertions.assertFalse(foundBook.isPresent());
    }


    @Test
    @DisplayName("Deve deletar um livro")
    public void deleteBook() {
        //Cenário
        Long id = 1l;
        Book book = createValidBook();
        book.setId(id);

        //Execução
        Assertions.assertDoesNotThrow( () -> bookService.delete(book));

        //Verificações
        Mockito.verify(repository, Mockito.times(1)).delete(book);
    }

    @Test
    @DisplayName("Deve ocorrer erro ao tentar deletar livro inexistente")
    public void deleteIvalidBookTest() {
        //Cenário
        Book book = new Book();

        //Execução
        Assertions.assertThrows(IllegalArgumentException.class, () -> bookService.delete(book));

        //Verificação
        Mockito.verify(repository, Mockito.never()).delete(book);
    }

    @Test
    @DisplayName("Deve atualizar um livro")
    public void updateBook() {
        //Cenário
        Long id = 1l;
        Book updatingBook = Book.builder().id(id).build();
        updatingBook.setId(id);
        Book updatedBook = createValidBook();
        updatedBook.setId(id);

        Mockito.when(repository.save(updatingBook)).thenReturn(updatedBook);

        //Execução
        Book book = bookService.update(updatingBook);

        //Verificações
        Assertions.assertEquals(book.getId(), updatedBook.getId());
        Assertions.assertEquals(updatedBook.getIsbn(), book.getIsbn());
        Assertions.assertEquals(updatedBook.getTitle(), book.getTitle());
        Assertions.assertEquals(updatedBook.getAuthor(), book.getAuthor());
    }

    @Test
    @DisplayName("Deve ocorrer erro ao tentar atualizar livro inexistente")
    public void updateIvalidBook() {
        //Cenário
        Book book = new Book();

        //Execução
        Assertions.assertThrows(IllegalArgumentException.class, () -> bookService.update(book));

        //Verificação
        Mockito.verify(repository, Mockito.never()).save(book);
    }

    @Test
    @DisplayName("Deve filtrar livros pelas propriedades")
    public void findBook() {
        //Cenário
        Book book = createValidBook();


        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Book> list = Arrays.asList(book);

        Page<Book> page = new PageImpl<Book>(
                            list, pageRequest, 1);

        Mockito.when(repository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class))).thenReturn(page);

        //Execução
        Page<Book> result = bookService.find(book, pageRequest);

        //Verificação
        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(list, result.getContent());
        Assertions.assertEquals(0, result.getPageable().getPageNumber());
        Assertions.assertEquals(10, result.getPageable().getPageSize());
    }

    public Book createValidBook() {
        return Book.builder().isbn("123").author("Fulano").title("As aventuras").build();
    }
}
