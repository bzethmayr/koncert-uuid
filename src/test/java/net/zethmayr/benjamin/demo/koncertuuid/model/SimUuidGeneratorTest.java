package net.zethmayr.benjamin.demo.koncertuuid.model;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.security.SecureRandom;
import java.util.Arrays;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;

@Slf4j
public class SimUuidGeneratorTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static final int ITERATIONS = 10000; // tune per dev patience

    @Test
    public void canGetDefaultedInstance() {
        val underTest = SimUuidGenerator.builder().build();
        LOG.info("Built {}", underTest);
        assertThat(underTest.getX(), is(SimUuidGenerator.DEFAULT_X));
        assertThat(underTest.getY(), is(SimUuidGenerator.DEFAULT_Y));
        assertThat(underTest.getZ(), is(SimUuidGenerator.DEFAULT_Z));
        assertThat(underTest.getRandom(), instanceOf(SecureRandom.class));
    }

    private void assertLengthThirtyOrMore(final String generated) {
        assertThat(generated.length(), greaterThanOrEqualTo(30));
    }

    @Test
    public void makes30OrMoreCharacters() {
        val underTest = SimUuidGenerator.builder().build();
        for (int i = 0; i < ITERATIONS; i++) {
            val generated = underTest.generate();
            LOG.info("Generated {}", generated);
            assertLengthThirtyOrMore(generated);
        }
        assertRule6WasMet(underTest);
    }

    private void assertLengthFortyOrLess(final String generated) {
        assertThat(generated.length(), lessThanOrEqualTo(40));
    }

    @Test
    public void makes40OrLessCharacters() {
        val underTest = SimUuidGenerator.builder().build();
        for (int i = 0; i < ITERATIONS; i++) {
            val generated = underTest.generate();
            LOG.info("Generated {}", generated);
            assertLengthFortyOrLess(generated);
        }
        assertRule6WasMet(underTest);
    }

    private void assertRule6WasMet(final SimUuidGenerator underTest) {
        val metrics = underTest.getMetrics();
        LOG.info("metrics are {}", Arrays.toString(metrics));
        // rule 6...
        assertThat(metrics[1] + metrics[2], greaterThanOrEqualTo(metrics[0] / 2));
    }

    @Test
    public void generatesWithMinimumRules() {
        val underTest = SimUuidGenerator.builder().z(0).build();
        assertThat(underTest.getZ(), is(SimUuidGenerator.MINIMUM_Z));
        for (int i = 0; i < ITERATIONS; i++) {
            val generated = underTest.generate();
            LOG.info("Generated {}", generated);
            assertLengthThirtyOrMore(generated);
            assertLengthFortyOrLess(generated);
        }
        assertRule6WasMet(underTest);
    }

    @Test
    public void generatesWithLotsOfRules() {
        val underTest = SimUuidGenerator.builder().z(99).y(23).x(3).build();
        for (int i = 0; i < ITERATIONS; i+= 100) {
            val generated = underTest.generate();
            LOG.info("Generated {}", generated);
            assertLengthThirtyOrMore(generated);
            assertLengthFortyOrLess(generated);
        }
        assertRule6WasMet(underTest);
        // seeing a lot of zero padding at this point.
    }

    @Test
    public void throwsOnConstructionWithTooSmallX() {
        thrown.expect(IllegalArgumentException.class);
        SimUuidGenerator.builder().x(1).build();
    }

    @Test
    public void throwsOnConstructionWithTooSmallY() {
        thrown.expect(IllegalArgumentException.class);
        SimUuidGenerator.builder().y(0).build();
    }

    @Test
    public void canRunExactlyFourRules() {
        val underTest = SimUuidGenerator.builder().z(4).build();
        for (int i = 0; i < ITERATIONS; i++) {
            val generated = underTest.generate();
            LOG.info("Generated {}", generated);
            assertLengthThirtyOrMore(generated);
            assertLengthFortyOrLess(generated);
        }
        assertRule6WasMet(underTest);
    }
}
