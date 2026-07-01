package com.jmane2026.totes.menu;

import com.jmane2026.totes.Totes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Totes.MODID);

    public static final Supplier<MenuType<ToteMenu>> TOTE_MENU = MENUS.register("tote_menu",
            () -> IMenuTypeExtension.create(ToteMenu::new));
}