package com.estatepro.service;

import com.estatepro.dto.ApiDtos.PaymentView;
import com.estatepro.entity.Payment;
import com.estatepro.entity.Role;
import com.estatepro.entity.User;
import com.estatepro.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {
    private final PaymentRepository payments;
    private final BookingService bookings;
    public PaymentService(PaymentRepository payments, BookingService bookings) {
        this.payments = payments; this.bookings = bookings;
    }
    @Transactional
    public PaymentView simulate(User user, UUID bookingId) {
        if (user.getRole() != Role.BUYER) throw new SecurityException("Only buyers can complete demo payments");
        var booking = bookings.requireOwnedForPayment(bookingId, user);
        Payment payment = payments.save(new Payment(booking,
                "DEMO-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase(), booking.getAmount()));
        return view(payment);
    }
    @Transactional(readOnly = true)
    public List<PaymentView> list(User user) {
        if (user.getRole() != Role.BUYER) throw new SecurityException("Payment history is private to the buyer");
        return payments.findByBookingBuyerIdOrderByCreatedAtDesc(user.getId()).stream().map(this::view).toList();
    }
    private PaymentView view(Payment payment) {
        return new PaymentView(payment.getTransactionReference(), payment.getBooking().getReference(),
                payment.getAmount(), payment.getStatus(), payment.getCreatedAt());
    }
}
