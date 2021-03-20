package hu.netcode.futurejavademo.completablefuture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(value = MockitoExtension.class)
@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
public class DuckDuckGoTest {
    @Mock
    private Call callMock;
    @Mock
    private OkHttpClient okHttpClientMock;
    @Mock
    private Request requestMock;
    @Mock
    private Request.Builder requestBuilderMock;
    @Mock
    private Response responseMock;
    @Mock
    private ResponseBody responseBodyMock;

    private ObjectMapper objectMapper;

    @BeforeAll
    void setup() {
        objectMapper = new ObjectMapper()
                .findAndRegisterModules();
    }

    @BeforeEach
    void init() {
        Mockito.reset(callMock, okHttpClientMock, requestMock, requestBuilderMock, responseMock, responseBodyMock);
    }

    @DisplayName(value = "DuckDuckGo: Test for function search")
    @Nested
    @TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
    class Search {
        @DisplayName(value = "Successful search")
        @Test
        void successfulSearch() throws IOException {
            when(callMock.execute()).thenReturn(responseMock);
            when(okHttpClientMock.newCall(any())).thenReturn(callMock);
            when(responseBodyMock.string()).thenReturn("{}");
            when(responseMock.body()).thenReturn(responseBodyMock);
            when(responseMock.isSuccessful()).thenReturn(true);
            DuckDuckGo duck = new DuckDuckGo(objectMapper, okHttpClientMock);
            String result = duck.search("apple");
            assertNotNull(result);
            assertEquals("{}", result);
        }

        @DisplayName(value = "Search failed because of an Internal Server Error")
        @Test
        void searchFailedBecauseOfAnInternalServerError() throws IOException {
            when(callMock.execute()).thenReturn(responseMock);
            when(okHttpClientMock.newCall(any())).thenReturn(callMock);
            when(responseBodyMock.string()).thenReturn("Internal Server Error");
            when(responseMock.body()).thenReturn(responseBodyMock);
            when(responseMock.isSuccessful()).thenReturn(false);
            DuckDuckGo duck = new DuckDuckGo(objectMapper, okHttpClientMock);
            String result = duck.search("apple");
            assertNotNull(result);
            assertEquals("", result);
        }

        @DisplayName(value = "Search failed because of an IOException")
        @Test
        void searchFailedBecauseOfAnIOException() throws IOException {
            when(callMock.execute()).thenThrow(new IOException());
            when(okHttpClientMock.newCall(any())).thenReturn(callMock);
            DuckDuckGo duck = new DuckDuckGo(objectMapper, okHttpClientMock);
            String result = duck.search("apple");
            assertNotNull(result);
            assertEquals("", result);
        }
    }

    @DisplayName(value = "DuckDuckGo: Test for function searchAsync")
    @Nested
    @TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
    class SearchAsync {
        @DisplayName(value = "Successful async search")
        @Test
        void successfulAsyncSearch() throws IOException {
            when(callMock.execute()).thenReturn(responseMock);
            when(okHttpClientMock.newCall(any())).thenReturn(callMock);
            when(responseBodyMock.string()).thenReturn("{}");
            when(responseMock.body()).thenReturn(responseBodyMock);
            when(responseMock.isSuccessful()).thenReturn(true);
            DuckDuckGo duck = new DuckDuckGo(objectMapper, okHttpClientMock);
            String result = duck.searchAsync("apple").join();
            assertNotNull(result);
            assertEquals("{}", result);
        }

        @DisplayName(value = "Async search failed because of a JsonProcessingException")
        @Test
        void searchFailedBecauseOfAJsonProcessingException() throws IOException {
            ObjectMapper objectMapperMock = Mockito.mock(ObjectMapper.class);
            when(callMock.execute()).thenReturn(responseMock);
            when(objectMapperMock.readValue(anyString(), eq(JsonNode.class)))
                    .thenThrow(new JsonProcessingException("error"){});
            when(okHttpClientMock.newCall(any())).thenReturn(callMock);
            when(responseBodyMock.string()).thenReturn("{}");
            when(responseMock.body()).thenReturn(responseBodyMock);
            when(responseMock.isSuccessful()).thenReturn(true);
            DuckDuckGo duck = new DuckDuckGo(objectMapperMock, okHttpClientMock);
            assertThrows(CompletionException.class, () -> {
                duck.searchAsync("apple").join();
            });
        }
    }
}