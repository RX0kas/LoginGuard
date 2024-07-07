package fr.ninjagoku4560.loginguard.utils;

import fr.ninjagoku4560.loginguard.LoginGuard;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;


public class FileUtil {
    public static void createFolder(String FolderName) {

        // Créer un objet File représentant le dossier
        File directory = new File(FolderName);

        // Vérifier si le dossier existe déjà
        if (!directory.exists()) {
            // Créer le dossier
            boolean created = directory.mkdir();

            if (created) {
                LoginGuard.LOGGER.info("The folder "+FolderName+" was created");
            } else {
                LoginGuard.LOGGER.error("Creation of the folder "+FolderName+" have failed");
            }
        } else {
            LoginGuard.LOGGER.warn("The folder "+FolderName+" already exist");
        }
    }

    public static void writeToFile(String filePath, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
        } catch (IOException e) {
            LoginGuard.LOGGER.error("Error while writing the file "+filePath+ " : " + e.getMessage());
        }
    }


    public static String read(String filePath){
        File file = new File(filePath);
        if (!file.exists()) {return null;}
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        } catch (IOException e) {
            LoginGuard.LOGGER.error("Error while reading the file : "+filePath+" : " + e.getMessage());
        }

        return content.toString();
    }

    public static boolean FileNotEmpty(String fileName) {
        Path path = Paths.get(fileName);
        return !Objects.equals(read(path.toString()), "") && read(path.toString()) != null;
    }

    public static boolean fileExists(String fileName) {
        Path path = Paths.get(fileName);
        return Files.exists(path);
    }

    public static void savePlayerPosition(String fileName, double x, double y, double z) {
        String content = x + "," + y + "," + z;
        writeToFile(fileName, content);
    }

    public static double[] loadPlayerPosition(String fileName) {
        String content = read(fileName);
        if (content == null || content.isEmpty()) {
            return null;
        }
        String[] parts = content.split(",");
        if (parts.length == 3) {
            try {
                double x = Double.parseDouble(parts[0]);
                double y = Double.parseDouble(parts[1]);
                double z = Double.parseDouble(parts[2]);
                return new double[]{x, y, z};
            } catch (NumberFormatException e) {
                LoginGuard.LOGGER.error(e);
            }
        } else {
            LoginGuard.LOGGER.warn(parts.length);
        }
        return null;
    }

    public static void createEmptyFile(String fileName) {
        Path path = Paths.get(fileName);
        try {
            Files.createDirectories(path.getParent());
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
        } catch (IOException e) {
            LoginGuard.LOGGER.error(e);
        }
    }
}
