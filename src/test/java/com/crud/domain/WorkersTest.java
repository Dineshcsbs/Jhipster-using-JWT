package com.crud.domain;

import static com.crud.domain.ManagerTestSamples.*;
import static com.crud.domain.WorkersTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.crud.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class WorkersTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Workers.class);
        Workers workers1 = getWorkersSample1();
        Workers workers2 = new Workers();
        assertThat(workers1).isNotEqualTo(workers2);

        workers2.setId(workers1.getId());
        assertThat(workers1).isEqualTo(workers2);

        workers2 = getWorkersSample2();
        assertThat(workers1).isNotEqualTo(workers2);
    }

    @Test
    void managerTest() {
        Workers workers = getWorkersRandomSampleGenerator();
        Manager managerBack = getManagerRandomSampleGenerator();

        workers.setManager(managerBack);
        assertThat(workers.getManager()).isEqualTo(managerBack);

        workers.manager(null);
        assertThat(workers.getManager()).isNull();
    }
}
