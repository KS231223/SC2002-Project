package common;

import java.io.*;
import java.util.*;

// Interface for entity creation (Factory pattern + OCP)
interface EntityFactory {
    Entity createEntity(String csvLine);
    boolean canHandle(String entityType);
}

// Concrete factory implementations
class StudentEntityFactory implements EntityFactory {
    public Entity createEntity(String csvLine) {
        return new StudentEntity(csvLine);
    }
    public boolean canHandle(String entityType) {
        return "Student".equals(entityType);
    }
}

class StaffEntityFactory implements EntityFactory {
    public Entity createEntity(String csvLine) {
        return new StaffEntity(csvLine);
    }
    public boolean canHandle(String entityType) {
        return "Staff".equals(entityType);
    }
}

class CREntityFactory implements EntityFactory {
    public Entity createEntity(String csvLine) {
        return new CREntity(csvLine);
    }
    public boolean canHandle(String entityType) {
        return "CR".equals(entityType);
    }
}

class InternshipEntityFactory implements EntityFactory {
    public Entity createEntity(String csvLine) {
        return new InternshipEntity(csvLine);
    }
    public boolean canHandle(String entityType) {
        return "Internship".equals(entityType);
    }
}

class ApplicationEntityFactory implements EntityFactory {
    public Entity createEntity(String csvLine) {
        return new ApplicationEntity(csvLine);
    }
    public boolean canHandle(String entityType) {
        return "Application".equals(entityType);
    }
}

class UserEntityFactory implements EntityFactory {
    public Entity createEntity(String csvLine) {
        return new UserEntity(csvLine);
    }
    public boolean canHandle(String entityType) {
        return "User".equals(entityType);
    }
}

// Registry for entity factories (OCP compliant)
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

    public static Entity createEntity(String entityType, String csvLine) {
        for (EntityFactory factory : factories) {
            if (factory.canHandle(entityType)) {
                return factory.createEntity(csvLine);
            }
        }
        return null;
    }

    // Allow adding new factories at runtime (OCP)
    public static void registerFactory(EntityFactory factory) {
        factories.add(factory);
    }
}

// Interface for file operations (DIP)
interface FileOperations {
    List<String> readLines(String filePath);
    void writeLines(String filePath, List<String> lines, boolean append);
}

// Concrete implementation of file operations (SRP)
class StandardFileOperations implements FileOperations {
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
            ex.printStackTrace();
        }
        return lines;
    }

    public void writeLines(String filePath, List<String> lines, boolean append) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, append))) {
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

// Repository for entity operations (SRP)
class EntityRepository {
    private final FileOperations fileOps;

    public EntityRepository(FileOperations fileOps) {
        this.fileOps = fileOps;
    }

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

    public void saveEntities(String filePath, List<Entity> entities) {
        List<String> lines = new ArrayList<>();
        for (Entity entity : entities) {
            lines.add(entity.toCSVFormat());
        }
        fileOps.writeLines(filePath, lines, false);
    }

    public void appendEntity(String filePath, Entity entity) {
        List<String> lines = Collections.singletonList(entity.toCSVFormat());
        fileOps.writeLines(filePath, lines, true);
    }

    public Entity findById(List<Entity> entities, String id) {
        for (Entity entity : entities) {
            if (entity.values[0].equals(id)) {
                return entity;
            }
        }
        return null;
    }

    public List<Entity> removeById(List<Entity> entities, String id) {
        List<Entity> filtered = new ArrayList<>();
        for (Entity entity : entities) {
            if (!entity.values[0].equals(id)) {
                filtered.add(entity);
            }
        }
        return filtered;
    }

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
public class DatabaseManager {
    private static final EntityRepository repository =
            new EntityRepository(new StandardFileOperations());

    public static List<Entity> getDatabase(String filePath, List<Entity> outList, String entityType) {
        outList.clear();
        List<Entity> entities = repository.loadEntities(filePath, entityType);
        outList.addAll(entities);
        return outList;
    }

    public static void appendEntry(String filePath, Entity entry) {
        repository.appendEntity(filePath, entry);
    }

    public static void updateEntry(String filePath, String id, Entity newEntry, String entityType) {
        List<Entity> list = repository.loadEntities(filePath, entityType);
        list = repository.replaceById(list, id, newEntry);
        repository.saveEntities(filePath, list);
    }

    public static Entity getEntryById(String filePath, String id, String entityType) {
        List<Entity> list = repository.loadEntities(filePath, entityType);
        return repository.findById(list, id);
    }

    public static void deleteEntry(String filePath, String id, String entityType) {
        List<Entity> list = repository.loadEntities(filePath, entityType);
        list = repository.removeById(list, id);
        repository.saveEntities(filePath, list);
    }
}
