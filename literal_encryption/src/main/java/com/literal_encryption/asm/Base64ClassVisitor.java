package com.literal_encryption.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ASM4;

public class Base64ClassVisitor extends ClassVisitor {

    public Base64ClassVisitor(int i) {
        super(i);
    }

    public Base64ClassVisitor(int i, ClassVisitor classVisitor) {
        super(i, classVisitor);
    }

    @Override
    public MethodVisitor visitMethod(int i, String s, String s1, String s2, String[] strings) {
        MethodVisitor methodVisitor = cv.visitMethod(i, s, s1, s2, strings);
        return new Base64MethodVisitor(ASM4, methodVisitor,i, s, s1);
    }
}
