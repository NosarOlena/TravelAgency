package com.epam.finaltask.service;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentService {

    void processPayment(UUID userId, BigDecimal amount);
    void refundPayment(UUID userId, BigDecimal amount);

}
