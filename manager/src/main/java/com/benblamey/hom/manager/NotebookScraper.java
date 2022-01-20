package com.benblamey.hom.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class NotebookScraper {

    private static Logger logger = LoggerFactory.getLogger(NotebookScraper.class);

    public static List<String> getFunctions(String directory) throws IOException, InterruptedException {
        String stdOut = Util.executeShellLogAndBlock(new String[]{
                        "bash",
                        "-ec",
                // Unescaped:
                // grep --extended-regexp --only-matching "^\s*\"\s*def ([^(])+\(" *.ipynb | sed -E "s/(.+):\s+\"def (.+)\(/\1,\2/"
                        "grep --extended-regexp --only-matching \"^\\s*\\\"\\s*def ([^(])+\\(\" *.ipynb | sed -E \"s/(.+):\\s+\\\"def (.+)\\(/\\1::\\2/\""
                }, new File(directory), null);

        return Arrays.stream(stdOut.split("\n")).toList();
    }

    // For testing...
    public static void main(String[] args) throws IOException, InterruptedException {
        getFunctions("/Users/benblamey/projects/github-me/hom-impl-2/persistentvolume");
    }
}
