package com.atlashub.shared.domain.repository;

import java.util.Optional;

/**
 * Generic domain repository interface.
 * To be implemented by infrastructure adapters (e.g., Spring Data JPA).
 */
public interface Repository<T> {
    Long nextIdentity();
    T save(T entity);
    Optional<T> findById(Long id);
    void deleteById(Long id);
    boolean existsById(Long id);
}
