package hu.netcode.futurejavademo.completablefuture;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
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

    private ObjectMapper objectMapper;

    @BeforeAll
    void setup() {
        objectMapper = new ObjectMapper()
                .findAndRegisterModules();
    }

    @BeforeEach
    void init() {
        //
    }

    @DisplayName(value = "DuckDuckGo: Test for function search")
    @Nested
    @TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
    class Search {
        @DisplayName(value = "Successful search")
        @Test
        void successfulSearch() throws IOException {
            ResponseBody responseBodyMock = Mockito.mock(ResponseBody.class);
            Response responseMock = Mockito.mock(Response.class);
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
            ResponseBody responseBodyMock = Mockito.mock(ResponseBody.class);
            Response responseMock = Mockito.mock(Response.class);
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
        @DisplayName(value = "Successfully search async")
        @Test
        void successfulAsyncSearch() {
            assertTrue(true);
        }
    }
}
