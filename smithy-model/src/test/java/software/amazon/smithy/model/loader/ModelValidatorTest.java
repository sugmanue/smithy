/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package software.amazon.smithy.model.loader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.validation.Severity;
import software.amazon.smithy.model.validation.ValidatedResult;
import software.amazon.smithy.model.validation.ValidationEvent;
import software.amazon.smithy.model.validation.Validator;

/**
 * This test exercises ModelValidator but done so using the ModelAssembler.
 */
public class ModelValidatorTest {

    @Test
    public void addsExplicitValidators() {
        ValidationEvent event = ValidationEvent.builder()
                .severity(Severity.ERROR)
                .id("Foo")
                .message("bar")
                .build();
        String document = "{\"smithy\": \"1.0\"}";
        ValidatedResult<Model> result = new ModelAssembler()
                .addUnparsedModel("[N/A]", document)
                .addValidator(index -> Collections.singletonList(event))
                .assemble();

        assertThat(result.getValidationEvents(), contains(event));
    }

    @Test
    public void registersTraitValidators() {
        ValidatedResult<Model> result = new ModelAssembler()
                .addImport(getClass().getResource("errors.json"))
                .assemble();

        assertThat(result.getValidationEvents(), hasSize(2));
        assertThat(result.getValidationEvents().get(0).getMessage(), containsString("client"));
        assertThat(result.getValidationEvents().get(1).getMessage(), containsString("server"));
    }

    // Custom validators are not critical, so this one only reports an event when validation reaches
    // the non-critical validators.
    private static final Validator REACHED_NON_CRITICAL = model -> Collections.singletonList(
            ValidationEvent.builder().id("ReachedNonCritical").severity(Severity.ERROR).message("reached").build());

    // ns.foo#Foo and ns.foo#foo conflict case-insensitively, which ShapeIdConflictValidator (a critical
    // validator) reports.
    private static final String CASE_CONFLICT = "\"ns.foo#Foo\": {\"type\": \"string\"}, "
            + "\"ns.foo#foo\": {\"type\": \"string\"}";

    private static List<String> errorIds(ValidatedResult<Model> result) {
        return result.getValidationEvents(Severity.ERROR)
                .stream()
                .map(ValidationEvent::getId)
                .sorted()
                .collect(Collectors.toList());
    }

    @Test
    public void correctnessErrorsStopValidation() {
        // An unresolved reference is reported by TargetValidator, a correctness validator. Its errors
        // stop validation before any other critical or non-critical validator runs.
        String document = "{\"smithy\": \"2.0\", \"shapes\": {" + CASE_CONFLICT + ", "
                + "\"ns.foo#Operation\": {\"type\": \"operation\", \"input\": {\"target\": \"ns.foo#Missing\"}}}}";
        ValidatedResult<Model> result = new ModelAssembler()
                .addUnparsedModel("a.json", document)
                .addValidator(REACHED_NON_CRITICAL)
                .assemble();

        assertThat(errorIds(result), contains("Target.UnresolvedShape"));
    }

    @Test
    public void criticalErrorsStopNonCriticalValidators() {
        String document = "{\"smithy\": \"2.0\", \"shapes\": {" + CASE_CONFLICT + "}}";
        ValidatedResult<Model> result = new ModelAssembler()
                .addUnparsedModel("a.json", document)
                .addValidator(REACHED_NON_CRITICAL)
                .assemble();

        assertThat(errorIds(result), everyItem(equalTo("ShapeIdConflict")));
        assertThat(errorIds(result), not(empty()));
    }

    @Test
    public void legacyValidationModeRunsNonCriticalValidatorsAfterCriticalErrors() {
        String document = "{\"smithy\": \"2.0\", \"shapes\": {" + CASE_CONFLICT + "}}";
        ValidatedResult<Model> result = new ModelAssembler()
                .addUnparsedModel("a.json", document)
                .addValidator(REACHED_NON_CRITICAL)
                .putProperty("LEGACY_VALIDATION_MODE", true)
                .assemble();

        assertThat(errorIds(result), hasItem("ShapeIdConflict"));
        assertThat(errorIds(result), hasItem("ReachedNonCritical"));
    }

    @Test
    public void nonCriticalValidatorsRunWithoutCriticalErrors() {
        ValidatedResult<Model> result = new ModelAssembler()
                .addUnparsedModel("a.json",
                        "{\"smithy\": \"2.0\", \"shapes\": {\"ns.foo#Foo\": {\"type\": \"string\"}}}")
                .addValidator(REACHED_NON_CRITICAL)
                .assemble();

        assertThat(errorIds(result), contains("ReachedNonCritical"));
    }

    @Test
    public void loadErrorsSkipValidation() {
        // An unknown trait is reported while loading; the model is then not validated at all.
        String document = "{\"smithy\": \"2.0\", \"shapes\": {" + CASE_CONFLICT + ", "
                + "\"ns.foo#Bar\": {\"type\": \"string\", \"traits\": {\"com.foo#unknown\": {}}}}}";
        ValidatedResult<Model> result = new ModelAssembler()
                .addUnparsedModel("a.json", document)
                .addValidator(REACHED_NON_CRITICAL)
                .assemble();

        assertThat(errorIds(result), contains("Model.UnresolvedTrait"));
        assertTrue(result.getResult().isPresent());
    }
}
