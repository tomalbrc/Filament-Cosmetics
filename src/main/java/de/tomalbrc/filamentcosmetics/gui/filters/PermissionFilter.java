package de.tomalbrc.filamentcosmetics.gui.filters;

import de.tomalbrc.filamentcosmetics.config.entries.CustomItemEntry;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.level.ServerPlayer;
import java.util.function.Predicate;

public class PermissionFilter implements Predicate<CustomItemEntry> {
    private final ServerPlayer player;

    public PermissionFilter(ServerPlayer player) {
        this.player = player;
    }

    @Override
    public boolean test(CustomItemEntry entry) {
        return Permissions.check(player, entry.permission());
    }
}