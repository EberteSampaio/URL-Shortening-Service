package br.com.deveberte.urlshortening;

import br.com.deveberte.urlshortening.domain.entity.Link;
import br.com.deveberte.urlshortening.service.UrlService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@AutoConfigureMockMvc
public class UrlResourceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UrlService urlService;

    @Test
    public void shouldCreateUrlAndReturn201WhitHeaderLocation() throws Exception {
        String url = "https://www.example.com";
        String json = String.format("""
                        {
                            "link": "%s"
                        }
                        """, url);

        Link linkMock = new Link();
        linkMock.setId(1L);
        linkMock.setUrl(url);
        linkMock.setShortCode("abc");

        when(urlService.create(anyString())).thenReturn(linkMock);

        mockMvc.perform(post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/shorten/1"));
    }

    @Test
    public void shouldReturnShortCodeWithStatus200() throws Exception {
        String shortCode = "abc";
        Link linkMock = new Link();
        linkMock.setId(1L);
        linkMock.setUrl("https://www.example.com");
        linkMock.setShortCode(shortCode);

        when(this.urlService.getLinkByShortCode(shortCode)).thenReturn(linkMock);

        mockMvc.perform(get("/api/shorten/{shortCode}", shortCode)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortcode").value("abc"))
                .andExpect(jsonPath("$.url").value("https://www.example.com"));
    }

    @Test
    public void shouldUpdateOriginalUrl() throws Exception {
        String shortCode = "abc";
        Link linkMock = new Link();
        linkMock.setId(1L);
        linkMock.setUrl("https://www.example.com");
        linkMock.setShortCode(shortCode);

        when(this.urlService.update(eq(linkMock.getShortCode()), any())).thenReturn(linkMock);

        String requestJson = """
                {
                    "link":"https://www.newexample.com"
                }
                """;

        mockMvc.perform(put("/api/shorten/{shortCode}", shortCode)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://www.example.com"));
    }

    @Test
    public void shouldDeleteUrlAndReturn204() throws Exception {
        String shortUrl = "abc";

        doNothing().when(urlService).delete(shortUrl);

        mockMvc.perform(delete("/api/shorten/{shortCode}", shortUrl))
                .andExpect(status().isNoContent());

        verify(urlService).delete(shortUrl);
    }
}
