package com.cvieira.libraryapi.model.repository;

import com.cvieira.libraryapi.model.entity.Book;
import com.cvieira.libraryapi.model.entity.Loan;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class LoanRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LoanRepository loanRepository;

    @Test
    @DisplayName("Deve verifica se existe empréstimo não devolvido para o livro")
    public void existsByBookAndNotReturned() {
        //Cenário
        Loan loan = createAndPersistLoan(LocalDate.now());

        boolean exists = loanRepository.existsByBookAndNotReturned(loan.getBook());

        Assertions.assertTrue(exists);
    }

    @Test
    @DisplayName("Deve buscar empréstimo pelo isbn do livro ou customer")
    public void findLoanByBookOrCustomer() {
        Loan loan = createAndPersistLoan(LocalDate.now());

        Page<Loan> result = loanRepository.findByBookIsbnOrCustomer("123", "Fulano", PageRequest.of(0, 10));

        Assertions.assertEquals(1, result.getContent().size());
        Assertions.assertTrue(result.getContent().contains(loan));
        Assertions.assertEquals(10, result.getPageable().getPageSize());
        Assertions.assertEquals(0, result.getPageable().getPageNumber());
        Assertions.assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Deve obter empréstimos cuja data empréstimo for menor ou igual a três dias atrás e não retornados")
    public void findByLoanDateLessThanAndNotReturned() {
        Loan loan = createAndPersistLoan(LocalDate.now().minusDays(5));

        List<Loan> result = loanRepository.findByLoanDateLessThanAndNotReturned(LocalDate.now().minusDays(4));

        Assertions.assertEquals(1, result.size());
        Assertions.assertTrue(result.contains(loan));
    }

    @Test
    @DisplayName("Deve retornar vazio quando não hover empréstimos atrasados")
    public void notFindByLoanDateLessThanAndNotReturned() {
        Loan loan = createAndPersistLoan(LocalDate.now());

        List<Loan> result = loanRepository.findByLoanDateLessThanAndNotReturned(LocalDate.now().minusDays(4));

        Assertions.assertTrue(result.isEmpty());
    }

    private Book createNewBook() {
        return Book.builder().title("Aventuras").author("Fulano").isbn("123").build();
    }

    private Loan createAndPersistLoan(LocalDate loanDate) {
        Book book = createNewBook();
        entityManager.persist(book);

        Loan loan = Loan.builder().book(book).customer("Fulano").loanDate(loanDate).build();
        entityManager.persist(loan);

        return loan;
    }
}
