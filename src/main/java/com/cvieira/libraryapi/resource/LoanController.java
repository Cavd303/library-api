package com.cvieira.libraryapi.resource;

import com.cvieira.libraryapi.dto.BookDTO;
import com.cvieira.libraryapi.dto.LoanDTO;
import com.cvieira.libraryapi.dto.LoanFilterDTO;
import com.cvieira.libraryapi.dto.ReturnedLoadDTO;
import com.cvieira.libraryapi.model.entity.Book;
import com.cvieira.libraryapi.model.entity.Loan;
import com.cvieira.libraryapi.service.BookService;
import com.cvieira.libraryapi.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {


    private final LoanService loanService;
    private final BookService bookService;
    private final ModelMapper modelMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody LoanDTO dto) {
        Book book = bookService.getBookByIsbn(dto.getIsbn())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book not found for passed isbn"));
        Loan entity = Loan.builder()
                            .book(book)
                            .customer(dto.getCustomer())
                            .loanDate(LocalDate.now())
                            .build();

        Loan savedOne = loanService.save(entity);

        return savedOne.getId();
    }

    @PatchMapping("{id}")
    public void returnBook(@PathVariable Long id, @RequestBody ReturnedLoadDTO dto) {
        Loan loan = loanService.getById(id).orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        loan.setReturned(dto.getReturned());

        loanService.update(loan);
    }

    @GetMapping
    public Page<LoanDTO> find(LoanFilterDTO dto, Pageable pageable) {
        Page<Loan> result = loanService.find(dto, pageable);
        List<LoanDTO> list = result.getContent().stream().map(
                entity -> {
                    Book book = entity.getBook();
                    BookDTO bookDTO = modelMapper.map(book, BookDTO.class);
                    LoanDTO loanDTO = modelMapper.map(entity, LoanDTO.class);
                    loanDTO.setBook(bookDTO);
                    return loanDTO;
                })
            .collect(Collectors.toList());

        return new PageImpl<LoanDTO>(list, pageable, result.getTotalElements());
    }
}
