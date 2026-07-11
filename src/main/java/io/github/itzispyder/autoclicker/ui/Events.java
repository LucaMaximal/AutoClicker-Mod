package io.github.itzispyder.autoclicker.ui;

import io.github.itzispyder.autoclicker.Global;
import io.github.itzispyder.autoclicker.mixin.AccessorMinecraftClient;
import io.github.itzispyder.improperui.util.MathUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;

public class Events implements Global {

    public static boolean leftToggle, rightToggle;
    public static Vec3 prevPos;
    public static float prevHp;
    public static int tickLeft, tickRight;
    public static int[] noiseMapLeft = new int[20];
    public static int[] noiseMapRight = new int[20];

    /*
    I hope this is how noise works
     */
    public static void distributeNoise(int cps, double clickChance, int[] noiseMap) {
        float chance = cps / 20.0F /* maxClicksPerTick */ / 20.0F /* averageAcrossEachTick */;
        int distributions = 0;

        for (int i = 0; i < 20; i++)
            noiseMap[i] = 0;

        while (distributions < cps) {
            int randomIndex = MathUtils.clamp((int)(Math.random() * 20),0, 19);
            if (distributions < 20 && noiseMap[randomIndex] > 0)
                continue;
            if (Math.random() > chance * clickChance) // simulates click
                continue;
            noiseMap[randomIndex]++;
            distributions++;
        }
    }

    public static void onTick() {
        if (!canClick())
            return;

        if (Config.left) {
            leftToggle = true;
            clickLeft();
        }
        else if (leftToggle) {
            leftToggle = false;
            mc.options.keyAttack.setDown(false);
        }

        if (Config.right) {
            rightToggle = true;
            clickRight();
        }
        else if (rightToggle) {
            rightToggle = false;
            mc.options.keyUse.setDown(false);
        }
    }

    private static void clickLeft() {
        if (!Config.leftSpam) {
            mc.options.keyAttack.setDown(true);
            return;
        }
        if (Config.leftOnlyHold && !mc.options.keyAttack.isDown())
            return;

        for (int i = 0; i < noiseMapLeft[tickLeft]; i++)
            if (Math.random() <= Config.leftChance)
                getInput().leftClick();

        if (++tickLeft >= 20) {
            tickLeft = 0;
            distributeNoise(Config.leftCps, Config.leftChance, noiseMapLeft);
            if (Config.debug)
                send("Left noise: " + Arrays.toString(noiseMapLeft));
        }
    }

    private static void clickRight() {
        if (!Config.rightSpam) {
            mc.options.keyUse.setDown(true);
            return;
        }
        if (Config.rightOnlyHold && !mc.options.keyUse.isDown())
            return;

        for (int i = 0; i < noiseMapRight[tickRight]; i++)
            if (Math.random() <= Config.rightChance)
                getInput().rightClick();

        if (++tickRight >= 20) {
            tickRight = 0;
            distributeNoise(Config.rightCps, Config.rightChance, noiseMapRight);
            if (Config.debug)
                send("Right noise: " + Arrays.toString(noiseMapRight));
        }
    }

    private static boolean canClick() {
        if (invalid())
            return false;

        var p = mc.player;
        boolean noTarget = true;
        boolean isBaby = false;
        float hp = prevHp;
        Vec3 pos = prevPos;
        prevHp = p.getHealth();
        prevPos = p.position();

        if (mc.hitResult instanceof EntityHitResult hit) {
            noTarget = false;
            isBaby = hit.getEntity() instanceof LivingEntity liv && liv.isBaby();
        }

        if (Config.onlyWhenTarget && noTarget)
            return false;
        if (Config.noBabies && isBaby)
            return false;
        if (Config.maxAttackCooldown > 0 && p.getAttackStrengthScale(1.0F) < Config.maxAttackCooldown)
            return false;
        if (Config.stopWhenDamage && p.getHealth() < hp) {
            if (Config.left || Config.right) {
                Config.write("left", false);
                Config.write("right", false);
                send("Damage taken, clickers disabled");
            }
            return false;
        }
        if (Config.stopWhenMove && p.position().distanceTo(pos) > 0.1) {
            if (Config.left || Config.right) {
                Config.write("left", false);
                Config.write("right", false);
                send("Player moved, clickers disabled");
            }
            return false;
        }
        return !Config.stopWhenTarget || noTarget;
    }

    public static void send(String msg) {
        if (valid())
            mc.player.sendSystemMessage(Component.literal(("&f[&6Autoclicker&f]&r " + msg).replace('&', '§')));
    }

    public static boolean invalid() {
        return mc.player == null || mc.level == null || mc.options == null;
    }

    public static boolean valid() {
        return !invalid();
    }

    public static AccessorMinecraftClient getInput() {
        return (AccessorMinecraftClient)mc;
    }
}
