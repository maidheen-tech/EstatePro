package com.estatepro.controller;

import com.estatepro.dto.ApiDtos.ContactRequest;
import com.estatepro.dto.ApiDtos.ContactResponse;
import com.estatepro.entity.ContactMessage;
import com.estatepro.repository.ContactMessageRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
public class ContactController {
    private final ContactMessageRepository messages;
    public ContactController(ContactMessageRepository messages) { this.messages = messages; }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContactResponse submit(@Valid @RequestBody ContactRequest request) {
        messages.save(new ContactMessage(request.name().trim(), request.email(), request.subject().trim(), request.message().trim()));
        return new ContactResponse("Thanks for reaching out. The EstatePro team will be in touch.");
    }
}
