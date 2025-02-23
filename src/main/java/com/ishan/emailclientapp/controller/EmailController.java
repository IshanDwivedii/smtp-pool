package com.ishan.emailclientapp.controller;


import com.ishan.emailclientapp.model.EmailRequest;
import com.ishan.emailclientapp.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    //method to post - send email
    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(@RequestBody EmailRequest emailRequest){
        boolean isSent = emailService.sendEmail(emailRequest);
        if(isSent){
            return ResponseEntity.ok("Email sent successfully :)");
        }
        else{
            return ResponseEntity.status(500).body("Failed To send email :(");
        }
    }
}
