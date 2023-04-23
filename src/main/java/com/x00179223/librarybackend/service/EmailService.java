package com.x00179223.librarybackend.service;

import com.x00179223.librarybackend.model.User;
import org.springframework.stereotype.Service;

public interface EmailService {
    void sendOverdueEmail(String to, String subject, String body);
}
