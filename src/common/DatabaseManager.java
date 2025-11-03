package common;

import java.io.*;
import java.util.*;

public class DatabaseManager {

    public static List<Entity> getDatabase(String filePath, List<Entity> outList, String entityType) {
        outList.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Entity e = switch (entityType) {
                    // wow very cool!
                    case "Student" -> new StudentEntity(line);
                    case "Staff" -> new StaffEntity(line);
                    case "CR" -> new CREntity(line);
                    case "Internship" -> new InternshipEntity(line);
                    case "Application" -> new ApplicationEntity(line);
                    case "User" -> new UserEntity(line);
                    default -> null;
                };
                if (e != null) outList.add(e);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return outList;
    }

    // Append new entry
    public static void appendEntry(String filePath, Entity entry) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
            bw.write(entry.toCSVFormat());
            bw.newLine();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Update entry by ID (first column)
    public static void updateEntry(String filePath, String id, Entity newEntry, String entityType) {
        List<Entity> list = getDatabase(filePath, new ArrayList<>(), entityType);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).values[0].equals(id)) {
                list.set(i, newEntry);
                break;
            }
        }
        rewriteFile(filePath, list);
    }
    public static Entity getEntryById(String filePath, String id, String entityType){
        List<Entity> list = getDatabase(filePath, new ArrayList<>(), entityType);
        for (Entity entity : list) {
            if (entity.values[0].equals(id)) {
                return entity;
            }
        }
        return null;
    }

    // Delete entry by ID (first column)
    public static void deleteEntry(String filePath, String id, String entityType) {
        List<Entity> list = getDatabase(filePath, new ArrayList<>(), entityType);
        List<Entity> filtered = new ArrayList<>();
        for (Entity eToDelete : list) {
            if (!eToDelete.values[0].equals(id)) {
                filtered.add(eToDelete);
            }
        }
        rewriteFile(filePath, filtered);
    }

    // Helper: rewrite the file with all entries
    private static void rewriteFile(String filePath, List<Entity> list) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, false))) {
            for (Entity e : list) {
                bw.write(e.toCSVFormat());
                bw.newLine();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
