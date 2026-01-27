package com.mlc.mlcgames.bank.utils;


import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.RegistryBuilderFactory;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.DialogRegistryEntry;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.set.RegistryValueSetBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Registry;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public class Bankgamedialog {
    public static Dialog dialog;
    public static void create() {
        final ClickCallback.Options unlimitedUses = ClickCallback.Options.builder().uses(-1).build();

        List<ActionButton> menuActions = List.of(
                ActionButton.builder(Component.text("1"))
                        .tooltip(Component.text("1"))
                        .action(DialogAction.staticAction(ClickEvent.runCommand("/bankgameprepare")))
                        .build()
        );

        dialog = Dialog.create(builder ->
                builder.empty().base(DialogBase.builder(Component.text("1"))
                                .body(List.of(DialogBody.plainMessage(Component.text("1")))).canCloseWithEscape(true).build())
                        .type(DialogType.multiAction(menuActions).columns(1).build()));
    }

}
