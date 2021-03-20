package hu.netcode.futurejavademo.completablefuture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class DuckDuckGo {
    private static final Logger LOGGER = LogManager.getLogger(DuckDuckGo.class);

    private static final String DUCK_DUCK_GO_API_BASE_URL = "https://api.duckduckgo.com";

    private final ObjectMapper objectMapper;
    private final OkHttpClient okHttpClient;

    public DuckDuckGo(ObjectMapper objectMapper, OkHttpClient okHttpClient) {
        this.objectMapper = objectMapper;
        this.okHttpClient = okHttpClient;
    }

    public CompletableFuture<String> searchAsync(String searchWord) {
        return CompletableFuture.supplyAsync(() -> search(searchWord))
                .thenApply(result -> {
                    try {
                        LOGGER.info(objectMapper.readValue(result, JsonNode.class).toString());
                    } catch (JsonProcessingException ex) {
                        LOGGER.error(ExceptionUtils.getMessage(ex));
                        throw new CompletionException(ex);
                    }
                    return result;
                }).whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        LOGGER.error(throwable.getMessage());
                    }
                    LOGGER.debug("Search finished");
                });
    }

    public String search(String searchWord) {
        Request request = getRequest(DUCK_DUCK_GO_API_BASE_URL, Map.of("q", searchWord, "format", "json",
                "pretty", "1"));
        LOGGER.debug(request);
        try {
            try (Response response = okHttpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    return response.body().string();
                } else {
                    LOGGER.error(response.body().string());
                }
            }
        } catch (IOException ex) {
            LOGGER.error(ExceptionUtils.getMessage(ex));
        }
        return "";
    }

    private Request getRequest(String url) {
        LOGGER.debug("Create request for \"{}\"", url);
        return new Request.Builder()
                .url(url)
                .build();
    }

    private Request getRequest(String url, Map<String, String> queryList) {
        HttpUrl.Builder httpUrlBuilder = Objects.requireNonNull(HttpUrl.parse(url))
                .newBuilder();
        queryList.forEach(httpUrlBuilder::addQueryParameter);
                return getRequest(httpUrlBuilder.build().toString());
    }
}
