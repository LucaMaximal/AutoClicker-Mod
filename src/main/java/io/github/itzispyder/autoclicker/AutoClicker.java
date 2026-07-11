package io.github.itzispyder.autoclicker;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.itzispyder.autoclicker.ui.Config;
import io.github.itzispyder.autoclicker.ui.Events;
import io.github.itzispyder.improperui.ImproperUIAPI;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class AutoClicker implements ModInitializer, Global {

    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(modId, "binds")
    );

    public static final KeyMapping BIND = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "binds.autoclicker.menu",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_0,
            CATEGORY
    ));

    @Override
    public void onInitialize() {
        ImproperUIAPI.init(modId, AutoClicker.class, screens);

        Config.update();
        Events.distributeNoise(Config.leftCps, Config.leftChance, Events.noiseMapLeft);
        Events.distributeNoise(Config.rightCps, Config.rightChance, Events.noiseMapRight);

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            Config.update();
            Events.onTick();
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (BIND.consumeClick()) {
                ImproperUIAPI.parseAndRunFile(modId, "screen.ui");
            }
        });
    }
}
