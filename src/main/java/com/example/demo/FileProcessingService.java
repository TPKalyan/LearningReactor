package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class FileProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(FileProcessingService.class);
    private final DataRepository dataRepository;

    @Autowired
    public FileProcessingService(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    public Flux<TargetAudience> processFile(String fileUrl) {
        return Flux.defer(() -> downloadFile(fileUrl))
                .flatMap(fileLine -> saveToDatabase(fileUrl, fileLine))
                .doOnError(throwable -> logger.error("Failed to fetch the file", throwable))
                .doOnComplete(() -> logger.info("Finished processing"))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Flux<UUID> downloadFile(String fileUrl) {
        logger.info("Creating flux for {}", fileUrl);
        return Flux.create(sink -> {
            try {
                URL url = new URL("http://localhost:3000/" + fileUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    reader.lines()
                            .map(UUID::fromString)
                            .forEach(sink::next);
                } catch (Exception e) {
                    logger.error("Error reading from the input stream", e);
                    sink.error(e);
                    return; // Exit early on error
                }

                logger.info("File download completed");
                sink.complete();
            } catch (Exception e) {
                logger.error("Failed to read the file from the server", e);
                sink.error(e);
            }
        });
    }

    private Mono<TargetAudience> saveToDatabase(String fileName, UUID fileLine) {
        TargetAudience entity = new TargetAudience();
        entity.setFilename(fileName);
        entity.setMemberId(fileLine);

        return Mono.fromCallable(() -> dataRepository.save(entity)); // Assuming save returns TargetAudience
    }
}