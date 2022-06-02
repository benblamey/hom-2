package com.benblamey.hom.manager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static java.nio.charset.StandardCharsets.UTF_8;

public class NotebooksFromTemplates {

    public static void CreateAnalyzeTierNotebookFromTemplate(String sampleJsonlPath,
                                                             String targetNotebookPath) throws IOException {

        String templ = Util.getResourceAsStringFromUTF8("example.analyze-tier_tmpl.ipynb")
                .replace("$jsonFilePath", sampleJsonlPath);

        Files.writeString(
                new File(targetNotebookPath).toPath(),
                templ,
                UTF_8);
    }


}
