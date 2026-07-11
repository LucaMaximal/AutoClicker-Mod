package io.github.itzispyder.autoclicker.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Minecraft.class)
public interface AccessorMinecraftClient {

    @Invoker("startAttack")
    boolean leftClick();

    @Invoker("startUseItem")
    void rightClick();

    @Invoker("pickBlock")
    void middleClick();
}
