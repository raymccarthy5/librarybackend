package com.x00179223.librarybackend;

import com.x00179223.librarybackend.service.EmailServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class EmailServiceImplTest {

    @InjectMocks
    private EmailServiceImpl emailService;

    @Mock
    private JavaMailSender mailSender;

    @Test
    public void sendOverdueEmailTest() {
        String to = "test@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        emailService.sendOverdueEmail(to, subject, body);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}
