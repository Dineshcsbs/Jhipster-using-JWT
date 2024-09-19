package com.crud.web.rest;

import com.crud.domain.Workers;
import com.crud.repository.WorkersRepository;
import com.crud.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.crud.domain.Workers}.
 */
@RestController
@RequestMapping("/api/workers")
@Transactional
public class WorkersResource {

    private static final Logger LOG = LoggerFactory.getLogger(WorkersResource.class);

    private static final String ENTITY_NAME = "workers";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final WorkersRepository workersRepository;

    public WorkersResource(WorkersRepository workersRepository) {
        this.workersRepository = workersRepository;
    }

    /**
     * {@code POST  /workers} : Create a new workers.
     *
     * @param workers the workers to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new workers, or with status {@code 400 (Bad Request)} if the workers has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<Workers> createWorkers(@RequestBody Workers workers) throws URISyntaxException {
        LOG.debug("REST request to save Workers : {}", workers);
        if (workers.getId() != null) {
            throw new BadRequestAlertException("A new workers cannot already have an ID", ENTITY_NAME, "idexists");
        }
        workers = workersRepository.save(workers);
        return ResponseEntity.created(new URI("/api/workers/" + workers.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, workers.getId().toString()))
            .body(workers);
    }

    /**
     * {@code PUT  /workers/:id} : Updates an existing workers.
     *
     * @param id the id of the workers to save.
     * @param workers the workers to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated workers,
     * or with status {@code 400 (Bad Request)} if the workers is not valid,
     * or with status {@code 500 (Internal Server Error)} if the workers couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Workers> updateWorkers(@PathVariable(value = "id", required = false) final Long id, @RequestBody Workers workers)
        throws URISyntaxException {
        LOG.debug("REST request to update Workers : {}, {}", id, workers);
        if (workers.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, workers.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!workersRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        workers = workersRepository.save(workers);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, workers.getId().toString()))
            .body(workers);
    }

    /**
     * {@code PATCH  /workers/:id} : Partial updates given fields of an existing workers, field will ignore if it is null
     *
     * @param id the id of the workers to save.
     * @param workers the workers to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated workers,
     * or with status {@code 400 (Bad Request)} if the workers is not valid,
     * or with status {@code 404 (Not Found)} if the workers is not found,
     * or with status {@code 500 (Internal Server Error)} if the workers couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Workers> partialUpdateWorkers(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Workers workers
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Workers partially : {}, {}", id, workers);
        if (workers.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, workers.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!workersRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Workers> result = workersRepository
            .findById(workers.getId())
            .map(existingWorkers -> {
                if (workers.getName() != null) {
                    existingWorkers.setName(workers.getName());
                }
                if (workers.getAge() != null) {
                    existingWorkers.setAge(workers.getAge());
                }

                return existingWorkers;
            })
            .map(workersRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, workers.getId().toString())
        );
    }

    /**
     * {@code GET  /workers} : get all the workers.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of workers in body.
     */
    @GetMapping("")
    public List<Workers> getAllWorkers() {
        LOG.debug("REST request to get all Workers");
        return workersRepository.findAll();
    }

    /**
     * {@code GET  /workers/:id} : get the "id" workers.
     *
     * @param id the id of the workers to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the workers, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Workers> getWorkers(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Workers : {}", id);
        Optional<Workers> workers = workersRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(workers);
    }

    /**
     * {@code DELETE  /workers/:id} : delete the "id" workers.
     *
     * @param id the id of the workers to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkers(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Workers : {}", id);
        workersRepository.deleteById(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
