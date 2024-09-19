package com.crud.domain;

import static com.crud.domain.ManagerTestSamples.*;
import static com.crud.domain.WorkersTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.crud.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ManagerTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Manager.class);
        Manager manager1 = getManagerSample1();
        Manager manager2 = new Manager();
        assertThat(manager1).isNotEqualTo(manager2);

        manager2.setId(manager1.getId());
        assertThat(manager1).isEqualTo(manager2);

        manager2 = getManagerSample2();
        assertThat(manager1).isNotEqualTo(manager2);
    }

    @Test
    void idTest() {
        Manager manager = getManagerRandomSampleGenerator();
        Workers workersBack = getWorkersRandomSampleGenerator();

        manager.addId(workersBack);
        assertThat(manager.getIds()).containsOnly(workersBack);
        assertThat(workersBack.getManager()).isEqualTo(manager);

        manager.removeId(workersBack);
        assertThat(manager.getIds()).doesNotContain(workersBack);
        assertThat(workersBack.getManager()).isNull();

        manager.ids(new HashSet<>(Set.of(workersBack)));
        assertThat(manager.getIds()).containsOnly(workersBack);
        assertThat(workersBack.getManager()).isEqualTo(manager);

        manager.setIds(new HashSet<>());
        assertThat(manager.getIds()).doesNotContain(workersBack);
        assertThat(workersBack.getManager()).isNull();
    }
}
