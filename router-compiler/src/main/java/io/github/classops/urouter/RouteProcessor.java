package io.github.classops.urouter;

import com.google.auto.service.AutoService;
import io.github.classops.urouter.annotation.Param;
import io.github.classops.urouter.annotation.Route;
import io.github.classops.urouter.route.ParamType;
import io.github.classops.urouter.route.RouteInfo;
import io.github.classops.urouter.route.RouteType;
import io.github.classops.urouter.utils.MemberUtils;
import com.squareup.javapoet.*;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;


@AutoService(Processor.class)
@SupportedAnnotationTypes({"io.github.classops.urouter.annotation.Route", "io.github.classops.urouter.annotation.Param"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class RouteProcessor extends BaseProcessor {

    static final String ROUTER = "Router";
    static final String CLASS_SEP = "$$";
    static final String ROUTER_PKG = "io.github.classops.urouter";
    static final Map<Integer, String> sTypeMapping = new HashMap<>();

    static {
        sTypeMapping.put(ParamType.BOOLEAN, "Boolean");
        sTypeMapping.put(ParamType.BYTE, "Byte");
        sTypeMapping.put(ParamType.SHORT, "Short");
        sTypeMapping.put(ParamType.INT, "Int");
        sTypeMapping.put(ParamType.LONG, "Long");
        sTypeMapping.put(ParamType.CHAR, "Char");
        sTypeMapping.put(ParamType.FLOAT, "Float");
        sTypeMapping.put(ParamType.DOUBLE, "Double");
        sTypeMapping.put(ParamType.CHARSEQUENCE, "CharSequence");
        sTypeMapping.put(ParamType.STRING, "String");
        sTypeMapping.put(ParamType.PARCELABLE, "String");
        sTypeMapping.put(ParamType.SERIALIZABLE, "String");
        sTypeMapping.put(ParamType.OBJECT, "String");
    }

    private TypeMirror mActivityType;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mActivityType = mElementUtils.getTypeElement("android.app.Activity").asType();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Route.class);
        if (elements == null || elements.isEmpty()) {
            return false;
        }

        Map<String, RouteItem> routes = new HashMap<>();
        for (Element element : elements) {
            if (element.getKind() != ElementKind.CLASS) {
                continue;
            }

            TypeElement typeElement = (TypeElement) element;
            Route route = typeElement.getAnnotation(Route.class);
            byte routeType = getRouteType(typeElement.asType());
            if (routeType == RouteType.UNKNOWN) {
                continue;
            }

            // 获取参数信息，字段名 和 类型
            List<? extends Element> members = mElementUtils.getAllMembers(typeElement);
            Map<String, Integer> paramsType = new HashMap<>();
            try {
                genInjectorJavaFile(typeElement, getFieldInfos(members, paramsType));
            } catch (IOException e) {
                e.printStackTrace();
            }
            routes.put(route.path(), new RouteItem(routeType, typeElement, paramsType));
        }

        try {
            genTableJavaFile(routes);
            genServiceJavaFile(routes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    /**
     * 获取类注解的所有字段信息
     */
    private List<FieldInfo> getFieldInfos(List<? extends Element> members, Map<String, Integer> paramsType) {
        // element, field name, field type, param name, param type
        if (members == null || members.isEmpty()) {
            return Collections.emptyList();
        }
        List<FieldInfo> fieldInfoList = new ArrayList<>();
        for (Element member : members) {
            if (member.getKind() != ElementKind.FIELD) {
                continue;
            }

            VariableElement varElement = (VariableElement) member;
            Param param = member.getAnnotation(Param.class);
            if (param == null) {
                continue;
            }

            TypeMirror typeMirror = varElement.asType();
            String fieldName = varElement.getSimpleName().toString();
            String paramName = param.name();
            if (StringUtils.isBlank(paramName)) {
                paramName = fieldName;
            }

            // check static modifier
            if (MemberUtils.isStatic(varElement)) {
                mMessager.printMessage(Diagnostic.Kind.ERROR,
                        "field " + varElement.getSimpleName() + " must be non-static!");
            }
            // check final modifier
            if (MemberUtils.isFinal(varElement)) {
                mMessager.printMessage(Diagnostic.Kind.ERROR,
                        "field " + varElement.getSimpleName() + " must be non-final!");
            }

            FieldType fieldType = getFieldType(varElement);
            if (fieldType == FieldType.NOT_SUPPORTED) {
                mMessager.printMessage(Diagnostic.Kind.ERROR,
                        "field " + varElement.getSimpleName() + " is not supported!");
                mMessager.printMessage(Diagnostic.Kind.ERROR,
                        "make field " + varElement.getSimpleName() + " public, or add setter/getter!");
            }

            int paramType = getParamType(typeMirror);
            paramsType.put(paramName, getParamType(typeMirror));
            fieldInfoList.add(new FieldInfo(varElement, fieldType, paramName, paramType));
        }
        return fieldInfoList;
    }

    private byte getRouteType(TypeMirror typeMirror) {
        // android.app.Fragment
        // androidx.fragment.app.Fragment
        TypeMirror typeActivity = mElementUtils.getTypeElement("android.app.Activity").asType();
        if (mTypeUtils.isSubtype(typeMirror, typeActivity)) {
            return RouteType.ACTIVITY;
        }

        TypeMirror typeFragment = mElementUtils.getTypeElement("androidx.fragment.app.Fragment").asType();
        if (mTypeUtils.isSubtype(typeMirror, typeFragment)) {
            return RouteType.FRAGMENT;
        }

        TypeMirror typeService = mElementUtils.getTypeElement(ROUTER_PKG + ".service.IService").asType();
        if (mTypeUtils.isSubtype(typeMirror, typeService)) {
            return RouteType.SERVICE;
        }

        return RouteType.UNKNOWN;
    }

    private boolean isAndroidActivity(Element classElement) {
        return mTypeUtils.isAssignable(classElement.asType(), mActivityType);
    }

    private FieldType getFieldType(VariableElement element) {
        if (MemberUtils.isPublic(element)) return FieldType.FIELD;

        // 获取字段的getter\setter方法，判断 直接赋值还是通过setter赋值
        String fieldName = element.getSimpleName().toString();
        String name = fieldName.substring(0, 1).toUpperCase() + (fieldName.length() > 1 ? fieldName.substring(1) : "");
        String getterName = "get" + name;
        String isGetterName = "is" + name;
        String setterName = "set" + name;
        // kotlin property get/set method
        String isGetterNameKt = null;
        String setterNameKt = null;
        // kotlin bool `is` prefix
        if (fieldName.length() > 2 && fieldName.startsWith("is") && Character.isUpperCase(fieldName.charAt(2))) {
            String nameKt = fieldName.substring(2);
            isGetterNameKt = "is" + nameKt;
            setterNameKt = "set" + nameKt;
        }

        boolean hasGetter = false;
        boolean hasIsGetter = false;
        boolean hasSetter = false;
        boolean isKtBoolProperty = false;

        // handle java-setter, kotlin-property 模式
        List<? extends Element> members = element.getEnclosingElement().getEnclosedElements();

        for (Element m : members) {
            if (m.getKind() != ElementKind.METHOD) continue;

            ExecutableElement method = (ExecutableElement) m;
            String methodName = m.getSimpleName().toString();

            if (!hasGetter && getterName.equals(methodName) &&
                    MemberUtils.isPublic(method) &&
                    method.getParameters().isEmpty() &&
                    mTypeUtils.isAssignable(method.getReturnType(), element.asType())) {
                hasGetter = true;
                if (hasSetter) {
                    break;
                } else {
                    continue;
                }
            }

            if (!hasIsGetter && isGetterName.equals(methodName) &&
                    MemberUtils.isPublic(method) &&
                    method.getParameters().isEmpty() &&
                    mTypeUtils.isAssignable(method.getReturnType(), element.asType())) {
                hasIsGetter = true;
                if (hasSetter) {
                    break;
                }
            }

            if (!hasGetter && isGetterNameKt != null && isGetterNameKt.equals(methodName) &&
                    MemberUtils.isPublic(method) &&
                    method.getParameters().isEmpty() &&
                    mTypeUtils.isAssignable(method.getReturnType(), element.asType())) {
                hasIsGetter = true;
                isKtBoolProperty = true;
                if (hasSetter) {
                    break;
                }
            }

            if (!hasSetter && setterName.equals(methodName) &&
                    MemberUtils.isPublic(method) &&
                    method.getParameters().size() == 1 &&
                    mTypeUtils.isAssignable(method.getParameters().get(0).asType(), element.asType()) &&
                    method.getReturnType() instanceof NoType) {
                hasSetter = true;
                if (hasGetter || hasIsGetter) {
                    break;
                }
            }

            if (!hasSetter && setterNameKt != null && setterNameKt.equals(methodName) &&
                    MemberUtils.isPublic(method) &&
                    method.getParameters().size() == 1 &&
                    mTypeUtils.isAssignable(method.getParameters().get(0).asType(), element.asType()) &&
                    method.getReturnType() instanceof NoType) {
                hasSetter = true;
                isKtBoolProperty = true;
                if (hasIsGetter) {
                    break;
                }
            }
        }

        if (isKtBoolProperty && hasSetter) {
            return FieldType.BOOL_PROPERTY_KT;
        } else if (hasIsGetter && hasSetter) {
            return FieldType.BOOL_PROPERTY;
        } else if (hasGetter && hasSetter) {
            return FieldType.PROPERTY;
        } else {
            return FieldType.NOT_SUPPORTED;
        }
    }

    private int getParamType(TypeMirror typeMirror) {
        if (ParamType.isPrimitiveType(typeMirror.getKind().ordinal())) {
            return typeMirror.getKind().ordinal();
        }

        switch (typeMirror.toString()) {
            case "java.lang.Boolean":
                return ParamType.BOOLEAN;
            case "java.lang.Byte":
                return ParamType.BYTE;
            case "java.lang.Short":
                return ParamType.SHORT;
            case "java.lang.Integer":
                return ParamType.INT;
            case "java.lang.Long":
                return ParamType.LONG;
            case "java.lang.Character":
                return ParamType.CHAR;
            case "java.lang.Float":
                return ParamType.FLOAT;
            case "java.lang.Double":
                return ParamType.DOUBLE;
            case "java.lang.String":
                return ParamType.STRING;
            case "java.lang.CharSequence":
                return ParamType.CHARSEQUENCE;
            default:
                if (mTypeUtils.isSubtype(typeMirror, mParcelableType)) {
                    return ParamType.PARCELABLE;
                } else if (mTypeUtils.isSubtype(typeMirror, mSerializableType) &&
                    !mTypeUtils.isAssignable(typeMirror, mListType)) {
                    // 忽略ArrayList类型
                    return ParamType.SERIALIZABLE;
                } else {
                    return ParamType.OBJECT;
                }
        }
    }

    private void genInjectorJavaFile(Element classElement, List<FieldInfo> list) throws IOException {
        if (list == null || list.isEmpty()) {
            return;
        }
        ClassName serilizationClassName = ClassName.get(RouteProcessor.ROUTER_PKG + ".service", "SerializationService");
        ClassName routerClass = ClassName.get(ROUTER_PKG, "Router");

        MethodSpec queryMethod = generateQueryMethod(classElement, list);
        MethodSpec injectMethod = generateInjectMethod(classElement, list);
        ClassName injectInterface = ClassName.get(ROUTER_PKG, "Injector");
        TypeSpec typeSpec = TypeSpec.classBuilder(classElement.getSimpleName() + CLASS_SEP + ROUTER + CLASS_SEP + "Injector")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(injectInterface)
                .addField(ClassName.get(ROUTER_PKG + ".service", "SerializationService"),
                        "service", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addStatement("service = $T.get().route($T.class)", routerClass,
                                serilizationClassName)
                        .build())
                .addMethod(queryMethod)
                .addMethod(injectMethod)
                .build();
        String packageName = mElementUtils.getPackageOf(classElement).getQualifiedName().toString();
        JavaFile.builder(packageName, typeSpec)
                .build()
                .writeTo(mFiler);
    }

    private MethodSpec generateQueryMethod(Element classElement, List<FieldInfo> list) {
        MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder("query")
                .addParameter(ParameterSpec.builder(ClassName.get(ROUTER_PKG, "UriRequest"), "request")
                        .build())
                .addParameter(ParameterSpec.builder(ClassName.get(ROUTER_PKG + ".route", "RouteInfo"), "routeInfo")
                        .build())
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get("android.os", "Bundle"));

//        Uri uri = request.getUri();
//        Bundle extras = request.getExtras();
//        if (extras == null) {
//            extras = new Bundle();
//        }
//        Map<String, Integer> paramsTypeMap = routeInfo.getParamsType();
//        Set<String> keys = uri.getQueryParameterNames();
//        if (keys != null) {
//            for (String key : keys) {
//                String value = uri.getQueryParameter(key);
//                Integer paramType = paramsTypeMap == null ? null : paramsTypeMap.get(key);
//                addParam(extras, key, value, paramType != null ? paramType : ParamType.STRING);
//            }
//        }
//        return extras;

        methodSpecBuilder
                .addStatement("$T uri = request.getUri()",
                        ClassName.get("android.net", "Uri"))
                .addStatement("$T extras = request.getExtras()",
                        ClassName.get("android.os", "Bundle"))
                .beginControlFlow("if (extras == null)")
                .addStatement("extras = new $T()",
                        ClassName.get("android.os", "Bundle"))
                .endControlFlow()
                .addStatement("$T keys = uri.getQueryParameterNames()",
                        ParameterizedTypeName.get(Set.class, String.class));

//                .addStatement("Set<String> keys = uri.getQueryParameterNames()")
//                .beginControlFlow("if (keys != null)")
//                .beginControlFlow("for ($T key : keys)", String.class)
//                .addStatement("$T value =  = uri.getQueryParameter(key)", String.class)
//                .addStatement("")
//                .endControlFlow()
//                .endControlFlow();


        methodSpecBuilder.beginControlFlow("if (keys != null && keys.size() > 0)");
        for (FieldInfo fieldInfo : list) {
            addParam(methodSpecBuilder, fieldInfo);
        }
        methodSpecBuilder.endControlFlow();
        return methodSpecBuilder.addStatement("return extras")
                .build();
    }

    private void addParam(MethodSpec.Builder builder, FieldInfo fieldInfo) {
        // FIXME 默认值
        switch (fieldInfo.paramType) {
            case ParamType.BOOLEAN:
                builder.addStatement("extras.putBoolean($S, $T.toBoolean(uri.getQueryParameter($S)))",
                        fieldInfo.paramName, ClassName.get(ROUTER_PKG, "Utils"), fieldInfo.paramName);
                break;

            case ParamType.BYTE:
                builder.addStatement("extras.putByte($S, $T.toByte(uri.getQueryParameter($S)))", fieldInfo.paramName,
                        ClassName.get(ROUTER_PKG, "Utils"), fieldInfo.paramName);
                break;

            case ParamType.SHORT:
                builder.addStatement("extras.putShort($S, $T.toShort(uri.getQueryParameter($S)))", fieldInfo.paramName,
                        ClassName.get(ROUTER_PKG, "Utils"), fieldInfo.paramName);
                break;

            case ParamType.INT:
                builder.addStatement("extras.putInt($S, $T.toInt(uri.getQueryParameter($S)))", fieldInfo.paramName,
                        ClassName.get(ROUTER_PKG, "Utils"), fieldInfo.paramName);
                break;

            case ParamType.LONG:
                builder.addStatement("extras.putLong($S, $T.toLong(uri.getQueryParameter($S)))", fieldInfo.paramName,
                        ClassName.get(ROUTER_PKG, "Utils"), fieldInfo.paramName);
                break;

            case ParamType.CHAR:
                builder.addStatement("extras.putInt($S, $T.toChar(uri.getQueryParameter($S)))", fieldInfo.paramName,
                        ClassName.get(ROUTER_PKG, "Utils"), fieldInfo.paramName);
                break;

            case ParamType.FLOAT:
                builder.addStatement("extras.putFloat($S, $T.toFloat(uri.getQueryParameter($S)))", fieldInfo.paramName,
                        ClassName.get(ROUTER_PKG, "Utils"), fieldInfo.paramName);
                break;

            case ParamType.DOUBLE:
                builder.addStatement("extras.putDouble($S, $T.toDouble(uri.getQueryParameter($S)))",
                        fieldInfo.paramName, ClassName.get(ROUTER_PKG, "Utils"), fieldInfo.paramName);
                break;

            case ParamType.PARCELABLE:
                builder.addStatement("extras.putParcelable($S, ($T) service.parseObject(uri.getQueryParameter($S), new $T() {}.getType()))",
                                fieldInfo.paramName,
                                ClassName.get("android.os", "Parcelable"),
                                fieldInfo.paramName,
                                ParameterizedTypeName.get(ClassName.get(ROUTER_PKG,"TypeToken"),
                                        TypeName.get(fieldInfo.element.asType())));
                break;

            case ParamType.SERIALIZABLE:
                builder.addStatement("extras.putSerializable($S, ($T) service.parseObject(uri.getQueryParameter($S), new $T() {}.getType()))",
                                fieldInfo.paramName,
                                Serializable.class,
                                fieldInfo.paramName,
                                ParameterizedTypeName.get(ClassName.get(ROUTER_PKG,"TypeToken"),
                                        TypeName.get(fieldInfo.element.asType())));
                break;

            case ParamType.CHARSEQUENCE:
                builder.addStatement("extras.putCharSequence($S, uri.getQueryParameter($S))", fieldInfo.paramName,
                        fieldInfo.paramName);
                break;

            default:
                // OBJECT/STRING 直接使用String
                builder.addStatement("extras.putString($S, uri.getQueryParameter($S))", fieldInfo.paramName,
                        fieldInfo.paramName);
                break;
        }
    }

    private MethodSpec generateInjectMethod(Element classElement, List<FieldInfo> list) {
        MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder("inject")
                .addParameter(ParameterSpec.builder(Object.class, "object")
                        .build())
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addStatement("$T target = ($T) object", classElement.asType(), classElement.asType());

        /*
        // SerializationService service = ...;
        ClassName serilizationClassName = ClassName.get(RouteProcessor.ROUTER_PKG + ".service", "SerializationService");
        ClassName routerClass = ClassName.get(ROUTER_PKG, "Router");
        methodSpecBuilder.addStatement("$T service = $T.get().route($T.class)",
                serilizationClassName,
                routerClass,
                serilizationClassName);
        */

        ClassName bundleClass = ClassName.get("android.os", "Bundle");
        if (isAndroidActivity(classElement)) {
            methodSpecBuilder.addStatement("$T args = target.getIntent().getExtras()", bundleClass);
        } else {
            methodSpecBuilder.addStatement("$T args = target.getArguments()", bundleClass);
        }

        for (FieldInfo fieldInfo : list) {
            String fieldName = fieldInfo.element.getSimpleName().toString();
            switch (fieldInfo.fieldType) {
                case FIELD:
                    // $T.{fieldName} = extras.getXX("")
                    if (fieldInfo.paramType == ParamType.PARCELABLE) {
                        // target.XXX = args.getParcelable("XXX");
                        methodSpecBuilder.addStatement("target.$N = args.getParcelable($S)",
                                fieldName, fieldInfo.paramName);
                    } else if (fieldInfo.paramType == ParamType.SERIALIZABLE) {
                        // activity.sa = (SA) args.getSerializable("sa");
                        methodSpecBuilder.addStatement("target.$N = ($T) args.getSerializable($S)",
                                fieldName, fieldInfo.element.asType(), fieldInfo.paramName);
                    } else if (fieldInfo.paramType == ParamType.OBJECT) {
                        methodSpecBuilder.addStatement("target.$N = service.parseObject($T.getString(target, $S), new $T() {}.getType())",
                                fieldName,
                                ClassName.get(RouteProcessor.ROUTER_PKG + ".utils", "StateUtils"),
                                fieldInfo.paramName,
                                ParameterizedTypeName.get(ClassName.get(ROUTER_PKG,"TypeToken"),
                                        TypeName.get(fieldInfo.element.asType())));
                    } else {
                        methodSpecBuilder.addStatement("target.$N = $T.get$N(target, $S, target.$N)",
                                fieldName,
                                ClassName.get(RouteProcessor.ROUTER_PKG + ".utils", "StateUtils"),
                                RouteProcessor.sTypeMapping.get(fieldInfo.paramType),
                                fieldInfo.paramName,
                                fieldName);
                    }
                    break;

                case PROPERTY:
                case BOOL_PROPERTY:
                case BOOL_PROPERTY_KT:
                    // setterName
                    // target.setterName(extras.getXXX("{fieldInfo.paramName}"))
                    String upperFieldName;
                    if (fieldInfo.fieldType == FieldType.BOOL_PROPERTY_KT) {
                        upperFieldName = fieldName.substring(2);
                    } else {
                        upperFieldName = fieldName.substring(0, 1).toUpperCase() +
                                (fieldName.length() > 1 ? fieldName.substring(1) : "");
                    }
                    String setterName = "set" + upperFieldName;
                    String getterName;
                    if (fieldInfo.fieldType == FieldType.BOOL_PROPERTY) {
                        getterName = "is" + upperFieldName;
                    } else if (fieldInfo.fieldType == FieldType.BOOL_PROPERTY_KT) {
                        getterName = "is" + upperFieldName;
                    } else {
                        getterName = "get" + upperFieldName;
                    }

                    if (fieldInfo.paramType == ParamType.PARCELABLE) {
                        // target.setXXX(args.getParcelable("name"));
                        methodSpecBuilder.addStatement("target.$N(args.getParcelable($S))",
                                setterName,
                                fieldInfo.paramName);
                    } else if (fieldInfo.paramType == ParamType.SERIALIZABLE) {
                        // target.setXXX((SA) args.getSerializable("name"));
                        methodSpecBuilder.addStatement("target.$N($T) args.getSerializable($S)",
                                setterName,
                                fieldInfo.element.asType(),
                                fieldInfo.paramName);
                    } else if (fieldInfo.paramType == ParamType.OBJECT) {
                        methodSpecBuilder.addStatement("target.$N(service.parseObject($T.getString(target, $S), new $T() {}.getType()))",
                                setterName,
                                ClassName.get(RouteProcessor.ROUTER_PKG + ".utils", "StateUtils"),
                                fieldInfo.paramName,
                                ParameterizedTypeName.get(ClassName.get(ROUTER_PKG,"TypeToken"),
                                        TypeName.get(fieldInfo.element.asType())));
                    } else {
                        methodSpecBuilder.addStatement("target.$N($T.get$N(target, $S, target.$N()))",
                                setterName,
                                ClassName.get(RouteProcessor.ROUTER_PKG + ".utils", "StateUtils"),
                                RouteProcessor.sTypeMapping.get(fieldInfo.paramType),
                                fieldInfo.paramName,
                                getterName);
                    }
                    break;

                default:
                    break;
            }
        }
        return methodSpecBuilder.build();
    }

    private void genTableJavaFile(Map<String, RouteItem> routes) throws IOException {
        ClassName routeTableInterface = ClassName.get(ROUTER_PKG + ".route", "IRouteTable");
        ParameterizedTypeName inputMapType = ParameterizedTypeName.get(ClassName.get(Map.class),
                ClassName.get(String.class), ClassName.get(RouteInfo.class));
        MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder("load")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(
                        ParameterSpec.builder(inputMapType, "table")
                                .build()
                )
                .returns(void.class);
        for (String path : routes.keySet()) {
            RouteItem routeItem = routes.get(path);
            methodSpecBuilder.addStatement("table.put($S, $T.build((byte) $L, $N.class, $N))", path, RouteInfo.class,
                    routeItem.getType(), routeItem.getTypeElement().getQualifiedName().toString(), buildParamsMap(routeItem.getParamsType()));
        }
        MethodSpec methodSpec = methodSpecBuilder.build();
        TypeSpec typeSpec = TypeSpec.classBuilder(ROUTER + CLASS_SEP + mProjectName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(routeTableInterface)
                .addMethod(methodSpec)
                .build();
        JavaFile.builder(ROUTER_PKG + ".generated.route", typeSpec)
                .build()
                .writeTo(mFiler);
    }

    private void genServiceJavaFile(Map<String, RouteItem> routes) throws IOException {
        ClassName routeTableInterface = ClassName.get(ROUTER_PKG + ".route", "IServiceTable");

        ParameterizedTypeName inputMapType = ParameterizedTypeName.get(ClassName.get(Map.class),
                ClassName.get(String.class), ClassName.get(RouteInfo.class));
        MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder("load")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(inputMapType, "table")
                        .build())
                .returns(void.class);

        for (String path : routes.keySet()) {
            RouteItem routeItem = routes.get(path);
            List<? extends TypeMirror> interfaces = routeItem.getTypeElement().getInterfaces();
            TypeMirror serviceType = mElementUtils.getTypeElement(ROUTER_PKG + ".service.IService").asType();
            //  判断是否接口
            for (TypeMirror i : interfaces) {
                if (mTypeUtils.isSubtype(i, serviceType)) {
                    // 实现的接口
                    methodSpecBuilder.addStatement("table.put($S, $T.build((byte) $L, $N.class, $N))",
                            i.toString(), RouteInfo.class, routeItem.getType(), routeItem.getTypeElement().getQualifiedName(),
                            buildParamsMap(routeItem.getParamsType()));
                }
            }
        }
        MethodSpec methodSpec = methodSpecBuilder.build();
        TypeSpec typeSpec = TypeSpec.classBuilder(ROUTER + CLASS_SEP + "Service" + CLASS_SEP + mProjectName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(routeTableInterface)
                .addMethod(methodSpec)
                .build();
        JavaFile.builder(ROUTER_PKG + ".generated.route", typeSpec)
                .build()
                .writeTo(mFiler);
    }

    private String buildParamsMap(Map<String, Integer> paramsType) {
        if (paramsType == null || paramsType.isEmpty()) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        for (String key : paramsType.keySet()) {
            Integer value = paramsType.get(key);
            sb.append(".add(\"")
                    .append(key)
                    .append("\", ")
                    .append(value)
                    .append(")");
        }
        return ROUTER_PKG + ".MapUtils.newBuilder()" + sb + ".build()";
    }

    enum FieldType {
        FIELD,
        PROPERTY,
        BOOL_PROPERTY,
        BOOL_PROPERTY_KT,
        NOT_SUPPORTED
    }

    static class FieldInfo {
        Element element;
        FieldType fieldType;
        String paramName;
        int paramType;

        public FieldInfo(Element element, FieldType fieldType, String paramName, int paramType) {
            this.element = element;
            this.fieldType = fieldType;
            this.paramName = paramName;
            this.paramType = paramType;
        }
    }

}
