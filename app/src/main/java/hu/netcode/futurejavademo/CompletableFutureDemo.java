package hu.netcode.futurejavademo;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.netcode.futurejavademo.completablefuture.DuckDuckGo;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

public class CompletableFutureDemo {
    private static final Logger LOGGER = LogManager.getLogger(CompletableFutureDemo.class);

    public static void main(String[] args) {
        final var objectMapper = new ObjectMapper().findAndRegisterModules();
        final var okHttpClient = new OkHttpClient();
        final var stopWatch = new StopWatch();
        final var duck = new DuckDuckGo(objectMapper, okHttpClient);
        stopWatch.start();
        try {
            final List<CompletableFuture<String>> futureList = CompletableFuture.supplyAsync(() -> {
                try {
                    return Files.readAllLines(
                            Path.of(Objects.requireNonNull(CompletableFutureDemo.class.getClassLoader()
                                    .getResource("car_brands.txt")).toURI()))
                            .parallelStream()
                            .map(duck::searchAsync)
                            .collect(Collectors.toList());
                } catch (IOException | URISyntaxException ex) {
                    LOGGER.error(ExceptionUtils.getMessage(ex));
                    return Collections.<CompletableFuture<String>>emptyList();
                }
            }).join();
            CompletableFuture.allOf(futureList.toArray(CompletableFuture[]::new))
                    .thenApply(v -> futureList.parallelStream()
                            .map(CompletableFuture::join)
                            .collect(Collectors.toList())
                    )
                    .join();
            futureList.forEach(f -> f.whenComplete((t, ex) -> {
                if (ex != null) {
                    LOGGER.error(ExceptionUtils.getMessage(ex));
                }
            }));
            stopWatch.stop();
            LOGGER.info("{} ms", stopWatch.getTime());
        } catch (CompletionException ex) {
            LOGGER.error(ExceptionUtils.getMessage(ex));
            System.exit(-1);
        }
    }
}
