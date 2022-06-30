package main.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.collections.map.HashedMap;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

import main.data.JavaMethodInvocation;
import main.data.SourceCodeCategory;
import main.helper.ClassPathHelper;
import main.visitor.AbstractASTVisitor;

/**
 * Parser that pre-analyzes the parsed files. Thereby there is distinguished
 * between the following four categories of
 * sourcecode based on the filepath:
 * a) manual created sourcecode - no-testcode: code without unit tests
 * b) manual created sourcecode - testcode containing all unit tests
 * c) generated sourcecode - non-testcode: code without unit tests
 * d) generated sourcecode - testcode containing all unit tests
 */
public class CustomASTParserPreAnalyzed {

    // use ASTParse to parse string
    public static Map<JavaMethodInvocation, Integer> parse(final String[] libSources, final char[] source,
	    final String fileName, final AbstractASTVisitor astVisitor) {
	ASTParser parser = ASTParser.newParser(AST.JLS8);
	parser.setResolveBindings(true);
	parser.setBindingsRecovery(true);
	parser.setSource(source);
	parser.setEnvironment(libSources, new String[] { "." }, new String[] { "UTF-8" }, true);
	parser.setUnitName(fileName);

	@SuppressWarnings("rawtypes")
	Map options = JavaCore.getOptions();
	JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
	parser.setCompilerOptions(options);
	CompilationUnit result = (CompilationUnit) parser.createAST(null);
	ASTVisitor visitor = astVisitor;
	result.accept(visitor);

	return astVisitor.getMethodInvocationCount();
    }

    /**
     * Reads the file content specified in the filepath into a string.
     *
     * @param filePath of the file to read
     * @return Filecontent as String
     * @throws IOException
     */
    public static String readFileToString(final String filePath) throws IOException {
	StringBuilder fileData = new StringBuilder(1000);
	BufferedReader reader = new BufferedReader(new FileReader(filePath));

	char[] buf = new char[10];
	int numRead = 0;
	while ((numRead = reader.read(buf)) != -1) {
	    String readData = String.valueOf(buf, 0, numRead);
	    fileData.append(readData);
	    buf = new char[1024];
	}
	reader.close();
	return fileData.toString();
    }

    /**
     * Merges two maps of the specified type, where the little map is merged in the
     * bigMap. The specified map contains
     * the number of invocations for each type of JavaMethodInvocation
     *
     * @param bigMap
     * @param littleMap
     * @return merged map of bigMap and littleMap
     */
    private static Map<JavaMethodInvocation, Integer> mergeCountMaps(Map<JavaMethodInvocation, Integer> bigMap,
	    final Map<JavaMethodInvocation, Integer> littleMap) {
	if (null == bigMap) {
	    bigMap = new HashedMap();
	}
	if (null == littleMap) {
	    return bigMap;
	}
	for (Map.Entry<JavaMethodInvocation, Integer> mi : littleMap.entrySet()) {
	    if (null == bigMap.get(mi.getKey())) {
		bigMap.put(mi.getKey(), mi.getValue());
	    } else {
		bigMap.put(mi.getKey(), bigMap.get(mi.getKey()) + mi.getValue());
	    }
	}

	return bigMap;

    }

    /**
     * Internal Method to recursively parse and analyse the specified file or
     * directory with the specified libraries and
     * the specified ASTVisitor.
     *
     * @param directoryPathLibSources
     * @param file
     * @param astVisitor
     * @return
     * @throws IOException
     */
    private static Map<SourceCodeCategory, Map<JavaMethodInvocation, Integer>> ParseFilesInDir(
	    final String directoryPathLibSources, final File file, final AbstractASTVisitor astVisitor)
	    throws IOException {

	Map<SourceCodeCategory, Map<JavaMethodInvocation, Integer>> methodInvocations = new HashedMap();

	if (file.isDirectory()) {
	    File[] files = file.listFiles();
	    for (File f : files) {
		if (f.isDirectory()) {
		    Map<SourceCodeCategory, Map<JavaMethodInvocation, Integer>> resultTemp = ParseFilesInDir(
			    directoryPathLibSources, f, astVisitor);
		    if (null != resultTemp) {
			methodInvocations.put(SourceCodeCategory.manual_nontest,
				mergeCountMaps(methodInvocations.get(SourceCodeCategory.manual_nontest),
					resultTemp.get(SourceCodeCategory.manual_nontest)));
			methodInvocations.put(SourceCodeCategory.manual_test,
				mergeCountMaps(methodInvocations.get(SourceCodeCategory.manual_test),
					resultTemp.get(SourceCodeCategory.manual_test)));
			methodInvocations.put(SourceCodeCategory.generated_nontest,
				mergeCountMaps(methodInvocations.get(SourceCodeCategory.generated_nontest),
					resultTemp.get(SourceCodeCategory.generated_nontest)));
			methodInvocations.put(SourceCodeCategory.generated_test,
				mergeCountMaps(methodInvocations.get(SourceCodeCategory.generated_test),
					resultTemp.get(SourceCodeCategory.generated_test)));
		    }
		} else if (f.isFile() && f.getAbsolutePath().endsWith(".java")) {
		    String filepath = f.getAbsolutePath();
		    System.out.print(filepath + "     ");
		    Map<JavaMethodInvocation, Integer> resultTemp = parse(
			    ClassPathHelper.getRequiredClasspaths(directoryPathLibSources),
			    readFileToString(filepath).toCharArray(), f.getName(), astVisitor);

		    if (null != resultTemp) {
			Pattern p_manual_nontest = Pattern.compile("\\\\src\\\\main\\\\");
			Pattern p_manual_test = Pattern.compile("\\\\src\\\\test\\\\");
			Pattern p_generated_nontest = Pattern
				.compile("\\\\target\\\\generated-resources\\\\|\\\\target\\\\generated-sources\\\\");
			Pattern p_generated_test = Pattern.compile(
				"\\\\target\\\\generated-test-resources\\\\|\\\\target\\\\generated-test-sources\\\\");
			if (p_manual_nontest.matcher(filepath).find()) {
			    System.out.println(SourceCodeCategory.manual_nontest);
			    methodInvocations.put(SourceCodeCategory.manual_nontest, mergeCountMaps(
				    methodInvocations.get(SourceCodeCategory.manual_nontest), resultTemp));
			} else if (p_manual_test.matcher(filepath).find()) {
			    System.out.println(SourceCodeCategory.manual_test);
			    methodInvocations.put(SourceCodeCategory.manual_test,
				    mergeCountMaps(methodInvocations.get(SourceCodeCategory.manual_test), resultTemp));
			} else if (p_generated_nontest.matcher(filepath).find()) {
			    System.out.println(SourceCodeCategory.generated_nontest);
			    methodInvocations.put(SourceCodeCategory.generated_nontest, mergeCountMaps(
				    methodInvocations.get(SourceCodeCategory.generated_nontest), resultTemp));
			} else if (p_generated_test.matcher(filepath).find()) {
			    System.out.println(SourceCodeCategory.generated_test);
			    methodInvocations.put(SourceCodeCategory.generated_test, mergeCountMaps(
				    methodInvocations.get(SourceCodeCategory.generated_test), resultTemp));
			}
		    }
		}

	    }
	}

	return methodInvocations;

    }

    /**
     * Entrypoint-Method to search and analyse the sourcefiles in the specified
     * directory with the given
     * AbstractASTVisitor and the libraries at the specified path.
     *
     * @param directoryPathLibSources: Directory containing the Java-Libraries
     *                                 necessary for parsing the specified
     *                                 source files.
     * @param dirPath:                 The directory to parse and analyse.
     * @param astVisitor:              AST-Visitor to use when parsing and analyzing
     *                                 the files.
     * @return A map Containing ??
     * @throws IOException
     */
    public static Map<SourceCodeCategory, Map<JavaMethodInvocation, Integer>> ParseFilesInDir(
	    final String directoryPathLibSources, final String dirPath, final AbstractASTVisitor astVisitor)
	    throws IOException {
	System.out.println(dirPath);
	File root = new File(dirPath);

	return ParseFilesInDir(directoryPathLibSources, root, astVisitor);
    }
}
