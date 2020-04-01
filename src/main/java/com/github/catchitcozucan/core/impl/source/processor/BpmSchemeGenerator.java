package com.github.catchitcozucan.core.impl.source.processor;

import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.NL;


import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.github.catchitcozucan.core.exception.ProcessRuntimeException;

import com.github.catchitcozucan.core.internal.util.domain.BaseDomainObject;
import com.github.catchitcozucan.core.internal.util.domain.ToStringBuilder;
import com.github.catchitcozucan.core.internal.util.io.IO;

public class BpmSchemeGenerator extends BaseDomainObject {

    private final File xmlFile;
    private final List<BpmSchemeElementDescriptor> descriptors;
    // @formatter:off
	private static final String HEADER = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append(NL)
			.append("<bpmn:definitions xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" ")
			.append("xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" ")
			.append("xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\" id=\"%s\" targetNamespace=\"http://bpmn.io/schema/bpmn\" ")
			.append("exporter=\"catchitcozucan (https://www.github.com/catchitcozucan/core)\" exporterVersion=\"1.0\">\n" + "  ")
			.append("<bpmn:process id=\"%s\" isExecutable=\"true\">").toString();
	// @formatter:on

    private static final String PROCESS_END = "</bpmn:process>";

    BpmSchemeGenerator(File xmlFile, List<BpmSchemeElementDescriptor> descriptors) {
        this.xmlFile = xmlFile;
        this.descriptors = descriptors;
    }

    void generateAndWriteScheme() {
        StringBuilder content = new StringBuilder(String.format(HEADER, BpmSchemeElementDescriptor.generateIdForInBetween(BpmSchemeElementDescriptor.TypeInBetween.Definitions), BpmSchemeElementDescriptor.generateIdForInBetween(BpmSchemeElementDescriptor.TypeInBetween.Process)));
        content.append(NL).append(PROCESS_END);
        try {
            IO.overwriteStringToFileWithEncoding(xmlFile.getAbsolutePath(), content.toString(), StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            throw new ProcessRuntimeException(String.format("Could not write BPM 2.0 XML file : %s", xmlFile.getAbsolutePath()), e);
        }
    }

    @Override
    public String doToString() {
        ToStringBuilder b = new ToStringBuilder("file", xmlFile.getAbsolutePath());
        descriptors.stream().forEachOrdered(d -> b.append(d.getTaskName(), d.toString()));
        return b.toString();
    }
}

