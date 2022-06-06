package com.amigoscode.testing.payment;

import com.amigoscode.testing.customer.Customer;
import com.amigoscode.testing.customer.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    final private List<Currency> ACCEPTED_CURRENCIES = List.of(Currency.USD, Currency.GPD);

    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;
    private final CardPaymentCharger cardPaymentCharger;

    public PaymentService(
            PaymentRepository paymentRepository,
            CustomerRepository customerRepository,
            CardPaymentCharger cardPaymentCharger
    ) {
        this.paymentRepository = paymentRepository;
        this.customerRepository = customerRepository;
        this.cardPaymentCharger = cardPaymentCharger;
    }

    void chargeCard(UUID customerId, PaymentRequest paymentRequest) {
        Optional<Customer> optionalCustomer = customerRepository.findById(customerId);
        optionalCustomer.orElseThrow(IllegalStateException::new);
        boolean isCurrencyValid = ACCEPTED_CURRENCIES.stream()
                .anyMatch(c -> c.equals(paymentRequest.getPayment().getCurrency()));
        if (!isCurrencyValid) {
            throw new IllegalStateException(
               String.format("Invalid currency: the currency %s is not supported", paymentRequest.getPayment().getCurrency())
            );
        }
        final CardPaymentCharge cardPaymentCharge = cardPaymentCharger.chargeCard(
                paymentRequest.getPayment().getSource(),
                paymentRequest.getPayment().getAmount(),
                paymentRequest.getPayment().getCurrency(),
                paymentRequest.getPayment().getDescription()
        );

        if (!cardPaymentCharge.isCardDebited()) {
            throw new IllegalStateException(String.format("Card not debited for customer %s", customerId));
        }

        paymentRequest.getPayment().setCustomerId(customerId);

        paymentRepository.save(paymentRequest.getPayment());
    }
}
