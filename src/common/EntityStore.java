package common;

import java.util.List;

/**
 * Minimal persistence abstraction that hides the concrete CSV-backed storage from high-level code.
 */
public interface EntityStore {
    /**
     * Loads all entities contained in the target resource.
     *
     * @param filePath resource resolved via {@link PathResolver#resource(String)}
     * @param entityType logical discriminator used by {@link EntityFactoryRegistry}
     * @return list of hydrated entities (may be empty when the file is missing)
     */
    List<Entity> loadAll(String filePath, String entityType);

    /**
     * Finds the entity identified by {@code id} inside the supplied resource.
     */
    Entity findById(String filePath, String id, String entityType);

    /**
     * Persists a new entity by appending it to the backing resource.
     */
    void append(String filePath, Entity entity);

    /**
     * Replaces the entity that matches {@code id} inside the backing resource.
     */
    void update(String filePath, String id, Entity entity, String entityType);

    /**
     * Removes the entity identified by {@code id} from the backing resource.
     */
    void delete(String filePath, String id, String entityType);
}
