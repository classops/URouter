## URouter

一个简单的路由框架

特点：

- AGP8 的支持
- Kotlin属性和setter的支持
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
    id 'io.github.classops.urouter' version '1.0.2' apply false
}
```

2. app模块的build.gradle添加插件和依赖：
```groovy
plugins {
    id "kotlin-kapt"
    id 'io.github.classops.urouter'
}

dependencies {
    kapt "io.github.classops.urouter:router-compiler:1.0.2"
    implementation "io.github.classops.urouter:router:1.0.2"
}
```

3. Application中初始化

```kotlin
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Router.get().init(this)
    }
}
```

4. 项目中使用
```kotlin
// 在需要路由的 Activity/Fragment 上添加注解
@Route(path = "/home")
class MainActivity : AppCompatActivity() {

    @Param
    var toast: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // 解析参数到 toast 字段
        Router.get().inject(this)

        // 点击跳转新页面
        val btnNav = findViewById<Button>(R.id.btnNav)
        btnNav.setOnClickListener {
            Router.get().build("/home")
                .withString("toast", "hello world")
                .navigate(this)
        }

        // 接收 toast 参数
        if (toast?.isNotEmpty() == true) {
            Toast.makeText(this, toast, Toast.LENGTH_LONG)
                .show()
        }
    }
}
```

#### URouter 支持 ActivityResult 方式 跳转页面处理结果

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
