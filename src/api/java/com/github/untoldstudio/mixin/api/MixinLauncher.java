package com.github.untoldstudio.mixin.api;

import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.launch.platform.CommandLineOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.mixin.transformer.MixinTransformer;

import java.io.File;

public class MixinLauncher {
    public static void initializeMixin(String mixinJsonPath) {
        MixinBootstrap.init();
        MixinEnvironment.init(MixinEnvironment.Phase.DEFAULT);
        Mixins.addConfiguration(mixinJsonPath);
        MixinEnvironment.gotoPhase(MixinEnvironment.Phase.DEFAULT);
        MixinBootstrap.getPlatform().prepare(CommandLineOptions.defaultArgs());
        try {
            File agentJar = new File(MixinLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            String pid = java.lang.management.ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
            com.sun.tools.attach.VirtualMachine vm = com.sun.tools.attach.VirtualMachine.attach(pid);
            vm.loadAgent(agentJar.getAbsolutePath(), mixinJsonPath);
            vm.detach();
        } catch (Exception e) {
            throw new RuntimeException("Failed to attach Mixin agent", e);
        }
    }
    public static IMixinTransformer createTransformer(){
        return new MixinTransformer();
    }
    public static void addConfiguration(String path){
        Mixins.addConfiguration(path);
    }
    public static MixinEnvironment getEnvironment(){
        return MixinEnvironment.getDefaultEnvironment();
    }
}
