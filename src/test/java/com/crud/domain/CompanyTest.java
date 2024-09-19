package com.crud.domain;

import static com.crud.domain.CompanyTestSamples.*;
import static com.crud.domain.EmployeeTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.crud.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CompanyTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Company.class);
        Company company1 = getCompanySample1();
        Company company2 = new Company();
        assertThat(company1).isNotEqualTo(company2);

        company2.setId(company1.getId());
        assertThat(company1).isEqualTo(company2);

        company2 = getCompanySample2();
        assertThat(company1).isNotEqualTo(company2);
    }

    @Test
    void employeeTest() {
        Company company = getCompanyRandomSampleGenerator();
        Employee employeeBack = getEmployeeRandomSampleGenerator();

        company.addEmployee(employeeBack);
        assertThat(company.getEmployees()).containsOnly(employeeBack);
        assertThat(employeeBack.getCompany()).isEqualTo(company);

        company.removeEmployee(employeeBack);
        assertThat(company.getEmployees()).doesNotContain(employeeBack);
        assertThat(employeeBack.getCompany()).isNull();

        company.employees(new HashSet<>(Set.of(employeeBack)));
        assertThat(company.getEmployees()).containsOnly(employeeBack);
        assertThat(employeeBack.getCompany()).isEqualTo(company);

        company.setEmployees(new HashSet<>());
        assertThat(company.getEmployees()).doesNotContain(employeeBack);
        assertThat(employeeBack.getCompany()).isNull();
    }
}
