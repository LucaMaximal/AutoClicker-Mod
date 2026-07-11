package io.github.itzispyder.autoclicker;

import net.minecraft.client.Minecraft;

public interface Global {

    Minecraft mc = Minecraft.getInstance();
    String modId = "autoclicker";
    String[] screens = {
        "assets/autoclicker/improperui/screen.ui"
    };
}
