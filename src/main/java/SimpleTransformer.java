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
            CtBehavior[] methods = cl.getDeclaredBehaviors();
            for (int i = 0; i < methods.length; i++) {
                if (shouldChange(methods[i])) {
                    changeMethod(methods[i]);
                }
                if (shouldSetCorrelationId(methods[i])) {
                    setCorrelationId(methods[i]);
                }
                if (shouldMarkTx(methods[i])) {
                    markTx(methods[i]);
                }
                if (shouldWrap(methods[i])) {
                    wrap(methods[i]);
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

    private void wrap(CtBehavior method) throws CannotCompileException {
        method.insertAfter("{ $_ = StatementWrapper.wrap($_);  }");
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

    private void markTx(CtBehavior method) throws CannotCompileException {
        method.insertAfter("{ SimpleMain.newTx($_.getJoinpointIdentification(), " +
                "$_.getTransactionStatus().isNewTransaction(), " +
                "$_.getTransactionAttribute().getPropagationBehavior()); }");
    }

    private boolean shouldMarkTx(CtBehavior method) {
        if (method.isEmpty()) return false;
        if (!method.getName().equals("createTransactionIfNecessary")) return false;
        if (!method.getSignature().equals("(Ljava/lang/reflect/Method;Ljava/lang/Class;)L" +
                "org/springframework/transaction/interceptor/TransactionAspectSupport$TransactionInfo;")) ;
        if (!method.getDeclaringClass().getName().equals("org.springframework.transaction.interceptor.TransactionAspectSupport"))
            return false;

        return true;
    }

    private void setCorrelationId(CtBehavior method) throws CannotCompileException, NotFoundException {
        method.getDeclaringClass().addField(CtField.make("private final String correlationId = CorrelationIdHolder.get();", method.getDeclaringClass()));
        method.insertBefore("{CorrelationIdHolder.set(correlationId);}");
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

        for (CtClass ctClass : declaringClass.getInterfaces()) {
            if (ctClass.getName().equals(className)) return true;
        }

        if (declaringClass.getSuperclass() != null) {
            if (isInstanceOf(declaringClass.getSuperclass(), className)) return true;
        }

        return false;
    }

    private boolean shouldChange(CtBehavior method) {
        return !(method.getDeclaringClass().getPackageName() == null
                || !method.getDeclaringClass().getPackageName().startsWith("com.jmisur")
                || method.isEmpty()
                || method.getName().equals("toString")
                || method.getName().equals("hashCode")
                || method.getName().equals("equals")
                || method.getName().equals("$jacocoInit")
                || method instanceof CtConstructor);
    }

    private void changeMethod(CtBehavior method) throws NotFoundException, CannotCompileException {
        method.insertBefore(before(method));
        method.insertAfter(after(method));
    }

    private String before(CtBehavior method) {
        return "{SimpleMain.before(\"" + method.getLongName() + "\", " + names(method) + ", $args);}";
    }

    private String after(CtBehavior method) {
        return "{SimpleMain.after(\"" + method.getLongName() + "\", " + names(method) + ", $args, $_);}";
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