package com.example.campaignapi.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RequestValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @Test
    void createRequest_withBlankFields_hasViolationsForBoth() {
        Set<ConstraintViolation<CreateCampaignRequest>> violations =
                validator.validate(new CreateCampaignRequest("", ""));

        assertThat(violations).hasSize(2);
    }

    @Test
    void createRequest_withValidFields_hasNoViolations() {
        Set<ConstraintViolation<CreateCampaignRequest>> violations =
                validator.validate(new CreateCampaignRequest("Spring Sale", "20% off"));

        assertThat(violations).isEmpty();
    }

    @Test
    void updateRequest_withNullFields_hasNoViolations() {
        Set<ConstraintViolation<UpdateCampaignRequest>> violations =
                validator.validate(new UpdateCampaignRequest(null, null));

        assertThat(violations).isEmpty();
    }

    @Test
    void updateRequest_withBlankName_hasViolation() {
        Set<ConstraintViolation<UpdateCampaignRequest>> violations =
                validator.validate(new UpdateCampaignRequest("", null));

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("name");
    }

    @Test
    void updateRequest_withBlankSubject_hasViolation() {
        Set<ConstraintViolation<UpdateCampaignRequest>> violations =
                validator.validate(new UpdateCampaignRequest(null, ""));

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("subject");
    }
}
