package com.pdfforge.sejda;

import org.sejda.core.service.DefaultTaskExecutionService;
import org.sejda.core.support.io.model.FileOutput;
import org.sejda.model.input.PdfFileSource;
import org.sejda.model.input.PdfMergeInput;
import org.sejda.model.parameter.MergeParameters;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public final class SejdaMergeService {
    private final DefaultTaskExecutionService execution = new DefaultTaskExecutionService();

    public File merge(List<File> pdfs) throws IOException {
        if (pdfs == null || pdfs.size() < 2) {
            throw new IllegalArgumentException("At least two PDF files are required.");
        }

        File output = Files.createTempFile("pdfforge-merged-", ".pdf").toFile();
        try {
            MergeParameters parameters = new MergeParameters();
            for (File input : pdfs) {
                if (input == null || !input.isFile() || input.length() == 0) {
                    throw new IllegalArgumentException("An uploaded PDF is empty or unavailable.");
                }
                parameters.addInput(new PdfMergeInput(PdfFileSource.newInstanceNoPassword(input)));
            }
            parameters.setOutput(FileOutput.file(output).name("merged.pdf"));
            execution.execute(parameters);

            if (!output.isFile() || output.length() == 0) {
                throw new IOException("Sejda did not produce a merged PDF.");
            }
            return output;
        } catch (RuntimeException | IOException ex) {
            Files.deleteIfExists(output.toPath());
            throw ex;
        }
    }
}
