package com.amigoscode.testing.payment.stripe;

import com.amigoscode.testing.payment.CardPaymentCharge;
import com.amigoscode.testing.payment.Currency;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.net.RequestOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

class StripeServiceTest {

    @Mock
    private StripeApi stripeApi;

    private StripeService underTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        underTest = new StripeService(stripeApi);
    }

    @Test
    void itShouldChargeCardSuccessfully() throws StripeException {
        // Given
        final String cardSource = "4544";
        final BigDecimal amount = new BigDecimal("1000");
        final Currency currency = Currency.USD;
        final String description = "some description";

        // When
        var charge = new Charge();
        charge.setPaid(true);
        given(stripeApi.create(any(), any())).willReturn(charge);
        CardPaymentCharge cardPaymentCharge = underTest.chargeCard(cardSource, amount, currency, description);
        final ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);

        final ArgumentCaptor<RequestOptions> requestOptionsCaptor = ArgumentCaptor.forClass(RequestOptions.class);
        then(stripeApi).should().create(mapCaptor.capture(), requestOptionsCaptor.capture());
        final Map<String, Object> params = mapCaptor.getValue();
        final RequestOptions options = requestOptionsCaptor.getValue();

        // Then
        assertThat(params.keySet().size()).isEqualTo(4);
        assertThat(params.get("source")).isEqualTo(cardSource);
        assertThat(params.get("description")).isEqualTo(description);
        assertThat(params.get("amount")).isEqualTo(amount);
        assertThat(params.get("currency")).isEqualTo(currency);

        assertThat(options).isNotNull();

        assertThat(cardPaymentCharge.isCardDebited()).isEqualTo(true);

    }

    @Test
    void itShouldThrowIfCardIsNotCharged() throws StripeException {
        // Given
        final String cardSource = "4544";
        final BigDecimal amount = new BigDecimal("1000");
        final Currency currency = Currency.USD;
        final String description = "some description";

        // When
        // Throw exception when stripe api is called
        StripeException stripeException = mock(StripeException.class);
        doThrow(stripeException).when(stripeApi).create(anyMap(), any());

        // Then
        assertThatThrownBy(() -> underTest.chargeCard(cardSource, amount, currency, description))
                .isInstanceOf(IllegalStateException.class)
                .hasRootCause(stripeException)
                .hasMessageContaining("Cannot make stripe charge");
    }
}