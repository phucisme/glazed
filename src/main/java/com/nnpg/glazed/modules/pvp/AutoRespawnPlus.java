/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.nnpg.glazed.modules.pvp;
import com.nnpg.glazed.GlazedAddon;

import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.WaypointsModule;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.gui.screens.DeathScreen;

public class AutoRespawnPlus extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> autoBack = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-back")
        .description("Automatically sends /back after respawning.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> backDelay = sgGeneral.add(new IntSetting.Builder()
        .name("back-delay")
        .description("Ticks to wait after respawning before sending /back.")
        .defaultValue(20)
        .min(0)
        .max(200)
        .sliderMax(200)
        .build()
    );

    private int backTicks;

    public AutoRespawnPlus() {
        super(GlazedAddon.pvp, "auto-respawn-plus", "Automatically respawns after death.");
    }

    @Override
    public void onDeactivate() {
        backTicks = 0;
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onOpenScreenEvent(OpenScreenEvent event) {
        if (!(event.screen instanceof DeathScreen)) return;

        Modules.get().get(WaypointsModule.class).addDeath(mc.player.position());
        mc.player.respawn();
        backTicks = autoBack.get() ? backDelay.get() : 0;
        event.cancel();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (backTicks <= 0 || mc.player == null || mc.getConnection() == null) return;
        if (backTicks-- > 1) return;

        mc.getConnection().sendCommand("back");
    }
}
