package com.nnpg.glazed.modules.main;

import com.nnpg.glazed.GlazedAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractStringWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.dialog.DialogScreen;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;
import java.util.Locale;

public class AutoLogin extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> password = sgGeneral.add(new StringSetting.Builder()
        .name("password")
        .description("Password used on the AuthMe login screen.")
        .defaultValue("")
        .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Ticks to wait after joining before sending /login.")
        .defaultValue(40)
        .min(0)
        .max(200)
        .sliderMax(200)
        .build()
    );

    private int loginTicks;
    private boolean loginSent;
    private Screen loginDialog;

    public AutoLogin() {
        super(GlazedAddon.CATEGORY, "auto-login", "Automatically fills and submits the AuthMe login screen.");
    }

    @Override
    public void onDeactivate() {
        loginTicks = 0;
        loginSent = false;
        loginDialog = null;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        Screen screen = mc.screen;
        if (screen != null) handleScreen(screen);

        if (mc.player == null || mc.getConnection() == null || password.get().isBlank()) {
            loginTicks = 0;
            loginSent = false;
            return;
        }
        if (loginSent || loginTicks++ < delay.get()) return;

        mc.getConnection().sendCommand("login " + password.get());
        loginSent = true;
    }

    public void handleScreen(Screen screen) {
        boolean authMeScreen = isAuthMeScreen(screen);
        if (!authMeScreen || screen == loginDialog || password.get().isBlank()) return;

        EditBox passwordBox = findPasswordBox(screen.children());
        AbstractButton loginButton = findLoginButton(screen.children());
        if (passwordBox == null) return;

        passwordBox.setValue(password.get());
        if (loginButton != null) {
            loginButton.onPress(null);
            loginDialog = screen;
            loginSent = true;
        }
    }

    private boolean isAuthMeScreen(Screen screen) {
        String title = screen.getTitle().getString().toLowerCase(Locale.ROOT);
        String text = collectText(screen.children()).toLowerCase(Locale.ROOT);

        boolean loginText = title.contains("login") || title.contains("đăng nhập")
            || title.contains("đăng nhập sử dụng") || title.contains("dang nhap")
            || text.contains("login") || text.contains("đăng nhập") || text.contains("dang nhap");
        boolean passwordText = text.contains("password") || text.contains("mật khẩu")
            || text.contains("mat khau");
        boolean authMeTitle = title.contains("đăng nhập sư phụ")
            || title.contains("đăng nhập sử dụng");
        boolean recognized = authMeTitle || (loginText && passwordText)
            || (findPasswordBox(screen.children()) != null && screen instanceof DialogScreen<?>);

        return recognized;
    }

    private EditBox findPasswordBox(List<? extends GuiEventListener> elements) {
        for (GuiEventListener element : elements) {
            if (element instanceof EditBox editBox) return editBox;
            if (element instanceof ContainerEventHandler parent) {
                EditBox nested = findPasswordBox(parent.children());
                if (nested != null) return nested;
            }
        }
        return null;
    }

    private AbstractButton findLoginButton(List<? extends GuiEventListener> elements) {
        for (GuiEventListener element : elements) {
            if (element instanceof AbstractButton button) {
                String label = button.getMessage().getString().toLowerCase(Locale.ROOT);
                if (label.contains("login") || label.contains("đăng nhập")
                    || label.contains("dang nhap") || label.contains("ok")
                    || label.contains("confirm") || label.contains("xác nhận")) return button;
            }
            if (element instanceof ContainerEventHandler parent) {
                AbstractButton nested = findLoginButton(parent.children());
                if (nested != null) return nested;
            }
        }
        return null;
    }

    private String collectText(List<? extends GuiEventListener> elements) {
        StringBuilder text = new StringBuilder();
        for (GuiEventListener element : elements) {
            if (element instanceof AbstractWidget widget) text.append(' ').append(widget.getMessage().getString());
            if (element instanceof AbstractStringWidget widget) text.append(' ').append(widget.getMessage().getString());
            if (element instanceof EditBox editBox) text.append(' ').append(editBox.getMessage().getString());
            if (element instanceof ContainerEventHandler parent) text.append(' ').append(collectText(parent.children()));
        }
        return text.toString();
    }
}