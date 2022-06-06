package com.amigoscode.testing.customer;

import com.amigoscode.testing.utils.PhoneNumberValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class CustomerRegistrationService {

    private final CustomerRepository customerRepository;

    private final PhoneNumberValidator phoneNumberValidator;

    @Autowired
    public CustomerRegistrationService(CustomerRepository customerRepository, PhoneNumberValidator phoneNumberValidator) {
        this.customerRepository = customerRepository;
        this.phoneNumberValidator = phoneNumberValidator;
    }

    public void registerNewCustomer(CustomerRegistrationRequest request) {
        Customer customer = request.getCustomer();

        if (!phoneNumberValidator.test(customer.getPhoneNumber())) {
            throw new IllegalStateException(String.format("Phone number %s is not valid", customer.getPhoneNumber()));
        }

        Optional<Customer> optionalCustomer = customerRepository.findCustomerByPhoneNumber(customer.getPhoneNumber());
        if (optionalCustomer.isPresent()) {
            boolean isTheSameCustomer = optionalCustomer.get().getName().equals(customer.getName());
            if (isTheSameCustomer) return;

            throw new IllegalArgumentException(String.format("The phone number [%s] is already taken", customer.getPhoneNumber()));
        }

        if (customer.getId() == null) {
            customer.setId(UUID.randomUUID());
        }

        customerRepository.save(customer);
    }

}
