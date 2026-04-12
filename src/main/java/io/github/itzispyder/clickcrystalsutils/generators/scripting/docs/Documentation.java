package io.github.itzispyder.clickcrystalsutils.generators.scripting.docs;

import com.google.gson.GsonBuilder;
import io.github.itzispyder.clickcrystalsutils.generators.scripting.format.Format;
import io.github.itzispyder.clickcrystalsutils.generators.scripting.format.FormatGroup;
import io.github.itzispyder.clickcrystalsutils.util.FileValidationUtils;
import io.github.itzispyder.clickcrystalsutils.util.nonstatic.StringFormatter;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Documentation {

    private final Map<String, DocumentationFile> files;

    public Documentation() {
        this.files = new HashMap<>();
    }

    public void registerComment(String rawComment) {
        if (rawComment.charAt(0) == '(') { // comment that starts with multi-literal argument
            String regex = "\\((!?\\w+\\|?)+\\)\\??\\s*";
            Matcher matcher;

            matcher = Pattern.compile(regex).matcher(rawComment);
            if (!matcher.find())
                throw new IllegalArgumentException("malformed multi-literal comment: " + rawComment);
            matcher = Pattern.compile("(\\w+)\\|?").matcher(matcher.group());
            rawComment = rawComment.replaceFirst(regex, "");

            while (matcher.find())
                registerComment(matcher.group(1) + " " + rawComment);
        }
        else { // comment that starts with a singular literal argument
            String name = rawComment.split("\\s+")[0];
            DocumentationFile file = files.computeIfAbsent(name, string -> new DocumentationFile());
            file.registerComment(rawComment);
        }
    }

    public void generateFiles(Format format) {
        for (DocumentationFile file : files.values()) {
            file.generateContents();
        }
        this.generateLegendFile();
        this.generateFormatJson(format);
        this.generateFormatRegex(format);
//        this.generateNetworkPacketFile();
        this.generateCodeBlockOpenersFile(format);
    }

    private void generateLegendFile() {
        System.out.println("<- generating legend.md");

        File file = new File("DOCUMENTATION/legend.md");
        FileValidationUtils.validate(file);

        StringBuilder builder = new StringBuilder();
        files.values().stream().sorted(Comparator.comparing(DocumentationFile::getPath)).forEach(doc -> {
            builder.append("- [%s (%s)](./commands/%s.md)%n".formatted(doc.getName(), doc.getRawName(), doc.getRawName()));
        });

        StringFormatter formatter = new StringFormatter();
        formatter.def("commands", builder.toString().trim());

        FileValidationUtils.quickWrite(file, formatter.format("""
                # Documentation Legend
                Here you will find out how to read this documentation, and what each symbol means.

                ### Legend Table
                
                | Symbol            | Meaning                                     | Aliases                             | Example           |
                |-------------------|---------------------------------------------|-------------------------------------|-------------------|
                | \\<int\\>           | integer                                     |                                     | 123               |
                | \\<num\\>           | number                                      |                                     | 1.23              |
                | \\<vec\\>           | singular relative vector component          | \\<x\\>,\\<y\\>,\\<z\\>,\\<pitch\\>,\\<yaw\\> | ~1.23             |
                | \\<comparator\\>    | \\> < == >= <= !=                            |                                     | >=                |
                | \\<identifier\\>    | :direct_identifier OR #indirect_identifier  |                                     | :diamond_sword    |
                | \\<server-packet\\> | [server packet](./network_packets.md)       |                                     | playerList        |
                | \\<client-packet\\> | [client packet](./network_packets.md)       |                                     | handSwing         |
                | \\<input\\>         | an input type                               |                                     | attack            |
                | \\<aim-anchor\\>    | aim anchor                                  |                                     | head              |
                | ...               | literal                                     |                                     | abc               |
                | "..."             | quoted literal                              |                                     | "a b c"           |
                | \\w+               | constant literal                            |                                     |                   |
                | (\\w+\\|\\w+\\|...)   | constant literals                           |                                     |                   |
                | {}                | command line or code block of command lines |                                     | say "Hello World" |
                
                ### Optional Argument Symbols
                The argument is optional if a ? is appended at the end. Any argument symbol followed by a question mark 
                will render said argument optional, meaning the script interpreter will not throw an error if it was absent.
                
                ### What is a Command Line?
                A script command line is any instruction that'll tell your Minecraft client what to do.
                For example, a command line that tells your Minecraft client to say Hello World in chat would look like:
                
                ```
                say "Hello World"
                ```
                
                Here are some examples of command lines:
                ${commands}
                
                ### What is a Code Block?
                Normally, we would have 1 command line per, let's say, an if statement.
                
                ```
                if holding :diamond say "I am holding a diamond"
                ```
                
                The problem emerges when we try to execute more than 1 command line per if statement.
                In theory, we could just spam new lines like so:
                
                ```
                if holding :diamond say "I am holding a diamond"
                if holding :diamond say "Hello World"
                if holding :diamond input jump
                ```
                
                But this practice is redundant and may be seen as inefficient to our scripters.
                To solve this problem, you can have all of your command lines stored in between brackets, 
                essentially telling the script interpreter that you want to execute all of these lines. Do note that 
                each time a new pair of nest brackets appear, it is conventional to increase your indentation by 1.
                
                ```
                if holding :diamond {
                    say "I am holding a diamond"
                    say "Hello World"
                    input jump
                }
                ```
                
                Nested indentation example:
                ```
                if holding :diamond {
                    say "I am holding a diamond"
                    say "Hello World"
                    input jump
                    
                    if off_holding :diamond {
                        say "My off hand is also holding a diamond"
                    }
                }
                ```
                
                Happy coding and cpvping!
                """));
    }

    private void generateFormatJson(Format format) {
        System.out.println("<- generating format.json");

        File file = new File("DOCUMENTATION/format.json");
        FileValidationUtils.validate(file);
        FileValidationUtils.quickWrite(file, new GsonBuilder().serializeNulls().setPrettyPrinting().create().toJson(format));
    }

    private void generateFormatRegex(Format format) {
        System.out.println("<- generating regex.txt");

        File file = new File("DOCUMENTATION/regex.txt");
        FileValidationUtils.validate(file);
        FileValidationUtils.quickWrite(file, format.getAcceptingRegex());
    }

    private void generateCodeBlockOpenersFile(Format format) {
        System.out.println("<- generating code_block_openers.txt");

        File file = new File("DOCUMENTATION/code_block_openers.txt");
        Format results = new Format();

        for (FormatGroup group : format.getCodeBlockOpeners())
            for (FormatGroup opener : group.getLeadingCodeBlockOpeners())
                if (!results.has(opener))
                    results.append(opener);

        FileValidationUtils.validate(file);
        FileValidationUtils.quickWrite(file, results.getAcceptingRegex());
    }

    private void generateNetworkPacketFile() {
        System.out.println("<- generating network_packets.md");

        try {
            Path dst = Paths.get("DOCUMENTATION/network_packets.md");
            Path src = Paths.get("run/.clickcrystals/.data/network_packets.md");
            Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
