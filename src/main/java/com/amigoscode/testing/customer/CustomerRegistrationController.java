package com.amigoscode.testing.customer;

import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("api/v1/customer-registration")
public class CustomerRegistrationController {

    private final CustomerRegistrationService customerRegistrationService;

    public CustomerRegistrationController(CustomerRegistrationService customerRegistrationService) {
        this.customerRegistrationService = customerRegistrationService;
    }

    @PostMapping
    public void registerNewCustomer(
          @RequestBody @Valid CustomerRegistrationRequest request
    ) {

        customerRegistrationService.registerNewCustomer(request);
    }

}
