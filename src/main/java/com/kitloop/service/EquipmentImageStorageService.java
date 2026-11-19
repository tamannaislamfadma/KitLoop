package com.kitloop.service;

import com.kitloop.exception.FileStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class EquipmentImageStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of(
                    "image/jpeg",
                    "image/png"
            );

    private static final Pattern SAFE_FILENAME_PATTERN =
            Pattern.compile(
                    "^[a-f0-9\\-]{36}\\.(jpg|png)$"
            );

    private final Path equipmentDirectory;

    private final long maximumFileSize;

    public EquipmentImageStorageService(
            @Value("${kitloop.upload.equipment-directory}")
            String equipmentDirectory,
            @Value("${kitloop.upload.equipment-max-bytes:5242880}")
            long maximumFileSize
    ) {

        this.equipmentDirectory =
                Paths.get(equipmentDirectory)
                        .toAbsolutePath()
                        .normalize();

        this.maximumFileSize =
                maximumFileSize;

        createStorageDirectory();
    }

    public String storeEquipmentImage(
            MultipartFile file
    ) {

        validateFile(file);

        String contentType =
                file.getContentType()
                        .toLowerCase(Locale.ROOT);

        String extension =
                contentType.equals("image/png")
                        ? ".png"
                        : ".jpg";

        String generatedFilename =
                UUID.randomUUID()
                        + extension;

        Path destination =
                equipmentDirectory
                        .resolve(generatedFilename)
                        .normalize();

        if (!destination.startsWith(
                equipmentDirectory
        )) {
            throw new FileStorageException(
                    "Invalid image storage path."
            );
        }

        try (
                InputStream inputStream =
                        file.getInputStream()
        ) {

            Files.copy(
                    inputStream,
                    destination,
                    StandardCopyOption.REPLACE_EXISTING
            );

        } catch (IOException exception) {

            throw new FileStorageException(
                    "KitLoop could not store the equipment image.",
                    exception
            );
        }

        return generatedFilename;
    }

    public Resource loadEquipmentImage(
            String filename
    ) {

        validateGeneratedFilename(filename);

        Path imagePath =
                equipmentDirectory
                        .resolve(filename)
                        .normalize();

        if (!imagePath.startsWith(
                equipmentDirectory
        )) {
            throw new FileStorageException(
                    "Invalid equipment image path."
            );
        }

        try {

            Resource resource =
                    new UrlResource(
                            imagePath.toUri()
                    );

            if (!resource.exists()
                    || !resource.isReadable()) {

                throw new FileStorageException(
                        "Equipment image was not found."
                );
            }

            return resource;

        } catch (
                MalformedURLException exception
        ) {

            throw new FileStorageException(
                    "Equipment image could not be loaded.",
                    exception
            );
        }
    }

    public String determineContentType(
            String filename
    ) {

        validateGeneratedFilename(filename);

        String normalized =
                filename.toLowerCase(
                        Locale.ROOT
                );

        if (normalized.endsWith(".png")) {
            return "image/png";
        }

        return "image/jpeg";
    }

    public void deleteEquipmentImage(
            String filename
    ) {

        if (filename == null
                || filename.isBlank()) {
            return;
        }

        validateGeneratedFilename(filename);

        Path imagePath =
                equipmentDirectory
                        .resolve(filename)
                        .normalize();

        if (!imagePath.startsWith(
                equipmentDirectory
        )) {
            return;
        }

        try {

            Files.deleteIfExists(
                    imagePath
            );

        } catch (IOException ignored) {
        }
    }

    private void validateFile(
            MultipartFile file
    ) {

        if (file == null
                || file.isEmpty()) {

            throw new FileStorageException(
                    "Equipment picture is required."
            );
        }

        if (file.getSize()
                > maximumFileSize) {

            throw new FileStorageException(
                    "Equipment picture must be 5 MB or smaller."
            );
        }

        String contentType =
                file.getContentType();

        if (contentType == null) {

            throw new FileStorageException(
                    "Unable to determine the image type."
            );
        }

        String normalizedType =
                contentType.toLowerCase(
                        Locale.ROOT
                );

        if (!ALLOWED_CONTENT_TYPES.contains(
                normalizedType
        )) {

            throw new FileStorageException(
                    "Only JPG and PNG equipment pictures are allowed."
            );
        }

        try (
                InputStream inputStream =
                        file.getInputStream()
        ) {

            BufferedImage image =
                    ImageIO.read(inputStream);

            if (image == null
                    || image.getWidth() <= 0
                    || image.getHeight() <= 0) {

                throw new FileStorageException(
                        "The uploaded file is not a valid image."
                );
            }

        } catch (IOException exception) {

            throw new FileStorageException(
                    "KitLoop could not validate the uploaded image.",
                    exception
            );
        }
    }

    private void validateGeneratedFilename(
            String filename
    ) {

        if (filename == null
                || !SAFE_FILENAME_PATTERN
                .matcher(filename)
                .matches()) {

            throw new FileStorageException(
                    "Invalid equipment image filename."
            );
        }
    }

    private void createStorageDirectory() {

        try {

            Files.createDirectories(
                    equipmentDirectory
            );

        } catch (IOException exception) {

            throw new FileStorageException(
                    "KitLoop could not create the equipment upload directory.",
                    exception
            );
        }
    }
}