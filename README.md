## Naix

A Kotlin Compiler Plugin (Not Gradle Transform Plugin) that helps **Android-Kotlin** developers to automatically output method call information through annotations.

### Usage

[![](https://jitpack.io/v/succlz123/naix.svg)](https://jitpack.io/#succlz123/naix)

~~~
    repositories {
        maven { url 'https://jitpack.io' }
    }
~~~

~~~
    dependencies {
        classpath "com.github.succlz123.naix:gradle-plugin:x.y.z"
    }
~~~

~~~
    apply plugin: 'com.github.succlz123.naix'
~~~

~~~
    implementation "com.github.succlz123.naix:compiler-runtime:x.y.z"
~~~

### Option

#### Disable Naix

~~~
    naix {
        enable = false
    }
~~~

#### Custom Logger

~~~ kotlin
    NaixOption.set { className, methodName, msg -> Log.i("Naix ", msg) }
~~~

### Sample

#### Naix

~~~ kotlin
    @Naix
    fun func1(a: String?, b: String?, c: Double?): String {
        retrun "test"
    }
~~~
↓
~~~
╔══════════════════════════════════════════════════════════════════════════════╗
║ Class#Method -> org/succlz123/naix/app/MainActivity#func1                    ║
║ Parameters -> a = 123 b = test c = 1.2                                       ║
║ Return value -> test                                                         ║
║ Elapsed time -> 0 ms                                                         ║
╚══════════════════════════════════════════════════════════════════════════════╝
~~~

#### NaixFull

~~~ kotlin
    @NaixFull
    fun func2(a: String?, b: String?, c: Long): String {
        ...... // some method call
        retrun "test2"
    }
~~~
↓
~~~
╔══════════════════════════════════════════════════════════════════════════════╗
║ Class#Method -> org/succlz123/naix/app/MainActivity#func2                    ║
║ Parameters -> a = 321 b = tset c = 2.1                                       ║
║ 1 -> 1 ms, kotlin/jvm/internal/Intrinsics#areEqual                           ║
║ 2 -> 0 ms, java/util/ArrayList#<init>                                        ║
║ 3 -> 0 ms, org/succlz123/naix/app/MainActivity#funcTest                      ║         
║ 4 -> 0 ms, java/util/ArrayList#<init>                                        ║
║ 5 -> 0 ms, java/util/ArrayList#add                                           ║
║ Return value -> test2                                                        ║
║ Elapsed time -> 1 ms                                                         ║         
╚══════════════════════════════════════════════════════════════════════════════╝
~~~

### Thanks

https://www.youtube.com/watch?v=w-GMlaziIyo

https://github.com/Leaking/Hunter

