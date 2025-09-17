package server.database;

import org.springframework.data.jpa.repository.JpaRepository;

import commons.Collection;

import java.util.Optional;

public interface CollectionRepository extends JpaRepository<Collection, Long>
{
    /**
     * Check if a collection with the given title
     * and a different ID from the given one exists.
     * @param title The title to check.
     * @param id The ID to exclude.
     * @return True if a collection with the given title exists, otherwise false.
     */
    boolean existsByTitleAndIdNot(String title, long id);

    /**
     * Find collection by title if exists
     * @param title the title to search by
     * @return Optional which contains nothing or found collection
     */
    Optional<Collection> findByTitle(String title);
}
