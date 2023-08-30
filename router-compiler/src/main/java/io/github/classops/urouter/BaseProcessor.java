package io.github.classops.urouter;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public abstract class BaseProcessor extends AbstractProcessor {

    public static final String LIST = "java.util.List";
    public static final String PARCELABLE = "android.os.Parcelable";
    public static final String SERIALIZABLE = "java.io.Serializable";

    protected Filer mFiler;
    protected Elements mElementUtils;
    protected Types mTypeUtils;
    protected Map<String, String> mOptions;
    protected String mProjectName;
    protected TypeMirror mListType;
    protected TypeMirror mParcelableType;
    protected TypeMirror mSerializableType;
    protected Messager mMessager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.mFiler = processingEnv.getFiler();
        this.mElementUtils = processingEnv.getElementUtils();
        this.mTypeUtils = processingEnv.getTypeUtils();
        this.mOptions = processingEnv.getOptions();
        this.mMessager = processingEnv.getMessager();
        this.mParcelableType = mElementUtils.getTypeElement(PARCELABLE).asType();
        this.mSerializableType = mElementUtils.getTypeElement(SERIALIZABLE).asType();
        this.mListType = mTypeUtils.getDeclaredType(mElementUtils.getTypeElement(LIST),
                mTypeUtils.getWildcardType(null, null));
        Map<String, String> options = processingEnv.getOptions();
        options.forEach((key, value) -> {
            mMessager.printMessage(Diagnostic.Kind.NOTE, "option key:" + key + ", value: " + value);
        });
        this.mProjectName = getProjectName();
    }

    String getProjectName() {
        try {
            String output = mOptions.get("kapt.kotlin.generated");
            if (output == null) {
                FileObject fileObj = mFiler.createResource(StandardLocation.SOURCE_OUTPUT, "", ".project_test", (Element[]) null);
                Path path = Paths.get(fileObj.toUri());
                fileObj.delete();
                output = path.toString();
            }
            int index = output.lastIndexOf(File.separator + "build" + File.separator);
            if (index != -1) {
                String module = output.substring(0, index);
                File moduleFile = new File(module);
                return moduleFile.getName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void printStandardPath(StandardJavaFileManager fm, JavaFileManager.Location location) {
        try {
            Iterable<? extends java.io.File> files = fm.getLocation(location);
            if (files != null) {
                for (File file : files) {
                    try {
                        System.out.println("location: " + location + ", path: " + file.getCanonicalPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
