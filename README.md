## URouter

一个简单的路由框架，特点：

- APG8 的支持
- kotlin属性和setter的支持
- ActivityResult的支持
- 自动获取路由模块名，减少了配置项
- 支持增量编译

### 功能

- [x] 支持 AGP8
- [x] @Param 参数注入，支持 setter 参数设置（kotlin属性）
- [x] 支持ActivityResult方式，启动和处理返回结果
- [x] 路由拦截处理
- [x] 路由结果回调
- [x] Parcelable Serialization Array 参数的序列化
- [x] Uri 方式解析

### 使用方法

1. 项目顶级build.gradle添加
```groovy
plugins {
    id 'com.android.application' version '7.1.1' apply false
    id 'com.android.library' version '7.1.1' apply false
    id 'io.github.classops.urouter' version '1.0.1' apply false
}
```

2. app模块的build.gradle添加插件和依赖：
```groovy
plugins {
    id "kotlin-kapt"
    id 'io.github.classops.urouter'
}

dependencies {
    kapt "io.github.classops.urouter:router-compiler:1.0.1"
    implementation "io.github.classops.urouter:router:1.0.1"
}
```

3. 项目中使用
```kotlin
// Application 里初始化
Router.get().init(this)

// 在需要路由的 Activity/Fragment 上添加注解
@Route(path = "/test")

// 跳转方法
Router.get().build("/test")
    .withString("toast", "hello world")
    .navigate(this)
```

**URouter 支持 ActivityResult 方式 跳转页面处理结果**
```kotlin
private lateinit var launcher: ActivityResultLauncher<UriRequest>

override fun onCreate(savedInstanceState: Bundle?) {
    launcher = Router.get().registerForResult(
        this,
        ActivityResultContracts.StartActivityForResult(),
    ) {
        // 返回结果
    }
}

// 通过UriRequest构建，启动路由页面
private fun start() {
    launcher.launch(
        UriRequest.Builder("/test")
            .withString("toast", "hello world")
            .build()
    )
}
```
