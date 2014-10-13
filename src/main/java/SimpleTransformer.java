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

    private boolean shouldChange(CtBehavior method) {
        return !(method.getDeclaringClass().getPackageName() == null
                || !method.getDeclaringClass().getPackageName().startsWith("com.jmisur")
                || method.isEmpty()
                || method.getName().equals("toString")
                || method.getName().equals("hashCode")
                || method.getName().equals("equals")
                || method.getName().equals("$jacocoInit"));
    }

    private void changeMethod(CtBehavior method) throws NotFoundException, CannotCompileException {
        method.insertBefore(p(method));
    }

    private String p(CtBehavior method) {
        return "{SimpleMain.capture(\"" + method.getLongName() + "\", " + names(method) + ", $args);}";
    }

    private String names(CtBehavior method) {
        StringBuilder str = new StringBuilder();

        LocalVariableAttribute table = getParamsTable(method);
        if (table != null && table.tableLength() > 1) {
            str.append("new String[] {");

            for (int i = 1; i < table.tableLength(); i++) { // 0 index is 'this'
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

    private LocalVariableAttribute getParamsTable(CtBehavior method) {
        MethodInfo methodInfo = method.getMethodInfo();
        return (LocalVariableAttribute) methodInfo.getCodeAttribute().getAttribute(LocalVariableAttribute.tag);
    }
}