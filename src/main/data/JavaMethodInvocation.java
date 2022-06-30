package main.data;

import java.io.Serializable;

public class JavaMethodInvocation implements Serializable {
    private static final long serialVersionUID = 6731000636657849167L;
    private String methodName;
    private String memberClassName;

    public JavaMethodInvocation(final String methodName, final String memberClassName) {
        this.methodName = methodName;
        this.memberClassName = memberClassName;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getMemberClassName() {
        return memberClassName;
    }

    @Override
    public String toString() {
        return memberClassName + " " + methodName;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj.getClass() != this.getClass()) {
            return false;
        }
        final JavaMethodInvocation other = (JavaMethodInvocation) obj;
        if (!this.methodName.equals(other.methodName)) {
            return false;
        }
        if (!this.memberClassName.equals(other.memberClassName)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return (methodName + memberClassName).hashCode();
    }
}
