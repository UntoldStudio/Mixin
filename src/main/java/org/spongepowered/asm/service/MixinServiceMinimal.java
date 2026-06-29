package org.spongepowered.asm.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.logging.Level;

public class MixinServiceMinimal extends MixinServiceAbstract {

    @Override
    protected ILogger createLogger(String name) {
        //return new org.spongepowered.asm.logging.LoggerAdapterConsole(name).setDebugStream(System.out);
        return new ILogger() {
            @Override public String getId() { return name; }
            @Override public String getType() { return "Silent"; }

            @Override public void catching(Level level, Throwable t) { }
            @Override public void catching(Throwable t) { }

            @Override public void debug(String message, Object... params) { }
            @Override public void debug(String message, Throwable t) { }

            @Override public void info(String message, Object... params) { }
            @Override public void info(String message, Throwable t) { }

            @Override public void warn(String message, Object... params) {
                System.err.println("[WARN] " + format(message, params));
            }
            @Override public void warn(String message, Throwable t) {
                System.err.println("[WARN] " + message);
                t.printStackTrace(System.err);
            }

            @Override public void error(String message, Object... params) {
                System.err.println("[ERROR] " + format(message, params));
            }
            @Override public void error(String message, Throwable t) {
                System.err.println("[ERROR] " + message);
                t.printStackTrace(System.err);
            }

            @Override public void fatal(String message, Object... params) { }
            @Override public void fatal(String message, Throwable t) { }

            @Override public void trace(String message, Object... params) { }
            @Override public void trace(String message, Throwable t) { }

            @Override public void log(Level level, String message, Object... params) {
                if (level == Level.WARN) this.warn(message, params);
                else if (level == Level.ERROR || level == Level.FATAL) this.error(message, params);
            }
            @Override public void log(Level level, String message, Throwable t) {
                if (level == Level.WARN) this.warn(message, t);
                else if (level == Level.ERROR || level == Level.FATAL) this.error(message, t);
            }

            @Override public <T extends Throwable> T throwing(T t) { return t; }

            private String format(String msg, Object... params) {
                if (params == null || params.length == 0) return msg;
                return msg.replace("{}", "%s").formatted(params);
            }
        };
    }

    @Override
    public String getName() {
        return "Minimal";
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public IClassProvider getClassProvider() {
        return new IClassProvider() {
            @Override
            public URL[] getClassPath() {
                return new URL[0];
            }

            @Override
            public Class<?> findClass(String name) throws ClassNotFoundException {
                return Class.forName(name);
            }

            @Override
            public Class<?> findClass(String name, boolean initialize) throws ClassNotFoundException {
                return Class.forName(name, initialize, MixinServiceMinimal.class.getClassLoader());
            }

            @Override
            public Class<?> findAgentClass(String name, boolean initialize) throws ClassNotFoundException {
                return Class.forName(name, initialize, MixinServiceMinimal.class.getClassLoader());
            }
        };
    }

    @Override
    public IClassBytecodeProvider getBytecodeProvider() {
        return new IClassBytecodeProvider() {
            @Override
            public ClassNode getClassNode(String name) throws IOException {
                return getClassNode(name, true);
            }

            @Override
            public ClassNode getClassNode(String name, boolean runTransformers) throws IOException {
                return getClassNode(name, runTransformers, 0);
            }

            @Override
            public ClassNode getClassNode(String name, boolean runTransformers, int readerFlags) throws IOException {
                ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
                InputStream is = null;
                if (contextLoader != null) {
                    is = contextLoader.getResourceAsStream(name.replace('.', '/') + ".class");
                }
                if (is == null) {
                    is = ClassLoader.getSystemClassLoader().getResourceAsStream(name.replace('.', '/') + ".class");
                }
                if (is == null) {
                    is = MixinServiceMinimal.class.getClassLoader().getResourceAsStream(name.replace('.', '/') + ".class");
                }
                if (is == null) {
                    throw new IOException("Class bytecode not found: " + name);
                }
                ClassReader reader = new ClassReader(is);
                ClassNode node = new ClassNode();
                reader.accept(node, readerFlags);
                return node;
            }
        };
    }

    @Override
    public ITransformerProvider getTransformerProvider() {
        return null;
    }

    @Override
    public IClassTracker getClassTracker() {
        return null;
    }

    @Override
    public IMixinAuditTrail getAuditTrail() {
        return null;
    }

    @Override
    public IFeatureValidator getFeatureValidator() {
        return null;
    }

    @Override
    public IAdviceProvider getAdviceProvider() {
        return null;
    }

    @Override
    public Collection<String> getPlatformAgents() {
        return Collections.emptyList();
    }

    @Override
    public IContainerHandle getPrimaryContainer() {
        return new MinimalContainerHandle();
    }

    @Override
    public Collection<IContainerHandle> getMixinContainers() {
        return Collections.singletonList(getPrimaryContainer());
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        return MixinServiceMinimal.class.getClassLoader().getResourceAsStream(name);
    }
}