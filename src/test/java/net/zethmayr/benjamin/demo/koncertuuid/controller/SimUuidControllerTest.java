package net.zethmayr.benjamin.demo.koncertuuid.controller;

import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import sun.security.provider.certpath.OCSPResponse;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SimUuidControllerTest {
    @LocalServerPort
    private int port;

    private String root;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SimUuidController underTest;

    @Before
    public void setUp() {
        root = "http://localhost:"+port;
    }

    private static final String VALIDATION_REGEX = "^[0-9]{30,40}$";

    @Test
    public void canGetValueFromEndpoint() {
        final ResponseEntity<String> response = restTemplate.getForEntity(root + "/simUuid", String.class);
        assertThat(response.getStatusCode(), is(OK));
        val body = response.getBody();
        assertThat(body.length(), greaterThanOrEqualTo(30));
        assertThat(body.length(), lessThanOrEqualTo(40));
        assertThat(body.matches(VALIDATION_REGEX), is(true));
    }

    @Test
    public void canGetValueFromEndpointForManyParameters() {
        for (int x = 0; x < 20; x++) {
            for (int y = 0; y < 20; y++) {
                for (int z = 0; z < 20; z++) {
                    val uri = new StringBuilder(root + "/simUuid");
                    String sep = "?";
                    val amp = "&";
                    if (x > 1) {
                        uri.append(sep).append("x=").append(x);
                        sep = amp;
                    }
                    if (y > 0) {
                        uri.append(sep).append("y=").append(y);
                        sep = amp;
                    }
                    if (z > 0) {
                        uri.append(sep).append("z=").append(z);
                    }
                    final ResponseEntity<String> response = restTemplate.getForEntity(uri.toString(), String.class);
                    assertThat(response.getStatusCode(), is(OK));
                    assertThat(response.getBody().matches(VALIDATION_REGEX), is(true));
                }
            }
        }
    }

    @Test
    public void yieldsABadRequestOnABadRequest() {
        final ResponseEntity<String> response = restTemplate.getForEntity(root + "/simUuid?x=0", String.class);
        assertThat(response.getStatusCode(), is(BAD_REQUEST));
    }
}
