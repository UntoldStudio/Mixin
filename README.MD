# Mixin for Java 25

A lightweight, standalone bytecode weaving framework based on [ASM](https://asm.ow2.io/) and the SpongePowered Mixin project, tailored for modern Java applications.

## Installation

Add the JitPack repository and dependency to your `build.gradle`:

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    implementation 'com.github.UntoldStudio:Mixin:0.2.1'
    annotationProcessor 'com.github.UntoldStudio:Mixin:0.2.1'
}
```

Replace `0.2.1` with the latest release tag.

## Core Concepts

- **Mixin Class**: A class annotated with `@Mixin` that defines how to modify a target class.
- **Target Class**: The original class you want to enhance or modify.
- **Injections**: Methods annotated with `@Inject`, `@Redirect`, `@ModifyArg`, etc., which are woven into specific points in the target class.
- **Configuration File**: A JSON file (`mixins.<name>.json` or `*.mixin.json`) that tells the framework which mixins to apply.

## Quick Start

Choose the startup method that suits your project:

### Method 1: Java Agent (zero‑code intrusion)

Start your application with the `-javaagent` JVM argument:

```shell
java -javaagent:/path/to/mixin.jar -jar your-app.jar
```

The agent automatically scans the classpath for configuration files named `mixins.*.json` or `*.mixin.json` and applies the declared mixins. Your application code remains untouched.

### Method 2: Dynamic attachment (non‑agent, no command‑line changes)

Call `MixinLauncher.initializeMixin()` at the very beginning of your `main` method:

```java
import com.github.untoldstudio.mixin.api.MixinLauncher;

public class Main {
    public static void main(String[] args) {
        MixinLauncher.initializeMixin("mixins.example.json");
        // Now all mixin targets are automatically woven when loaded
    }
}
```

> **Note:** This method requires `-Djdk.attach.allowAttachSelf=true` and `--add-modules jdk.attach` to be added to your JVM arguments if you are using JDK 25+.

### Method 3: Manual initialization (low‑level control)

Use the classic bootstrap sequence and a custom class loader when you need full control over the transformation process. Refer to the [manual example](#manual-initialization-example) below.

## Creating a Mixin

1. **Target class**:

```java
package com.example;

public class Greeter {
    public void sayHello(String name) {
        System.out.println("Hello, " + name);
    }
}
```

2. **Mixin class**:

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

3. **Configuration file** (`mixins.example.json` in classpath root):

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

## Advanced Injections

| Annotation | Description |
|------------|-------------|
| `@Inject` | Insert code at a specific point (`HEAD`, `RETURN`, `INVOKE`, `TAIL`, etc.) |
| `@Overwrite` | Completely replace a method implementation |
| `@Redirect` | Redirect a method call to your own handler |
| `@ModifyArg` | Modify arguments passed to a method call |
| `@ModifyConstant` | Change a constant value used in the target method |
| `@Shadow` | Access a private field from the target class |
| `@Unique` | Add a brand new method to the target class |

## Configuration Options

- `required` – fail if mixins cannot be applied
- `package` – the package containing your mixin classes
- `compatibilityLevel` – target Java version (`JAVA_8` through `JAVA_25`)
- `mixins` – list of mixin class simple names
- `minVersion` – minimum required Mixin version (optional)

## Manual Initialization Example

If you choose manual control, here is a minimal setup:

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
        MixinBootstrap.init();
        MixinEnvironment.init(MixinEnvironment.Phase.DEFAULT);
        Mixins.addConfiguration("mixins.example.json");
        MixinEnvironment.gotoPhase(MixinEnvironment.Phase.DEFAULT);
        MixinBootstrap.getPlatform().prepare(CommandLineOptions.defaultArgs());

        MixinTransformer transformer = new MixinTransformer();
        byte[] original = loadClassBytes("com.example.Greeter");
        byte[] transformed = transformer.transformClassBytes(
            "com.example.Greeter", "com.example.Greeter", original
        );

        CustomClassLoader loader = new CustomClassLoader();
        Class<?> clazz = loader.defineClass("com.example.Greeter", transformed);
        Object instance = clazz.getDeclaredConstructor().newInstance();
        clazz.getMethod("sayHello", String.class).invoke(instance, "World");
    }

    // ... helper methods toByteArray, CustomClassLoader
}
```

## Limitations

- Minecraft‑specific launchers (`LaunchWrapper`, `ModLauncher`) are not supported.
- Side detection (client/server) is not available.

## Building from Source

```shell
git clone https://github.com/UntoldStudio/Mixin.git
cd Mixin
./gradlew build
```

Output JARs in `build/libs/`:

- `mixin.jar` – runtime, annotation processor, and API
- `mixin-processor.jar` – annotation processor only

## License

MIT – see [LICENSE.txt](LICENSE.txt).

Based on [SpongePowered/Mixin](https://github.com/SpongePowered/Mixin) and [FabricMC/Mixin](https://github.com/FabricMC/Mixin).