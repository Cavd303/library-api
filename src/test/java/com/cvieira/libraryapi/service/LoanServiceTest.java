package com.cvieira.libraryapi.service;

import com.cvieira.libraryapi.dto.LoanFilterDTO;
import com.cvieira.libraryapi.exception.BusinessException;
import com.cvieira.libraryapi.model.entity.Book;
import com.cvieira.libraryapi.model.entity.Loan;
import com.cvieira.libraryapi.model.repository.LoanRepository;
import com.cvieira.libraryapi.service.impl.LoanServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {

    private LoanService loanService;

    @MockBean
    private LoanRepository repository;

    @BeforeEach
    public void setUp() {
        loanService = new LoanServiceImpl(repository);
    }

    @Test
    @DisplayName("Deve salvar um emprestimo")
    public void saveLoan() {
        Book book = Book.builder().id(1l).build();

        Loan savingLoan = Loan.builder()
                .book(book)
                .customer("fulano")
                .loanDate(LocalDate.now())
                .build();

        Loan savedLoan = Loan.builder()
                .id(1l)
                .book(savingLoan.getBook())
                .customer(savingLoan.getCustomer())
                .loanDate(savingLoan.getLoanDate())
                .build();

        Mockito.when(repository.existsByBookAndNotReturned(book)).thenReturn(false);
        Mockito.when(repository.save(savingLoan)).thenReturn(savedLoan);

        Loan loan = loanService.save(savingLoan);


        Assertions.assertEquals(savedLoan.getId(), loan.getId());
        Assertions.assertEquals(savedLoan.getBook().getId(), loan.getBook().getId());
        Assertions.assertEquals(savedLoan.getCustomer(), loan.getCustomer());
        Assertions.assertEquals(savedLoan.getLoanDate(), loan.getLoanDate());
    }

    @Test
    @DisplayName("Deve lançar erro de negócio ao salvar um emprestimo com livro já emprestado")
    public void loanedBookSave() {

        Book book = Book.builder().id(1l).build();
        Loan savingLoan = Loan.builder()
                .book(book)
                .customer("fulano")
                .loanDate(LocalDate.now())
                .build();


        Mockito.when(repository.existsByBookAndNotReturned(book)).thenReturn(true);

        Assertions.assertThrows(BusinessException.class, () -> loanService.save(savingLoan));


        Mockito.verify(repository, Mockito.never()).save(savingLoan);
    }

    @Test
    @DisplayName("Deve obter as informações de um emprèstimo pelo id")
    public void getLoanDetails() {
        Long id = 1l;
        Loan loan = createLoan();
        loan.setId(id);


        Mockito.when(repository.findById(id)).thenReturn(Optional.of(loan));

        Optional<Loan> result = loanService.getById(id);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(id, result.get().getId());
        Assertions.assertEquals(loan.getCustomer(), result.get().getCustomer());
        Assertions.assertEquals(loan.getBook(), result.get().getBook());
        Assertions.assertEquals(loan.getLoanDate(), result.get().getLoanDate());

        Mockito.verify(repository).findById(id);
    }

    @Test
    @DisplayName("Deve atualizar um empréstimo")
    public void updateLoan() {
        Loan loan = createLoan();
        loan.setId(1l);
        loan.setReturned(true);
        Mockito.when(repository.save(loan)).thenReturn(loan);


        Loan updatedLoan = loanService.update(loan);

        Assertions.assertTrue(updatedLoan.isReturned());
    }

    @Test
    @DisplayName("Deve filtrar empréstimos pelas propriedades")
    public void findLoan() {
        //Cenário
        LoanFilterDTO dto = LoanFilterDTO.builder().customer("Fulano").isbn("321").build();
        Loan loan = createLoan();


        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Loan> list = Arrays.asList(loan);

        Page<Loan> page = new PageImpl<Loan>(
                list, pageRequest, list.size());

        Mockito.when(repository.findByBookIsbnOrCustomer(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.any(PageRequest.class))).thenReturn(page);

        //Execução
        Page<Loan> result = loanService.find(dto, pageRequest);

        //Verificação
        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(list, result.getContent());
        Assertions.assertEquals(0, result.getPageable().getPageNumber());
        Assertions.assertEquals(10, result.getPageable().getPageSize());
    }

    public Loan createLoan() {
        Book book = Book.builder().id(1l).build();
        return Loan.builder()
                .book(book)
                .customer("fulano")
                .loanDate(LocalDate.now())
                .build();
    }
}
