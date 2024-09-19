package com.crud.repository;

import com.crud.domain.Workers;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Workers entity.
 */
@SuppressWarnings("unused")
@Repository
public interface WorkersRepository extends JpaRepository<Workers, Long> {}
