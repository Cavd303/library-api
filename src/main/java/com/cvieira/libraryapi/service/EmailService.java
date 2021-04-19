package com.cvieira.libraryapi.service;


import java.util.List;


public interface EmailService {
    void sendEmails(String message, List<String> emailList);
}
