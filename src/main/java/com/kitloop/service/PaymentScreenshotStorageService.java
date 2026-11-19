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
public class PaymentScreenshotStorageService {

    private static final Set<String>
            ALLOWED_CONTENT_TYPES =
            Set.of(
                    "image/jpeg",
                    "image/png"
            );

    private static final Pattern
            SAFE_FILENAME_PATTERN =
            Pattern.compile(
                    "^[a-f0-9\\-]{36}\\.(jpg|png)$"
            );

    private final Path paymentDirectory;
    private final long maximumFileSize;

    public PaymentScreenshotStorageService(
            @Value("${kitloop.upload.payment-directory}")
            String paymentDirectory,
            @Value("${kitloop.upload.payment-max-bytes:5242880}")
            long maximumFileSize
    ) {

        this.paymentDirectory =
                Paths.get(paymentDirectory)
                        .toAbsolutePath()
                        .normalize();

        this.maximumFileSize =
                maximumFileSize;

        createStorageDirectory();
    }

    public String store(
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

        String filename =
                UUID.randomUUID()
                        + extension;

        Path destination =
                paymentDirectory
                        .resolve(filename)
                        .normalize();

        if (!destination.startsWith(
                paymentDirectory
        )) {
            throw new FileStorageException(
                    "Invalid payment screenshot path."
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
                    "KitLoop could not store the payment screenshot.",
                    exception
            );
        }

        return filename;
    }

    public Resource load(
            String filename
    ) {

        validateFilename(filename);

        Path filePath =
                paymentDirectory
                        .resolve(filename)
                        .normalize();

        if (!filePath.startsWith(
                paymentDirectory
        )) {
            throw new FileStorageException(
                    "Invalid payment screenshot path."
            );
        }

        try {

            Resource resource =
                    new UrlResource(
                            filePath.toUri()
                    );

            if (!resource.exists()
                    || !resource.isReadable()) {

                throw new FileStorageException(
                        "Payment screenshot was not found."
                );
            }

            return resource;

        } catch (
                MalformedURLException exception
        ) {

            throw new FileStorageException(
                    "Payment screenshot could not be loaded.",
                    exception
            );
        }
    }

    public String determineContentType(
            String filename
    ) {

        validateFilename(filename);

        if (filename
                .toLowerCase(Locale.ROOT)
                .endsWith(".png")) {

            return "image/png";
        }

        return "image/jpeg";
    }

    public void delete(
            String filename
    ) {

        if (filename == null
                || filename.isBlank()) {
            return;
        }

        validateFilename(filename);

        try {

            Files.deleteIfExists(
                    paymentDirectory
                            .resolve(filename)
                            .normalize()
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
                    "Payment screenshot is required."
            );
        }

        if (file.getSize()
                > maximumFileSize) {

            throw new FileStorageException(
                    "Payment screenshot must be 5 MB or smaller."
            );
        }

        String contentType =
                file.getContentType();

        if (contentType == null
                || !ALLOWED_CONTENT_TYPES
                .contains(
                        contentType.toLowerCase(
                                Locale.ROOT
                        )
                )) {

            throw new FileStorageException(
                    "Only JPG and PNG payment screenshots are allowed."
            );
        }

        try (
                InputStream inputStream =
                        file.getInputStream()
        ) {

            BufferedImage image =
                    ImageIO.read(
                            inputStream
                    );

            if (image == null
                    || image.getWidth() <= 0
                    || image.getHeight() <= 0) {

                throw new FileStorageException(
                        "The uploaded payment proof is not a valid image."
                );
            }

        } catch (IOException exception) {

            throw new FileStorageException(
                    "KitLoop could not validate the payment screenshot.",
                    exception
            );
        }
    }

    private void validateFilename(
            String filename
    ) {

        if (filename == null
                || !SAFE_FILENAME_PATTERN
                .matcher(filename)
                .matches()) {

            throw new FileStorageException(
                    "Invalid payment screenshot filename."
            );
        }
    }

    private void createStorageDirectory() {

        try {

            Files.createDirectories(
                    paymentDirectory
            );

        } catch (IOException exception) {

            throw new FileStorageException(
                    "KitLoop could not create the payment screenshot directory.",
                    exception
            );
        }
    }
}