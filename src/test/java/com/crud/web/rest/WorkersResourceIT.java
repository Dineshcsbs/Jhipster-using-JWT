package com.crud.web.rest;

import static com.crud.domain.WorkersAsserts.*;
import static com.crud.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.crud.IntegrationTest;
import com.crud.domain.Workers;
import com.crud.repository.WorkersRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link WorkersResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class WorkersResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final Integer DEFAULT_AGE = 1;
    private static final Integer UPDATED_AGE = 2;

    private static final String ENTITY_API_URL = "/api/workers";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private WorkersRepository workersRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restWorkersMockMvc;

    private Workers workers;

    private Workers insertedWorkers;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Workers createEntity() {
        return new Workers().name(DEFAULT_NAME).age(DEFAULT_AGE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Workers createUpdatedEntity() {
        return new Workers().name(UPDATED_NAME).age(UPDATED_AGE);
    }

    @BeforeEach
    public void initTest() {
        workers = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedWorkers != null) {
            workersRepository.delete(insertedWorkers);
            insertedWorkers = null;
        }
    }

    @Test
    @Transactional
    void createWorkers() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Workers
        var returnedWorkers = om.readValue(
            restWorkersMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(workers)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            Workers.class
        );

        // Validate the Workers in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertWorkersUpdatableFieldsEquals(returnedWorkers, getPersistedWorkers(returnedWorkers));

        insertedWorkers = returnedWorkers;
    }

    @Test
    @Transactional
    void createWorkersWithExistingId() throws Exception {
        // Create the Workers with an existing ID
        workers.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restWorkersMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(workers)))
            .andExpect(status().isBadRequest());

        // Validate the Workers in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllWorkers() throws Exception {
        // Initialize the database
        insertedWorkers = workersRepository.saveAndFlush(workers);

        // Get all the workersList
        restWorkersMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(workers.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].age").value(hasItem(DEFAULT_AGE)));
    }

    @Test
    @Transactional
    void getWorkers() throws Exception {
        // Initialize the database
        insertedWorkers = workersRepository.saveAndFlush(workers);

        // Get the workers
        restWorkersMockMvc
            .perform(get(ENTITY_API_URL_ID, workers.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(workers.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.age").value(DEFAULT_AGE));
    }

    @Test
    @Transactional
    void getNonExistingWorkers() throws Exception {
        // Get the workers
        restWorkersMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingWorkers() throws Exception {
        // Initialize the database
        insertedWorkers = workersRepository.saveAndFlush(workers);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the workers
        Workers updatedWorkers = workersRepository.findById(workers.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedWorkers are not directly saved in db
        em.detach(updatedWorkers);
        updatedWorkers.name(UPDATED_NAME).age(UPDATED_AGE);

        restWorkersMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedWorkers.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedWorkers))
            )
            .andExpect(status().isOk());

        // Validate the Workers in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedWorkersToMatchAllProperties(updatedWorkers);
    }

    @Test
    @Transactional
    void putNonExistingWorkers() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        workers.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restWorkersMockMvc
            .perform(put(ENTITY_API_URL_ID, workers.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(workers)))
            .andExpect(status().isBadRequest());

        // Validate the Workers in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchWorkers() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        workers.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWorkersMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(workers))
            )
            .andExpect(status().isBadRequest());

        // Validate the Workers in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamWorkers() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        workers.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWorkersMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(workers)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Workers in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateWorkersWithPatch() throws Exception {
        // Initialize the database
        insertedWorkers = workersRepository.saveAndFlush(workers);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the workers using partial update
        Workers partialUpdatedWorkers = new Workers();
        partialUpdatedWorkers.setId(workers.getId());

        partialUpdatedWorkers.name(UPDATED_NAME);

        restWorkersMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedWorkers.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedWorkers))
            )
            .andExpect(status().isOk());

        // Validate the Workers in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertWorkersUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedWorkers, workers), getPersistedWorkers(workers));
    }

    @Test
    @Transactional
    void fullUpdateWorkersWithPatch() throws Exception {
        // Initialize the database
        insertedWorkers = workersRepository.saveAndFlush(workers);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the workers using partial update
        Workers partialUpdatedWorkers = new Workers();
        partialUpdatedWorkers.setId(workers.getId());

        partialUpdatedWorkers.name(UPDATED_NAME).age(UPDATED_AGE);

        restWorkersMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedWorkers.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedWorkers))
            )
            .andExpect(status().isOk());

        // Validate the Workers in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertWorkersUpdatableFieldsEquals(partialUpdatedWorkers, getPersistedWorkers(partialUpdatedWorkers));
    }

    @Test
    @Transactional
    void patchNonExistingWorkers() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        workers.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restWorkersMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, workers.getId()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(workers))
            )
            .andExpect(status().isBadRequest());

        // Validate the Workers in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchWorkers() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        workers.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWorkersMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(workers))
            )
            .andExpect(status().isBadRequest());

        // Validate the Workers in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamWorkers() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        workers.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restWorkersMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(workers)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Workers in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteWorkers() throws Exception {
        // Initialize the database
        insertedWorkers = workersRepository.saveAndFlush(workers);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the workers
        restWorkersMockMvc
            .perform(delete(ENTITY_API_URL_ID, workers.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return workersRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Workers getPersistedWorkers(Workers workers) {
        return workersRepository.findById(workers.getId()).orElseThrow();
    }

    protected void assertPersistedWorkersToMatchAllProperties(Workers expectedWorkers) {
        assertWorkersAllPropertiesEquals(expectedWorkers, getPersistedWorkers(expectedWorkers));
    }

    protected void assertPersistedWorkersToMatchUpdatableProperties(Workers expectedWorkers) {
        assertWorkersAllUpdatablePropertiesEquals(expectedWorkers, getPersistedWorkers(expectedWorkers));
    }
}
