package com.crud.web.rest;

import static com.crud.domain.ManagerAsserts.*;
import static com.crud.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.crud.IntegrationTest;
import com.crud.domain.Manager;
import com.crud.repository.ManagerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.util.UUID;
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
 * Integration tests for the {@link ManagerResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ManagerResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final Integer DEFAULT_AGE = 20;
    private static final Integer UPDATED_AGE = 21;

    private static final String DEFAULT_GENDER = "AAAAAAAAAA";
    private static final String UPDATED_GENDER = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/managers";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ManagerRepository managerRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restManagerMockMvc;

    private Manager manager;

    private Manager insertedManager;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Manager createEntity() {
        return new Manager().name(DEFAULT_NAME).age(DEFAULT_AGE).gender(DEFAULT_GENDER);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Manager createUpdatedEntity() {
        return new Manager().name(UPDATED_NAME).age(UPDATED_AGE).gender(UPDATED_GENDER);
    }

    @BeforeEach
    public void initTest() {
        manager = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedManager != null) {
            managerRepository.delete(insertedManager);
            insertedManager = null;
        }
    }

    @Test
    @Transactional
    void createManager() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Manager
        var returnedManager = om.readValue(
            restManagerMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(manager)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            Manager.class
        );

        // Validate the Manager in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertManagerUpdatableFieldsEquals(returnedManager, getPersistedManager(returnedManager));

        insertedManager = returnedManager;
    }

    @Test
    @Transactional
    void createManagerWithExistingId() throws Exception {
        // Create the Manager with an existing ID
        insertedManager = managerRepository.saveAndFlush(manager);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restManagerMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(manager)))
            .andExpect(status().isBadRequest());

        // Validate the Manager in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkGenderIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        manager.setGender(null);

        // Create the Manager, which fails.

        restManagerMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(manager)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllManagers() throws Exception {
        // Initialize the database
        insertedManager = managerRepository.saveAndFlush(manager);

        // Get all the managerList
        restManagerMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(manager.getId().toString())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].age").value(hasItem(DEFAULT_AGE)))
            .andExpect(jsonPath("$.[*].gender").value(hasItem(DEFAULT_GENDER)));
    }

    @Test
    @Transactional
    void getManager() throws Exception {
        // Initialize the database
        insertedManager = managerRepository.saveAndFlush(manager);

        // Get the manager
        restManagerMockMvc
            .perform(get(ENTITY_API_URL_ID, manager.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(manager.getId().toString()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.age").value(DEFAULT_AGE))
            .andExpect(jsonPath("$.gender").value(DEFAULT_GENDER));
    }

    @Test
    @Transactional
    void getNonExistingManager() throws Exception {
        // Get the manager
        restManagerMockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString())).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingManager() throws Exception {
        // Initialize the database
        insertedManager = managerRepository.saveAndFlush(manager);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the manager
        Manager updatedManager = managerRepository.findById(manager.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedManager are not directly saved in db
        em.detach(updatedManager);
        updatedManager.name(UPDATED_NAME).age(UPDATED_AGE).gender(UPDATED_GENDER);

        restManagerMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedManager.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedManager))
            )
            .andExpect(status().isOk());

        // Validate the Manager in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedManagerToMatchAllProperties(updatedManager);
    }

    @Test
    @Transactional
    void putNonExistingManager() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        manager.setId(UUID.randomUUID());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restManagerMockMvc
            .perform(put(ENTITY_API_URL_ID, manager.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(manager)))
            .andExpect(status().isBadRequest());

        // Validate the Manager in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchManager() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        manager.setId(UUID.randomUUID());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restManagerMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(manager))
            )
            .andExpect(status().isBadRequest());

        // Validate the Manager in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamManager() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        manager.setId(UUID.randomUUID());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restManagerMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(manager)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Manager in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateManagerWithPatch() throws Exception {
        // Initialize the database
        insertedManager = managerRepository.saveAndFlush(manager);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the manager using partial update
        Manager partialUpdatedManager = new Manager();
        partialUpdatedManager.setId(manager.getId());

        partialUpdatedManager.age(UPDATED_AGE);

        restManagerMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedManager.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedManager))
            )
            .andExpect(status().isOk());

        // Validate the Manager in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertManagerUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedManager, manager), getPersistedManager(manager));
    }

    @Test
    @Transactional
    void fullUpdateManagerWithPatch() throws Exception {
        // Initialize the database
        insertedManager = managerRepository.saveAndFlush(manager);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the manager using partial update
        Manager partialUpdatedManager = new Manager();
        partialUpdatedManager.setId(manager.getId());

        partialUpdatedManager.name(UPDATED_NAME).age(UPDATED_AGE).gender(UPDATED_GENDER);

        restManagerMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedManager.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedManager))
            )
            .andExpect(status().isOk());

        // Validate the Manager in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertManagerUpdatableFieldsEquals(partialUpdatedManager, getPersistedManager(partialUpdatedManager));
    }

    @Test
    @Transactional
    void patchNonExistingManager() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        manager.setId(UUID.randomUUID());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restManagerMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, manager.getId()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(manager))
            )
            .andExpect(status().isBadRequest());

        // Validate the Manager in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchManager() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        manager.setId(UUID.randomUUID());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restManagerMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(manager))
            )
            .andExpect(status().isBadRequest());

        // Validate the Manager in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamManager() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        manager.setId(UUID.randomUUID());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restManagerMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(manager)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Manager in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteManager() throws Exception {
        // Initialize the database
        insertedManager = managerRepository.saveAndFlush(manager);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the manager
        restManagerMockMvc
            .perform(delete(ENTITY_API_URL_ID, manager.getId().toString()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return managerRepository.count();
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

    protected Manager getPersistedManager(Manager manager) {
        return managerRepository.findById(manager.getId()).orElseThrow();
    }

    protected void assertPersistedManagerToMatchAllProperties(Manager expectedManager) {
        assertManagerAllPropertiesEquals(expectedManager, getPersistedManager(expectedManager));
    }

    protected void assertPersistedManagerToMatchUpdatableProperties(Manager expectedManager) {
        assertManagerAllUpdatablePropertiesEquals(expectedManager, getPersistedManager(expectedManager));
    }
}
