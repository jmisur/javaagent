import javassist.*;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

class SimpleTransformer implements ClassFileTransformer {

    public byte[] transform(ClassLoader loader, String className, Class redefiningClass, ProtectionDomain domain, byte[] bytes) throws IllegalClassFormatException {
        return transformClass(redefiningClass, bytes);
    }

    private byte[] transformClass(Class classToTransform, byte[] b) {
        ClassPool pool = ClassPool.getDefault();
        CtClass cl = null;
        try {
            cl = pool.makeClass(new java.io.ByteArrayInputStream(b));
            if (shouldTransform(cl)) {
                CtBehavior[] methods = cl.getDeclaredBehaviors();
                for (int i = 0; i < methods.length; i++) {
                    if (shouldChange(methods[i])) {
                        changeMethod(methods[i]);
                    }
                    if (shouldSetCorrelationId(methods[i])) {
                        setCorrelationId(methods[i]);
                    }
                    if (shouldMarkNewTx(methods[i])) {
                        newTx(methods[i]);
                    }
                    if (shouldMarkCommitTx(methods[i])) {
                        commitTx(methods[i]);
                    }
                    if (shouldMarkRollbackTx(methods[i])) {
                        rollbackTx(methods[i]);
                    }
                    if (shouldWrap(methods[i])) {
                        wrap(methods[i]);
                    }
                    if (shouldCaptureSocket(methods[i])) {
                        captureSocket(methods[i]);
                    }
                }
            }
            b = cl.toBytecode();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cl != null) {
                cl.detach();
            }
        }
        return b;
    }

    private boolean shouldTransform(CtClass cl) {
    private void captureSocket(CtBehavior method) throws CannotCompileException {
        method.insertAfter("{ $_ = CapturingInputStream.wrap($_); }");
    }

    private boolean shouldCaptureSocket(CtBehavior method) throws NotFoundException {
        if (method.isEmpty()) return false;
        if (!method.getName().equals("getInputStream")) return false;
        if (!method.getSignature().equals("()Ljava/io/InputStream;")) return false;
        if (!isInstanceOf(method.getDeclaringClass(), "java.net.URLConnection")) return false;

        return true;
    }

    private boolean shouldTransform(CtClass cl) throws NotFoundException {
        if (isInstanceOf(cl, "java.net.URLConnection")) return true;

        return cl.getPackageName() != null
                && !cl.getPackageName().startsWith("sun")
                && !cl.getPackageName().startsWith("java");
    }

    private void wrap(CtBehavior method) throws CannotCompileException {
        method.insertAfter("{ $_ = StatementWrapper.wrap($_); }");
    }

    private boolean shouldWrap(CtBehavior method) throws NotFoundException {
        if (method.isEmpty()) return false;
        if (!method.getName().equals("createStatement")) return false;
        if (!method.getSignature().equals("()Ljava/sql/Statement;")
                && !method.getSignature().equals("(II)Ljava/sql/Statement;")
                && !method.getSignature().equals("(III)Ljava/sql/Statement;")) return false;
        if (!isInstanceOf(method.getDeclaringClass(), "java.sql.Connection")) return false;

        return true;
    }

    private void newTx(CtBehavior method) throws CannotCompileException {
        method.insertAfter("{ SimpleMain.newTx($1, $2); }");
    }

    private boolean shouldMarkNewTx(CtBehavior method) throws NotFoundException {
        if (method.isEmpty()) return false;
        if (!method.getName().equals("doBegin")) return false;
        if (!method.getSignature().equals("(Ljava/lang/Object;Lorg/springframework/transaction/TransactionDefinition;)V"))
            return false;
        if (!isInstanceOf(method.getDeclaringClass(), "org.springframework.transaction.support.AbstractPlatformTransactionManager"))
            return false;

        return true;
    }

    private void commitTx(CtBehavior method) throws CannotCompileException {
        method.insertAfter("{ SimpleMain.commitTx($1); }");
    }

    private boolean shouldMarkCommitTx(CtBehavior method) throws NotFoundException {
        if (method.isEmpty()) return false;
        if (!method.getName().equals("doCommit")) return false;
        if (!method.getSignature().equals("(Lorg/springframework/transaction/support/DefaultTransactionStatus;)V"))
            return false;
        if (!isInstanceOf(method.getDeclaringClass(), "org.springframework.transaction.support.AbstractPlatformTransactionManager"))
            return false;

        return true;
    }

    private void rollbackTx(CtBehavior method) throws CannotCompileException {
        method.insertAfter("{ SimpleMain.rollbackTx($1); }");
    }

    private boolean shouldMarkRollbackTx(CtBehavior method) throws NotFoundException {
        if (method.isEmpty()) return false;
        if (!method.getName().equals("doRollback")) return false;
        if (!method.getSignature().equals("(Lorg/springframework/transaction/support/DefaultTransactionStatus;)V"))
            return false;
        if (!isInstanceOf(method.getDeclaringClass(), "org.springframework.transaction.support.AbstractPlatformTransactionManager"))
            return false;

        return true;
    }

    private void setCorrelationId(CtBehavior method) throws CannotCompileException, NotFoundException {
        method.getDeclaringClass().addField(CtField.make("private final String correlationId = CorrelationIdHolder.get();", method.getDeclaringClass()));
        method.insertBefore("{ CorrelationIdHolder.set(correlationId); }");
    }

    private boolean shouldSetCorrelationId(CtBehavior method) throws NotFoundException {
        if (method.isEmpty()) return false;
        if (!method.getName().equals("run")) return false;
        if (!method.getSignature().equals("()V")) return false;
        if (!isInstanceOf(method.getDeclaringClass(), "java.lang.Runnable")) return false;

        return true;
    }

    private boolean isInstanceOf(CtClass declaringClass, String className) throws NotFoundException {
        if (declaringClass == null) return false;

        if (declaringClass.getName().equals(className)) return true;

        for (CtClass ctClass : declaringClass.getInterfaces()) {
            if (ctClass.getName().equals(className)) return true;
        }

        if (declaringClass.getSuperclass() != null) {
            if (isInstanceOf(declaringClass.getSuperclass(), className)) return true;
        }

        return false;
    }

    private boolean shouldChange(CtBehavior method) {
        if (method.getDeclaringClass().getPackageName() == null) return false;
        if (!method.getDeclaringClass().getPackageName().startsWith("com.jmisur")) return false;
        if (method.isEmpty()) return false;
        if (method.getName().equals("toString")
            || method.getName().equals("hashCode")
            || method.getName().equals("equals")
            || method.getName().equals("$jacocoInit")) return false;
        if (method instanceof CtConstructor) return false;

        return true;
    }

    private void changeMethod(CtBehavior method) throws NotFoundException, CannotCompileException {
        method.insertBefore(before(method));
        method.insertAfter(after(method));
        method.addCatch(catched(method), ClassPool.getDefault().get("java.lang.Exception"));
    }

    private String catched(CtBehavior method) {
        return "{ SimpleMain.ex(\"" + method.getLongName() + "\", " + names(method) + ", $args, $e); throw $e; }";
    }

    private String before(CtBehavior method) {
        return "{ SimpleMain.before(\"" + method.getLongName() + "\", " + names(method) + ", $args); }";
    }

    private String after(CtBehavior method) {
        return "{ SimpleMain.after(\"" + method.getLongName() + "\", " + names(method) + ", $args, $_); }";
    }

    private String names(CtBehavior method) {
        StringBuilder str = new StringBuilder();

        LocalVariableAttribute table = getParamsTable(method);
        if (table != null && table.tableLength() > 1) {
            str.append("new String[] {");

            int start = Modifier.isStatic(method.getModifiers()) ? 0 : 1;
            for (int i = start; i < start + paramCount(method); i++) { // 0 index is 'this'
                str.append("\"" + table.variableName(i) + "\", ");
            }

            str.deleteCharAt(str.length() - 1);
            str.deleteCharAt(str.length() - 1);
            str.append("}");
        } else {
            str.append("new String[0]");
        }

        return str.toString();
    }

    private int paramCount(CtBehavior method) {
        try {
            return method.getParameterTypes().length;
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private LocalVariableAttribute getParamsTable(CtBehavior method) {
        MethodInfo methodInfo = method.getMethodInfo();
        return (LocalVariableAttribute) methodInfo.getCodeAttribute().getAttribute(LocalVariableAttribute.tag);
    }
}