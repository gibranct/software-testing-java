package com.amigoscode.testing.customer;

import com.amigoscode.testing.utils.PhoneNumberValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class CustomerRegistrationServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PhoneNumberValidator phoneNumberValidator;

    @Captor
    private ArgumentCaptor<Customer> customerArgumentCaptor;

    private CustomerRegistrationService underTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        underTest = new CustomerRegistrationService(customerRepository, phoneNumberValidator);
    }

    @Test
    void itShouldSaveNewCustomer() {
        // Given
        final String phoneNumber = "000558";
        final Customer customer = new Customer(null, "Alex", phoneNumber);

        final CustomerRegistrationRequest customerRegistrationRequest = new CustomerRegistrationRequest(customer);

        given(customerRepository.findCustomerByPhoneNumber(phoneNumber))
                .willReturn(Optional.empty());
        given(phoneNumberValidator.test(phoneNumber))
                .willReturn(true);


        // When
        underTest.registerNewCustomer(customerRegistrationRequest);

        // Then
        then(customerRepository).should().save(customerArgumentCaptor.capture());
        Customer capturedCustomer = customerArgumentCaptor.getValue();
        assertThat(capturedCustomer.getName()).isEqualTo(customer.getName());
        assertThat(capturedCustomer.getPhoneNumber()).isEqualTo(customer.getPhoneNumber());
        assertThat(capturedCustomer.getId()).isNotNull();
    }

    @Test
    void itShouldNotSaveNewCustomerWhenPhoneNumberIsInvalid() {
        // Given
        final String phoneNumber = "000558";
        final Customer customer = new Customer(UUID.randomUUID(), "Alex", phoneNumber);

        final CustomerRegistrationRequest customerRegistrationRequest = new CustomerRegistrationRequest(customer);

        given(customerRepository.findCustomerByPhoneNumber(phoneNumber))
                .willReturn(Optional.empty());
        given(phoneNumberValidator.test(phoneNumber))
                .willReturn(false);


        // When
        // Then
        assertThatThrownBy(() -> underTest.registerNewCustomer(customerRegistrationRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(String.format("Phone number %s is not valid", phoneNumber));
    }

    @Test
    void itShouldSaveNewCustomerWhenIdIsNull() {
        // Given
        final String phoneNumber = "000558";
        final Customer customer = new Customer(UUID.randomUUID(), "Alex", phoneNumber);

        final CustomerRegistrationRequest customerRegistrationRequest = new CustomerRegistrationRequest(customer);

        given(customerRepository.findCustomerByPhoneNumber(phoneNumber))
                .willReturn(Optional.empty());
        given(phoneNumberValidator.test(phoneNumber))
                .willReturn(true);


        // When
        underTest.registerNewCustomer(customerRegistrationRequest);

        // Then
        then(customerRepository).should().save(customerArgumentCaptor.capture());
        Customer capturedCustomer = customerArgumentCaptor.getValue();
        assertThat(capturedCustomer).isEqualToIgnoringGivenFields(customer, "id");
        assertThat(capturedCustomer.getId()).isNotNull();
    }

    @Test
    void itShouldNotSaveCustomerAlreadyExists() {
        // Given
        final String phoneNumber = "000558";
        final Customer customer = new Customer(UUID.randomUUID(), "Alex", phoneNumber);

        final CustomerRegistrationRequest customerRegistrationRequest = new CustomerRegistrationRequest(customer);

        given(customerRepository.findCustomerByPhoneNumber(phoneNumber))
                .willReturn(Optional.of(customer));
        given(phoneNumberValidator.test(phoneNumber))
                .willReturn(true);


        // When
        underTest.registerNewCustomer(customerRegistrationRequest);

        // Then
        then(customerRepository).should(never()).save(any());
        // then(customerRepository).should().findCustomerByPhoneNumber(phoneNumber);
        // then(customerRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void itShouldNThrowWhenPhoneNumberIsTaken() {
        // Given
        final String phoneNumber = "000558";
        final Customer customer = new Customer(UUID.randomUUID(), "Alex", phoneNumber);

        final CustomerRegistrationRequest customerRegistrationRequest = new CustomerRegistrationRequest(
                new Customer(UUID.randomUUID(), "John", phoneNumber)
        );

        given(customerRepository.findCustomerByPhoneNumber(phoneNumber))
                .willReturn(Optional.of(customer));
        given(phoneNumberValidator.test(phoneNumber))
                .willReturn(true);

        // When
        // Then
        assertThatThrownBy(() -> underTest.registerNewCustomer(customerRegistrationRequest))
                .hasMessageContaining(String.format("The phone number [%s] is already taken", phoneNumber))
                .isInstanceOf(IllegalArgumentException.class);
    }
}