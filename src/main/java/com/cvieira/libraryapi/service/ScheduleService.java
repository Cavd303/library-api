package com.cvieira.libraryapi.service;

import com.cvieira.libraryapi.model.entity.Loan;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private static final String CRON_LATE_LOAN = "0 0 0 1/1 * ?";

    private final LoanService loanService;
    private final EmailService emailService;

    @Scheduled(cron = CRON_LATE_LOAN)
    public void sendMailToLateLoans() {
        List<Loan> list = loanService.getAllLateLoans();

        List<String> emailList = list.stream().map(
                loan -> loan.getCustomerEmail()
        ).collect(Collectors.toList());

        String message = "Atenção! Você tem um empréstimo atrasado. Favor devolver o livro mais rápido possível.";

        emailService.sendEmails(message, emailList);
    }
}
