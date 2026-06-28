package org.spongepowered.asm.service;

import java.util.Collection;
import java.util.Collections;

import org.spongepowered.asm.launch.platform.container.IContainerHandle;

public class MinimalContainerHandle implements IContainerHandle {

    @Override
    public String getId() {
        return "minimal-container";
    }

    @Override
    public String getDescription() {
        return "Minimal Mixin Container";
    }

    @Override
    public String getAttribute(String name) {
        /* Let system properties handle configs */
        return null;
    }

    @Override
    public Collection<IContainerHandle> getNestedContainers() {
        return Collections.emptyList();
    }
}