package hu.netcode.futurejavademo;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.netcode.futurejavademo.completablefuture.DuckDuckGo;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

public class CompletableFutureDemo {
    private static final Logger LOGGER = LogManager.getLogger(CompletableFutureDemo.class);

    public static void main(String[] args) {
        final var start = Instant.now();
        final var objectMapper = new ObjectMapper().findAndRegisterModules();
        final var okHttpClient = new OkHttpClient();
        final var duck = new DuckDuckGo(objectMapper, okHttpClient);
        try {
            final List<CompletableFuture<String>> futureList = Files.readAllLines(
                    Path.of(Objects.requireNonNull(CompletableFutureDemo.class.getClassLoader()
                            .getResource("car_brands.txt")).toURI()))
                    .stream()
                    .map(duck::searchAsync)
                    .collect(Collectors.toList());
            CompletableFuture.allOf(futureList.toArray(CompletableFuture[]::new))
                    .thenApply(v -> futureList.stream().map(CompletableFuture::join).collect(Collectors.toList()))
                    .join();
            futureList.forEach(f -> f.whenComplete((t, ex) -> {
                if (ex != null) {
                    LOGGER.error(ExceptionUtils.getMessage(ex));
                }
            }));
            LOGGER.info("{} ms", Duration.between(start, Instant.now()).toMillisPart());
        } catch (CompletionException | IOException | URISyntaxException ex) {
            LOGGER.error(ExceptionUtils.getMessage(ex));
            System.exit(-1);
        }
    }
}
