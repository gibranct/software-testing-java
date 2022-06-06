package com.amigoscode.testing.payment;

import com.amigoscode.testing.customer.Customer;
import com.amigoscode.testing.customer.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

class PaymentServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private CardPaymentCharger cardPaymentCharger;

    @Captor
    private ArgumentCaptor<Payment> paymentArgumentCaptor;

    private PaymentService underTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        underTest = new PaymentService(paymentRepository, customerRepository, cardPaymentCharger);
    }

    @Test
    void itShouldThrowIfCustomerNotExists() {
        // Given
        UUID customerId = UUID.randomUUID();
        PaymentRequest paymentRequest = new PaymentRequest(new Payment());

        // When
        given(customerRepository.findById(customerId)).willReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> underTest.chargeCard(customerId, paymentRequest))
                .isInstanceOf(IllegalStateException.class);
        then(customerRepository).should(Mockito.times(1)).findById(customerId);
        then(paymentRepository).shouldHaveNoMoreInteractions();
        then(cardPaymentCharger).shouldHaveNoInteractions();
    }

    @Test
    void itShouldThrowIfCurrencyNotExits() {
        // Given
        final UUID customerId = UUID.randomUUID();
        PaymentRequest paymentRequest = new PaymentRequest(new Payment(
              null,
              null,
              new BigDecimal("10"),
              Currency.EUR,
              "23123",
              "Description"
        ));

        // When
        final Customer customer = new Customer(customerId, "Alex", "1111");
        given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));

        // Then
        assertThatThrownBy(() -> underTest.chargeCard(customerId, paymentRequest))
                .hasMessageContaining("Invalid currency: the currency EUR is not supported")
                .isInstanceOf(IllegalStateException.class);
        then(customerRepository).should(Mockito.times(1)).findById(customerId);
        then(paymentRepository).shouldHaveNoMoreInteractions();
        then(cardPaymentCharger).shouldHaveNoMoreInteractions();
    }

    @Test
    void itShouldThrowIfCardIsNotDebited() {
        // Given
        final UUID customerId = UUID.randomUUID();
        PaymentRequest paymentRequest = new PaymentRequest(new Payment(
                null,
                null,
                new BigDecimal("10"),
                Currency.USD,
                "23123",
                "Description"
        ));

        // When
        final Customer customer = new Customer(customerId, "Alex", "1111");
        given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
        given(cardPaymentCharger.chargeCard(any(), any(), any(), any())).willReturn(new CardPaymentCharge(false));

        // Then
        assertThatThrownBy(() -> underTest.chargeCard(customerId, paymentRequest))
                .hasMessageContaining(String.format("Card not debited for customer %s", customerId))
                .isInstanceOf(IllegalStateException.class);
        then(customerRepository).should(Mockito.times(1)).findById(customerId);
        then(cardPaymentCharger).should(Mockito.times(1)).chargeCard(any(), any(), any(), any());
        then(paymentRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void itShouldChargeCustomerSuccessfully() {
        // Given
        final UUID customerId = UUID.randomUUID();
        PaymentRequest paymentRequest = new PaymentRequest(new Payment(
                null,
                null,
                new BigDecimal("10"),
                Currency.USD,
                "23123",
                "Description"
        ));
        given(customerRepository.findById(customerId)).willReturn(Optional.of(mock(Customer.class)));
        given(cardPaymentCharger.chargeCard(any(), any(), any(), any())).willReturn(new CardPaymentCharge(true));

        // When
        underTest.chargeCard(customerId, paymentRequest);

        // Then
        then(paymentRepository).should(Mockito.times(1)).save(
                paymentArgumentCaptor.capture()
        );
        Payment capturedPayment = paymentArgumentCaptor.getValue();
        assertThat(capturedPayment).isEqualToIgnoringGivenFields(paymentRequest.getPayment(), "customerId");
        assertThat(capturedPayment.getCustomerId()).isEqualTo(customerId);
    }
}