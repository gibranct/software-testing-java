package com.amigoscode.testing.payment;

import java.math.BigDecimal;

public interface CardPaymentCharger {

    CardPaymentCharge chargeCard(
            final String cardSource,
            final BigDecimal amout,
            final Currency currency,
            final String description
            );
}
