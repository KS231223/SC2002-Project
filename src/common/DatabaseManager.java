package common;

import java.io.*;
import java.util.*;

// Interface for entity creation (Factory pattern + OCP)
/**
 * Strategy interface that knows how to hydrate a concrete {@link Entity} subtype.
 */
interface EntityFactory {
    /**
     * Builds an entity instance from a CSV line.
     *
     * @param csvLine persisted record
     * @return domain entity or {@code null} when the factory cannot parse the input
     */
    Entity createEntity(String csvLine);

    /**
     * Indicates whether this factory supports the supplied logical entity type.
     *
     * @param entityType logical entity discriminator (e.g. {@code Student})
     * @return {@code true} when the type is handled by this factory
     */
    boolean canHandle(String entityType);
}

// Concrete factory implementations
/**
 * Factory for creating {@link StudentEntity} instances.
 */
class StudentEntityFactory implements EntityFactory {
    @Override
    public Entity createEntity(String csvLine) {
        return new StudentEntity(csvLine);
    }
    @Override
    public boolean canHandle(String entityType) {
        return "Student".equals(entityType);
    }
}

/**
 * Factory for creating {@link StaffEntity} instances.
 */
class StaffEntityFactory implements EntityFactory {
    @Override
    public Entity createEntity(String csvLine) {
        return new StaffEntity(csvLine);
    }
    @Override
    public boolean canHandle(String entityType) {
        return "Staff".equals(entityType);
    }
}

/**
 * Factory for creating {@link CREntity} instances.
 */
class CREntityFactory implements EntityFactory {
    @Override
    public Entity createEntity(String csvLine) {
        return new CREntity(csvLine);
    }
    @Override
    public boolean canHandle(String entityType) {
        return "CR".equals(entityType);
    }
}

/**
 * Factory for creating {@link InternshipEntity} instances.
 */
class InternshipEntityFactory implements EntityFactory {
    @Override
    public Entity createEntity(String csvLine) {
        return new InternshipEntity(csvLine);
    }
    @Override
    public boolean canHandle(String entityType) {
        return "Internship".equals(entityType);
    }
}

/**
 * Factory for creating {@link ApplicationEntity} instances.
 */
class ApplicationEntityFactory implements EntityFactory {
    @Override
    public Entity createEntity(String csvLine) {
        return new ApplicationEntity(csvLine);
    }
    @Override
    public boolean canHandle(String entityType) {
        return "Application".equals(entityType);
    }
}

/**
 * Factory for creating {@link UserEntity} instances.
 */
class UserEntityFactory implements EntityFactory {
    @Override
    public Entity createEntity(String csvLine) {
        return new UserEntity(csvLine);
    }
    @Override
    public boolean canHandle(String entityType) {
        return "User".equals(entityType);
    }
}

// Registry for entity factories (OCP compliant)
/**
 * Maintains the list of entity factories and routes creation requests.
 */
class EntityFactoryRegistry {
    private static final List<EntityFactory> factories = new ArrayList<>();

    static {
        // Register all factories
        factories.add(new StudentEntityFactory());
        factories.add(new StaffEntityFactory());
        factories.add(new CREntityFactory());
        factories.add(new InternshipEntityFactory());
        factories.add(new ApplicationEntityFactory());
        factories.add(new UserEntityFactory());
    }

    /**
     * Creates an entity instance using the matching factory for the entity type.
     *
     * @param entityType logical entity discriminator
     * @param csvLine persisted record
     * @return hydrated entity or {@code null} when no factory supports the type
     */
    public static Entity createEntity(String entityType, String csvLine) {
        for (EntityFactory factory : factories) {
            if (factory.canHandle(entityType)) {
                return factory.createEntity(csvLine);
            }
        }
        return null;
    }

    // Allow adding new factories at runtime (OCP)
    /**
     * Registers an additional factory at runtime.
     *
     * @param factory factory to add
     */
    public static void registerFactory(EntityFactory factory) {
        factories.add(factory);
    }
}

// Interface for file operations (DIP)
/**
 * Abstraction for reading and writing raw CSV content.
 */
interface FileOperations {
    /**
     * Reads non-empty lines from the supplied file.
     *
     * @param filePath path to the CSV file
     * @return list of lines, excluding blank lines
     */
    List<String> readLines(String filePath);

    /**
     * Writes lines to the supplied file path.
     *
     * @param filePath path to write
     * @param lines lines to write
     * @param append whether to append instead of replacing
     */
    void writeLines(String filePath, List<String> lines, boolean append);
}

// Concrete implementation of file operations (SRP)
/**
 * Default implementation backed by {@link FileReader}/{@link FileWriter}.
 */
class StandardFileOperations implements FileOperations {
    @Override
    public List<String> readLines(String filePath) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lines.add(line);
                }
            }
        } catch (IOException ex) {
            System.err.println("Failed to read file " + filePath + ": " + ex.getMessage());
        }
        return lines;
    }

    @Override
    public void writeLines(String filePath, List<String> lines, boolean append) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, append))) {
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException ex) {
            System.err.println("Failed to write file " + filePath + ": " + ex.getMessage());
        }
    }
}

// Repository for entity operations (SRP)
/**
 * Encapsulates file-backed CRUD operations for a specific CSV resource.
 */
class EntityRepository {
    private final FileOperations fileOps;

    /**
     * Creates a repository using the provided file operations strategy.
     */
    public EntityRepository(FileOperations fileOps) {
        this.fileOps = fileOps;
    }

    /**
     * Loads entities from a CSV file into domain objects.
     */
    public List<Entity> loadEntities(String filePath, String entityType) {
        List<Entity> entities = new ArrayList<>();
        List<String> lines = fileOps.readLines(filePath);

        for (String line : lines) {
            Entity entity = EntityFactoryRegistry.createEntity(entityType, line);
            if (entity != null) {
                entities.add(entity);
            }
        }
        return entities;
    }

    /**
     * Writes the supplied entities to disk, replacing existing contents.
     */
    public void saveEntities(String filePath, List<Entity> entities) {
        List<String> lines = new ArrayList<>();
        for (Entity entity : entities) {
            lines.add(entity.toCSVFormat());
        }
        fileOps.writeLines(filePath, lines, false);
    }

    /**
     * Appends a single entity to the backing file.
     */
    public void appendEntity(String filePath, Entity entity) {
        List<String> lines = Collections.singletonList(entity.toCSVFormat());
        fileOps.writeLines(filePath, lines, true);
    }

    /**
     * Searches the in-memory list for an entity with the requested identifier.
     */
    public Entity findById(List<Entity> entities, String id) {
        for (Entity entity : entities) {
            if (entity.values[0].equals(id)) {
                return entity;
            }
        }
        return null;
    }

    /**
     * Produces a new list excluding the entity with the given identifier.
     */
    public List<Entity> removeById(List<Entity> entities, String id) {
        List<Entity> filtered = new ArrayList<>();
        for (Entity entity : entities) {
            if (!entity.values[0].equals(id)) {
                filtered.add(entity);
            }
        }
        return filtered;
    }

    /**
     * Replaces the entity with the matching identifier.
     */
    public List<Entity> replaceById(List<Entity> entities, String id, Entity newEntity) {
        for (int i = 0; i < entities.size(); i++) {
            if (entities.get(i).values[0].equals(id)) {
                entities.set(i, newEntity);
                break;
            }
        }
        return entities;
    }
}

// Main DatabaseManager
/**
 * Facade providing CSV-backed persistence operations for domain entities.
 */
public class DatabaseManager {
    private static final EntityRepository repository =
            new EntityRepository(new StandardFileOperations());

    /**
     * Loads the requested database file into the supplied list.
     *
     * @param filePath CSV resource path
     * @param outList list to populate (cleared before population)
     * @param entityType logical entity discriminator
     * @return reference to {@code outList}
     */
    public static List<Entity> getDatabase(String filePath, List<Entity> outList, String entityType) {
        outList.clear();
        List<Entity> entities = repository.loadEntities(filePath, entityType);
        outList.addAll(entities);
        return outList;
    }

    /**
     * Appends a new entry to the backing CSV file.
     */
    public static void appendEntry(String filePath, Entity entry) {
        repository.appendEntity(filePath, entry);
    }

    /**
     * Updates the identified entry with the provided replacement entity.
     */
    public static void updateEntry(String filePath, String id, Entity newEntry, String entityType) {
        List<Entity> list = repository.loadEntities(filePath, entityType);
        list = repository.replaceById(list, id, newEntry);
        repository.saveEntities(filePath, list);
    }

    /**
     * Retrieves a single entry by identifier.
     */
    public static Entity getEntryById(String filePath, String id, String entityType) {
        List<Entity> list = repository.loadEntities(filePath, entityType);
        return repository.findById(list, id);
    }

    /**
     * Removes the matching entry from the backing file.
     */
    public static void deleteEntry(String filePath, String id, String entityType) {
        List<Entity> list = repository.loadEntities(filePath, entityType);
        list = repository.removeById(list, id);
        repository.saveEntities(filePath, list);
    }
}
