package com.estatepro.service;

import com.estatepro.dto.ApiDtos.*;
import com.estatepro.entity.*;
import com.estatepro.exception.ResourceNotFoundException;
import com.estatepro.repository.EnquiryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class EnquiryService {
    private static final List<String> STATUSES = List.of("NEW", "CONTACTED", "INTERESTED", "VISIT_SCHEDULED", "NEGOTIATION", "CONVERTED", "LOST");
    private final EnquiryRepository enquiries;
    private final PropertyService propertyService;
    public EnquiryService(EnquiryRepository enquiries, PropertyService propertyService) {
        this.enquiries = enquiries; this.propertyService = propertyService;
    }
    @Transactional
    public EnquiryView create(User user, EnquiryRequest request) {
        if (user.getRole() != Role.BUYER) throw new SecurityException("Only buyer accounts can submit enquiries");
        return EnquiryView.from(enquiries.save(new Enquiry(propertyService.requirePublished(request.propertyId()), user, request.message().trim())));
    }
    @Transactional(readOnly = true)
    public List<EnquiryView> list(User user) {
        return switch (user.getRole()) {
            case ADMIN -> enquiries.findAll().stream().map(EnquiryView::from).toList();
            case AGENT -> enquiries.findByAgentIdOrderByCreatedAtDesc(user.getId()).stream().map(EnquiryView::from).toList();
            case BUYER -> enquiries.findByBuyerIdOrderByCreatedAtDesc(user.getId()).stream().map(EnquiryView::from).toList();
            default -> List.of();
        };
    }
    @Transactional
    public EnquiryView update(User user, UUID id, EnquiryStatusRequest request) {
        Enquiry e = enquiries.findById(id).orElseThrow(() -> new ResourceNotFoundException("Enquiry not found"));
        if (user.getRole() != Role.ADMIN && (user.getRole() != Role.AGENT || e.getAgent() == null
                || !e.getAgent().getId().equals(user.getId())))
            throw new SecurityException("You do not have access to this enquiry");
        String status = request.status().trim().toUpperCase();
        if (!STATUSES.contains(status)) throw new IllegalArgumentException("Unsupported enquiry status");
        e.setStatus(status);
        if (request.internalNote() != null) e.setInternalNote(request.internalNote().trim());
        return EnquiryView.from(enquiries.save(e));
    }
}
