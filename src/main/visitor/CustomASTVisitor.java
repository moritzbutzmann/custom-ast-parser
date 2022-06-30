package main.visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;

import main.data.JavaMethodInvocation;

public class CustomASTVisitor extends AbstractASTVisitor {
    private List<JavaMethodInvocation> methodInvocations;

    private boolean debuggingOn = false;

    public CustomASTVisitor() {
        methodInvocations = new ArrayList<JavaMethodInvocation>();
    }

    @Override
    public boolean visit(final MethodInvocation mi) {

        if (mi.getExpression() != null) {
            ITypeBinding b = mi.getExpression().resolveTypeBinding();

            if (b != null) {
                storeMethodInvocationData(new JavaMethodInvocation(mi.getName().toString(), b.getQualifiedName()));
            }
        }
        super.visit(mi);
        return true;
    }

    @Override
    public boolean visit(final NormalAnnotation an) {
        ITypeBinding b = an.resolveTypeBinding();
        if (b != null) {
            storeMethodInvocationData(new JavaMethodInvocation(an.toString(), b.getQualifiedName()));
        }
        return true;
    }

    @Override
    public boolean visit(final MarkerAnnotation an) {
        ITypeBinding b = an.resolveTypeBinding();
        if (b != null) {
            storeMethodInvocationData(new JavaMethodInvocation(an.toString(), b.getQualifiedName()));
        }
        return true;
    }

    @Override
    public boolean visit(final SingleMemberAnnotation an) {
        ITypeBinding b = an.resolveTypeBinding();
        if (b != null) {
            storeMethodInvocationData(new JavaMethodInvocation(an.toString(), b.getQualifiedName()));
        }
        return true;
    }

    @Override
    public List<JavaMethodInvocation> getMethodInvocations() {
        return this.methodInvocations;
    }

    private void storeMethodInvocationData(final JavaMethodInvocation jMI) {
        this.methodInvocations.add(jMI);
        if (debuggingOn) {
            System.out.println("Class: " + jMI.getMemberClassName() + " Method: " + jMI.getMethodName());
        }
    }

    @Override
    public void setDebuggingOn(final boolean debuggingOn) {
        this.debuggingOn = debuggingOn;
    }

    @Override
    public Map<JavaMethodInvocation, Integer> getMethodInvocationCount() {
        Map<JavaMethodInvocation, Integer> result = new HashedMap();
        for (JavaMethodInvocation jMI : methodInvocations) {
            if (null == result.get(jMI)) {
                result.put(jMI, 0);
            }
            result.put(jMI, result.get(jMI) + 1);
        }
        return result;
    }

}
