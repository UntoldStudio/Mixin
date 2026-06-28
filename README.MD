# Mixin for Java 25

A lightweight, standalone bytecode weaving framework based on [ASM](https://asm.ow2.io/) and the SpongePowered Mixin project, tailored for modern Java applications.

## Installation

Add the JitPack repository and dependency to your `build.gradle`:

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    implementation 'com.github.UntoldStudio:Mixin:0.1.0'
    annotationProcessor 'com.github.UntoldStudio:Mixin:0.1.0:processor'
}
```

(Replace `0.1.0` with the latest release tag or `main-SNAPSHOT`.)

## Core Concepts

- **Mixin Class**: A class annotated with `@Mixin` that defines how to modify a target class.
- **Target Class**: The original class you want to enhance or modify.
- **Injections**: Methods annotated with `@Inject`, `@Redirect`, `@ModifyArg`, etc., which are woven into specific points in the target class.
- **Configuration File**: A JSON file (`mixins.<name>.json`) that tells the framework which mixins to apply.

## Quick Start

### 1. Create a target class

```java
package com.example;

public class Greeter {
    public void sayHello(String name) {
        System.out.println("Hello, " + name);
    }
}
```

### 2. Create a Mixin class

```java
package com.example.mixin;

import com.example.Greeter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Greeter.class)
public abstract class GreeterMixin {
    @Inject(method = "sayHello", at = @At("HEAD"))
    private void onSayHello(String name, CallbackInfo ci) {
        System.out.println("[Mixin] About to greet " + name);
    }
}
```

### 3. Create a configuration file

Place `mixins.example.json` in your resources folder:

```json
{
    "required": true,
    "package": "com.example.mixin",
    "compatibilityLevel": "JAVA_25",
    "mixins": [
        "GreeterMixin"
    ]
}
```

### 4. Bootstrap Mixin and apply transformations

In your application entry point:

```java
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.transformer.MixinTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

public class Main {
    public static void main(String[] args) throws Exception {
        // Step 1: Initialize the Mixin subsystem
        MixinBootstrap.init();
        MixinEnvironment.init(MixinEnvironment.Phase.DEFAULT);
        MixinEnvironment.gotoPhase(MixinEnvironment.Phase.DEFAULT);
        Mixins.addConfiguration("mixins.example.json");

        // Step 2: Create the transformer
        MixinTransformer transformer = new MixinTransformer();

        // Step 3: Load the target class and apply mixins
        ClassReader reader = new ClassReader("com.example.Greeter");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, 0);

        byte[] originalBytes = toByteArray(classNode);
        byte[] transformedBytes = transformer.transformClassBytes(
            "com.example.Greeter",
            "com.example.Greeter",
            originalBytes
        );

        // Step 4: Load the transformed class with a custom class loader
        CustomClassLoader loader = new CustomClassLoader();
        Class<?> transformedClass = loader.defineClass("com.example.Greeter", transformedBytes);
        Object instance = transformedClass.getDeclaredConstructor().newInstance();
        transformedClass.getMethod("sayHello", String.class).invoke(instance, "World");
    }

    private static byte[] toByteArray(ClassNode node) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }

    static class CustomClassLoader extends ClassLoader {
        public Class<?> defineClass(String name, byte[] b) {
            return super.defineClass(name, b, 0, b.length);
        }
    }
}
```

## Advanced Injections

This framework supports a wide range of injection types:

| Annotation | Description |
|------------|-------------|
| `@Inject` | Insert code at a specific point (`HEAD`, `RETURN`, `INVOKE`, etc.) |
| `@Redirect` | Redirect a method call to your own handler |
| `@ModifyArg` | Modify arguments passed to a method call |
| `@ModifyConstant` | Change a constant value used in the target method |
| `@ModifyVariable` | Alter local variables at a specific location |
| `@Overwrite` | Completely replace a method implementation |
| `@Shadow` | Access a private field or method from the target class |
| `@Unique` | Add a brand new method to the target class |
| `@Accessor` | Expose private fields or methods |
| `@Invoker` | Create a public invoker for a private method |

See the [test examples](src/test/java/test/mixin/TestMixin.java) for usage.

## Configuration Options

The JSON configuration supports these keys:

- `required` – fail if mixins cannot be applied (default: `false`)
- `package` – the package containing your mixin classes
- `compatibilityLevel` – target Java version (`JAVA_8` through `JAVA_25`)
- `mixins` – list of mixin class simple names
- `client`/`server` – side‑specific mixin lists (if side detection is available)
- `injectors` – custom injection point settings
- `overwrites` – overwrite behaviour settings

## Limitations

This standalone version **does not** include:
- Minecraft‑specific `ModLauncher` or `LaunchWrapper` support
- Automatic `ClassFileTransformer` registration via Java Agent (you must call `MixinTransformer` manually as shown above)
- Built‑in side (client/server) detection

## Building from Source

```shell
git clone https://github.com/UntoldStudio/Mixin.git
cd Mixin
./gradlew build
```

The output JARs are in `build/libs/`:
- `mixin-{version}-staging.jar` – runtime and annotation processor (fat jar)
- `mixin-{version}-processor.jar` – annotation processor only

## License

MIT – see [LICENSE.txt](LICENSE.txt).

Based on [SpongePowered/Mixin](https://github.com/SpongePowered/Mixin) and [FabricMC/Mixin](https://github.com/FabricMC/Mixin).