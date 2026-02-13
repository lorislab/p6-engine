package org.lorislab.p6.bpmn2;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.lorislab.p6.bpmn2.model.Definitions;
import org.lorislab.p6.bpmn2.model.Process;
import org.lorislab.p6.bpmn2.reader.ModelReader;

class XmlParserTest {

    @Test
    void test100StartEnd() {
        Definitions def = load("100-start-end.bpmn");
        assertThat(def).isNotNull().extracting(Definitions::getId).isNotNull().isEqualTo("100_start_end_def");
        assertThat(def.getRootElement().getProcess()).isNotEmpty()
                .hasSize(1).first().isNotNull()
                .extracting(org.lorislab.p6.bpmn2.model.Process::getId).isEqualTo("StartEndEventId");
    }

    @Test
    void test100StartEndDrools() {
        Definitions def = load("100-start-end-drools.bpmn");
        assertThat(def).isNotNull().extracting(Definitions::getId).isNotNull().isEqualTo("100_start_end_drools_def");
        assertThat(def.getRootElement().getProcess()).isNotEmpty()
                .hasSize(1).first().isNotNull()
                .extracting(Process::getId).isEqualTo("100_start_end_drools");
    }

    private Definitions load(String name) {
        try {
            return ModelReader.read(Files.readAllBytes(Paths.get("src/test/resources/bpmn2/" + name)));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
