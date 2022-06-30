package main;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import main.data.JavaMethodInvocation;
import main.data.SourceCodeCategory;
import main.parser.CustomASTParserPreAnalyzed;
import main.visitor.CustomASTVisitor;

/**
 * Main class to start the dependency-analysis.
 * To customize this project for your needs, you might consider:
 * - placing your required libraries under the libs folder
 * - changing the directory path in dirPathProject according to your project
 * - changing the output file appendix according to your needs
 */
public class Main {

    static String dirPathProject = "C:\\work\\project_name\\";
    static String dirPathProjectLibs = ".\\dependencies";
    static String outputFileAppendix = "project_name";

    private static void serialize(
	    final Map<SourceCodeCategory, Map<JavaMethodInvocation, Integer>> methodInvocationData,
	    final String filepath) {
	try {
	    FileWriter fileWriter = new FileWriter(filepath);
	    fileWriter.write("filePath;Class;Method;Occurrences;\n");
	    for (Map.Entry<SourceCodeCategory, Map<JavaMethodInvocation, Integer>> entry : methodInvocationData
		    .entrySet()) {
		for (Map.Entry<JavaMethodInvocation, Integer> mi : entry.getValue().entrySet()) {
		    fileWriter.write(entry.getKey() + ";" + mi.getKey().getMemberClassName() + ";"
			    + mi.getKey().getMethodName() + ";" + mi.getValue() + ";\n");
		}
	    }
	    fileWriter.close();
	} catch (IOException e) {
	    System.out.println("An error occurred.");
	    e.printStackTrace();
	}
    }

    public static void main(final String[] args) throws IOException {
	DateTimeFormatter outputTimeStampFormat = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm");
	String outputFilePath = "./results/result_" + outputTimeStampFormat.format(LocalDateTime.now()) + "_"
		+ outputFileAppendix + ".txt";
	Map<SourceCodeCategory, Map<JavaMethodInvocation, Integer>> result = CustomASTParserPreAnalyzed
		.ParseFilesInDir(dirPathProjectLibs, dirPathProject, new CustomASTVisitor());
	serialize(result, outputFilePath);
	System.out.println("finished");
    }

}
