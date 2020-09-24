package com.literal_encryption.asm;



import com.utils.Base64Algorithm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AdviceAdapter;

public class Base64MethodVisitor extends AdviceAdapter {

    protected Base64MethodVisitor(int i, MethodVisitor methodVisitor, int i1, String s, String s1) {
        super(i, methodVisitor, i1, s, s1);
    }

    @Override
    public void visitLdcInsn(Object o) {
        if (o instanceof String) {
            System.out.println(o);
            String content = Base64Algorithm.encode((String)o);
            mv.visitLdcInsn(content);
            mv.visitMethodInsn(INVOKESTATIC, "com/utils/Base64Algorithm", "decode", "(Ljava/lang/String;)Ljava/lang/String;", false);
            return;
        }
        super.visitLdcInsn(o);
    }
}
