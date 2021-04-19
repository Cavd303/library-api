package com.cvieira.libraryapi.resource;

import com.cvieira.libraryapi.dto.BookDTO;
import com.cvieira.libraryapi.dto.LoanDTO;
import com.cvieira.libraryapi.model.entity.Book;
import com.cvieira.libraryapi.model.entity.Loan;
import com.cvieira.libraryapi.service.BookService;
import com.cvieira.libraryapi.service.LoanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Api("Book API")
@Slf4j //adiciona logs
public class BookController {

    private final BookService bookService;
    private final ModelMapper modelMapper;
    private final LoanService loanService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("Creates a book")
    public BookDTO create(@RequestBody @Valid BookDTO dto) {
        log.info("creating  a book for isbn: {}", dto.getIsbn());
        Book entity = modelMapper.map(dto, Book.class);

        entity = bookService.save(entity);

        return modelMapper.map(entity, BookDTO.class);
    }

    @GetMapping("{id}")
    @ApiOperation("Obtains a book details by id")
    public BookDTO get(@PathVariable Long id) {
        log.info("Obtaining details for a book id {}", id);
        return bookService
                .getById(id)
                .map( book -> modelMapper.map(book, BookDTO.class))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation("Deletes a book by id")
    @ApiResponses({
            @ApiResponse(code = 204, message = "Book succefully deleted")
    })
    public void delete(@PathVariable Long id) {
        log.info("Delete book for id {}", id);
        Book book = bookService.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        bookService.delete(book);
    }

    @PutMapping("{id}")
    @ApiOperation("Updates a book by id")
    public BookDTO update(@PathVariable Long id, @RequestBody @Valid BookDTO bookDTO) {
        log.info("Update book for id {}", id);
        return bookService.getById(id).map(book -> {
            book.setAuthor(bookDTO.getAuthor());
            book.setTitle(bookDTO.getTitle());
            book = bookService.update(book);

            return modelMapper.map(book, BookDTO.class);
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    @ApiOperation("Find books by params")
    public Page<BookDTO> find(BookDTO dto, Pageable pageRequest) {
        Book filter = modelMapper.map(dto, Book.class);

        Page<Book> result = bookService.find(filter, pageRequest);

        List<BookDTO> list =  result.getContent()
                .stream()
                .map(entity -> modelMapper.map(entity, BookDTO.class))
                .collect(Collectors.toList());

        return new PageImpl<BookDTO>(list, pageRequest, result.getTotalElements());
    }

    @GetMapping("{id}/loans")
    @ApiOperation("Find loans by book id")
    public Page<LoanDTO> loandByBook(@PathVariable Long id, Pageable pageable) {
        Book book = bookService.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Page<Loan> result = loanService.getLoansByBook(book, pageable);

        List<LoanDTO> list = result.getContent()
                    .stream()
                    .map(loan -> {
                        Book loanBook = loan.getBook();
                        BookDTO bookDTO = modelMapper.map(loan, BookDTO.class);
                        LoanDTO loanDTO = modelMapper.map(loan, LoanDTO.class);
                        loanDTO.setBook(bookDTO);
                        return  loanDTO;
                    }).collect(Collectors.toList());

        return new PageImpl<LoanDTO>(list, pageable, result.getTotalElements());
    }


}
