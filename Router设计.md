# 功能

- [x] 支持 AGP8
- [x] 支持ActivityResultCallback
- [x] @Param 参数注入
- [x] @Param setter 参数设置方式
- [x] 对象传参和解析（JSON）
- [x] 路由拦截处理
- [x] 路由结果回调
- [x] Parcelable Serialization Array 参数的序列化
- [ ] Uri 方式解析

## 特点

- APG8 的支持
- 自动获取路由模块名，减少了配置项
- kotlin属性和setter的支持
- ActivityResult的支持

# Router设计

- router-annotation 对应的Route、Service注解。Java库，compiler和router Android中都引用
- router-compiler 根据注解，编译时生成模块的路由映射类
- router-gradle-plugin 最终把router-compiler各模块的路由项，生成合并路由初始化类
- router 对应的Android库，对路由表类初始化，对应路由导航

## 参数

- Bundle 携带的原生参数
- Uri query 带入的参数，支持的参数类型 基本数据类型、Object
- Uri 本身也传入到 Bundle 里，支持自定义处理

|参数类型|Bundle|Query|
|----|----|-----|
|Char|Char|String|
|CharSequence|CharSequeue|String|
|String[]|putStringArray|String|
|Parcelable|Parcelable|JSON String|
|Serializable|Serializable|JSON String|
|Object|Serializable|JSON String|


来源：intent.extras 还是 Uri.query 传值

## annotations

- [x] Route 路由，Activity、Fragment、IService 的路由
- [x] Param 参数注解，注入参数
- [x] Service 实现可用

## compiler

### @Route

Activity/Fragment/IService处理，生成对应的路由类，生成的在 */generated/route 包下面。

```java
```

### @Param

注解字段参数，生成对应的Injector注入类，在注解的类 一样的包下面。

- public 字段直接赋值
- private 通过setter赋值
- 对于kt，字段与类方法冲突，会报语法错误：The following declarations have the same JVM signature (setXXX)

```java
public final class MainActivity$$Router$$Injector implements Injector {
  @Override
  public void inject(Object object) {
    MainActivity target = (MainActivity) object;
    target.test = StateUtils.getString(target, "test", target.test);
    target.setTest0(StateUtils.getBoolean(target, "test0", target.getTest0()));
  }
}
```

### AGP8 移除了 之前transform API，更改方式不同

#### <= AGP7，处理

- Route_app 

## router接口

- RouteInfo 路由基本信息
- IRouteTable 注解按模块生成的路由映射表
- Injector 参数注入
- Service 服务 init(context: Context) 初始化

### Service 设计

- service 同时添加 class name 和 path 作为 key 的 Map 里
- 默认是，单例的

## annotations

## compiler

## router参数解析和路由

APT 时，对注解的类型分析， Activity\Fragment\FragmentV4\ISerivce

Types.isSubType
Elements.getType()

如果不符合 类型，进行报错处理！

### 1. 参数的特殊处理

- [ ] java.io.Serializable 仅支持本地
- [ ] android.os.Parcelable 仅支持本地
- [ ] java.lang.Object 支持Uri参数方式（JSON）

序列化特殊处理，参数中和原生的转换

### 2. 路由表的注册

遍历 IRouteTable 接口，在 Router 方法中添加

### 3. 成功，失败 回调

### 4. 拦截器

### 文档

javapoet: https://github.com/square/javapoet
asm: https://asm.ow2.io/javadoc/org/objectweb/asm/package-summary.html


### 添加自定义类ASM，dex加载报错

```
Unable to load dex file: /data/data/io.github.classops.urouter.demo/code_cache/.overlay/base.apk/classes.dex
java.io.IOException: Failed to open dex files from /data/data/io.github.classops.urouter.demo/code_cache/.overlay/base.apk/classes.dex because: Failure to verify dex file '/data/data/io.github.classops.urouter.demo/code_cache/.overlay/base.apk/classes.dex': Invalid type descriptor: 'Lio.github.classops.urouter.generated.route.Route_app;'
	at dalvik.system.DexFile.openDexFileNative(Native Method)
	at dalvik.system.DexFile.openDexFile(DexFile.java:367)
	at dalvik.system.DexFile.<init>(DexFile.java:82)
	at dalvik.system.DexPathList.loadDexFile(DexPathList.java:439)
	at dalvik.system.DexPathList.makeDexElements(DexPathList.java:388)
	at dalvik.system.DexPathList.<init>(DexPathList.java:166)
	at dalvik.system.BaseDexClassLoader.<init>(BaseDexClassLoader.java:129)
	at dalvik.system.BaseDexClassLoader.<init>(BaseDexClassLoader.java:104)
	at dalvik.system.PathClassLoader.<init>(PathClassLoader.java:74)
	at com.android.internal.os.ClassLoaderFactory.createClassLoader(ClassLoaderFactory.java:87)
	at com.android.internal.os.ClassLoaderFactory.createClassLoader(ClassLoaderFactory.java:116)
	at android.app.ApplicationLoaders.getClassLoader(ApplicationLoaders.java:114)
	at android.app.ApplicationLoaders.getClassLoaderWithSharedLibraries(ApplicationLoaders.java:60)
	at android.app.LoadedApk.createOrUpdateClassLoaderLocked(LoadedApk.java:901)
	at android.app.LoadedApk.getClassLoader(LoadedApk.java:958)
	at android.app.LoadedApk.getResources(LoadedApk.java:1190)
	at android.app.ContextImpl.createAppContext(ContextImpl.java:2683)
	at android.app.ContextImpl.createAppContext(ContextImpl.java:2675)
	at android.app.ActivityThread.handleBindApplication(ActivityThread.java:6864)
	at android.app.ActivityThread.access$1800(ActivityThread.java:274)
	at android.app.ActivityThread$H.handleMessage(ActivityThread.java:2104)
	at android.os.Handler.dispatchMessage(Handler.java:106)
	at android.os.Looper.loop(Looper.java:233)
	at android.app.ActivityThread.main(ActivityThread.java:8030)
	at java.lang.reflect.Method.invoke(Native Method)
	at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:631)
	at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:978)
```