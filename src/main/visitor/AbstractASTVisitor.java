package main.visitor;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTVisitor;

import main.data.JavaMethodInvocation;

public abstract class AbstractASTVisitor extends ASTVisitor {

    public abstract List<JavaMethodInvocation> getMethodInvocations();

    public abstract Map<JavaMethodInvocation, Integer> getMethodInvocationCount();

    public abstract void setDebuggingOn(boolean debuggingOn);

}
