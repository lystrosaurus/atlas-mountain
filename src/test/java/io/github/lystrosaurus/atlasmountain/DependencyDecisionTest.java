package io.github.lystrosaurus.atlasmountain;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class DependencyDecisionTest {

    @Test
    void dependencyDecisionDocumentDoesNotContainPlaceholders() throws Exception {
        String content = Files.readString(Path.of("docs/superpowers/specs/2026-05-09-atlas-mountain-dependency-decisions.md"));

        assertThat(content)
                .doesNotContain("Candidate")
                .doesNotContain("unverified");
    }
}
