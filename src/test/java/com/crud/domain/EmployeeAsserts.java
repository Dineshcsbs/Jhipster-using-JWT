package com.crud.domain;

import static org.assertj.core.api.Assertions.assertThat;

public class EmployeeAsserts {

    /**
     * Asserts that the entity has all properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertEmployeeAllPropertiesEquals(Employee expected, Employee actual) {
        assertEmployeeAutoGeneratedPropertiesEquals(expected, actual);
        assertEmployeeAllUpdatablePropertiesEquals(expected, actual);
    }

    /**
     * Asserts that the entity has all updatable properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertEmployeeAllUpdatablePropertiesEquals(Employee expected, Employee actual) {
        assertEmployeeUpdatableFieldsEquals(expected, actual);
        assertEmployeeUpdatableRelationshipsEquals(expected, actual);
    }

    /**
     * Asserts that the entity has all the auto generated properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertEmployeeAutoGeneratedPropertiesEquals(Employee expected, Employee actual) {
        assertThat(expected)
            .as("Verify Employee auto generated properties")
            .satisfies(e -> assertThat(e.getId()).as("check id").isEqualTo(actual.getId()));
    }

    /**
     * Asserts that the entity has all the updatable fields set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertEmployeeUpdatableFieldsEquals(Employee expected, Employee actual) {
        assertThat(expected)
            .as("Verify Employee relevant properties")
            .satisfies(e -> assertThat(e.getName()).as("check name").isEqualTo(actual.getName()))
            .satisfies(e -> assertThat(e.getAge()).as("check age").isEqualTo(actual.getAge()))
            .satisfies(e -> assertThat(e.getGender()).as("check gender").isEqualTo(actual.getGender()))
            .satisfies(e -> assertThat(e.getPancard()).as("check pancard").isEqualTo(actual.getPancard()));
    }

    /**
     * Asserts that the entity has all the updatable relationships set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertEmployeeUpdatableRelationshipsEquals(Employee expected, Employee actual) {
        assertThat(expected)
            .as("Verify Employee relationships")
            .satisfies(e -> assertThat(e.getCompany()).as("check company").isEqualTo(actual.getCompany()));
    }
}
