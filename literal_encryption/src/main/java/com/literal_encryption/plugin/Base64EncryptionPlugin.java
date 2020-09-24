package com.literal_encryption.plugin;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.api.transform.TransformOutputProvider;
import com.android.build.gradle.AppExtension;
import com.literal_encryption.asm.Base64ClassVisitor;

import org.apache.commons.io.FileUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import static com.android.build.gradle.internal.pipeline.TransformManager.CONTENT_CLASS;
import static com.android.build.gradle.internal.pipeline.TransformManager.SCOPE_FULL_PROJECT;
import static org.objectweb.asm.ClassReader.EXPAND_FRAMES;

public class Base64EncryptionPlugin extends Transform implements Plugin<Project> {

    @Override
    public String getName() {
        return "base64Literal";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return CONTENT_CLASS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return SCOPE_FULL_PROJECT;
    }

    @Override
    public boolean isIncremental() {
        return false;
    }



    @Override
    public void apply(Project project) {
        AppExtension android = project.getExtensions().getByType(AppExtension.class);
        android.registerTransform(this);
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        System.out.println("Start");
        super.transform(transformInvocation);
        Collection<TransformInput> inputs =  transformInvocation.getInputs();

        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider();
        if (!isIncremental()) {
            outputProvider.deleteAll();
        }

        for (TransformInput input : inputs) {
        Collection<DirectoryInput> directoryInputCollection = input.getDirectoryInputs();
        for (DirectoryInput directory : directoryInputCollection) {
        File dest = outputProvider.getContentLocation(directory.getName(), directory.getContentTypes(), directory.getScopes(), Format.DIRECTORY);
        System.out.println("SRC:" + directory.getFile().getAbsolutePath());
        System.out.println("DEST: " + dest.getAbsolutePath());
        traversal(directory.getFile());
        FileUtils.copyDirectoryToDirectory(directory.getFile(), dest);
    }

        for(JarInput jarInput : input.getJarInputs()) {
        File dest = outputProvider.getContentLocation(
                jarInput.getFile().getAbsolutePath(),
        jarInput.getContentTypes(),
        jarInput.getScopes(),
        Format.JAR);
        //将修改过的字节码copy到dest，就可以实现编译期间干预字节码的目的了
        FileUtils.copyFile(jarInput.getFile(), dest);
    }
    }
    }

    private void traversal(File file) {
        if (file.isDirectory()) {
            File[] subFiles = file.listFiles();
            for (File subFile: subFiles) {
                traversal(subFile);
            }
        } else if (file.isFile()) {
            String fileName = file.getName();
            if (!fileName.endsWith(".class")) {
                return;
            }
            if (fileName.equals("R.class") || fileName.startsWith("R$")) {
                return;
            }
            if (fileName.equals("BuildConfig.class")) {
                return;
            }
            handleEncryption(file);
        }
    }

    private void handleEncryption(File file) {
        ClassReader reader = null;
        try {
            System.out.println(file.getName());
            reader = new ClassReader(new FileInputStream(file));
            ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
            ClassVisitor visitor = new Base64ClassVisitor(Opcodes.ASM4, writer);
            reader.accept(visitor, EXPAND_FRAMES);
            byte[] result = writer.toByteArray();
            FileOutputStream fos = new FileOutputStream(file.getParentFile().getAbsolutePath() + File.separator +  file.getName());
            fos.write(result);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}