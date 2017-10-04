package com.github.games647.playerscan;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.stream.Stream;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.SkinConfiguration;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import static java.lang.String.valueOf;
import static net.md_5.bungee.api.ChatColor.*;
import static net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT;

class ScanCommand extends Command {

    private final BaseComponent[] emptyComponent = {};

    ScanCommand(String pluginName) {
        super(pluginName, pluginName + ".command");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length > 0) {
            String playerName = args[0];
            Optional<ProxiedPlayer> optPlayer = Optional.ofNullable(ProxyServer.getInstance().getPlayer(playerName));
            if (optPlayer.isPresent()) {
                printData(sender, optPlayer.get());
            } else {
                sender.sendMessage(builder("Player ", DARK_RED)
                        .append(playerName).color(YELLOW)
                        .append(" not online").color(DARK_RED)
                        .create());
            }
        } else {
            sender.sendMessage(builder("No player specified", DARK_RED).create());
        }
    }

    private void printData(CommandSender sender, ProxiedPlayer player) {
        //virtual player data
        sender.sendMessage(menu(sender, builder("Player: ", GOLD).append(player.getName()).color(GREEN),
                hoverMenu(LIGHT_PURPLE, DARK_PURPLE,
                        sub("", player.getUniqueId()::toString),
                        sub("Display name", player::getDisplayName)
                )));

        //connection data
        String serverName = player.getServer().getInfo().getName();

        sender.sendMessage(menu(sender, builder("Connection: ", GRAY).append(serverName).color(DARK_BLUE),
                hoverMenu(YELLOW, DARK_GREEN,
                        sub("Protocol", player.getPendingConnection()::getVersion),
                        sub("Online mode", player.getPendingConnection()::isOnlineMode),
                        sub("Address", player.getAddress()::getHostName),
                        sub("Ping", player::getPing)
                )));

        //permissions
        sender.sendMessage(mapPerm(sender, "Permissions", player.getPermissions()));
        sender.sendMessage(mapPerm(sender, "Groups", player.getGroups()));

        //forge
        sender.sendMessage(menu(sender,
                builder("Forge User: ", YELLOW).append(valueOf(player.isForgeUser())).color(BLUE),
                map(player.getModList().entrySet(), mod -> {
                    String modName = mod.getKey();
                    String modVersion = mod.getValue();

                    return builder("Name: ", YELLOW).append(modName).color(BLUE)
                            .append(" Version: ").color(YELLOW).append(modVersion).color(BLUE)
                            .create();
                })));

        //Client settings
        SkinConfiguration skinParts = player.getSkinParts();

        sender.sendMessage(menu(sender, builder("Client settings", GRAY),
                hoverMenu(GREEN, DARK_GREEN,
                        sub("View distance", player::getViewDistance),
                        sub("Locale", (Supplier<String>) player.getLocale()::getDisplayName),
                        sub("Chat mode", player.getChatMode()::name),
                        sub("Chat color support", player::hasChatColors),
                        sub("Displays cape", skinParts::hasCape),
                        sub("Displays hat", skinParts::hasHat),
                        sub("Displays jacket", skinParts::hasJacket),
                        sub("Displays left pants", skinParts::hasLeftPants),
                        sub("Displays right pants", skinParts::hasRightPants),
                        sub("Displays left sleeve", skinParts::hasLeftSleeve),
                        sub("Displays right sleeve", skinParts::hasRightSleeve)
                )));
    }

    private BaseComponent[] mapPerm(CommandSender sender, String category, Collection<String> permInfo) {
        return menu(sender, builder(category + ": ", DARK_AQUA)
                        .append(valueOf(permInfo.size())).color(DARK_GRAY),
                map(permInfo, info -> builder(info, DARK_GREEN).append("\n").create()));
    }

    private <T> BaseComponent[] map(Collection<T> lst, Function<T, BaseComponent[]> mapper) {
        if (lst.isEmpty()) {
            return emptyComponent;
        }

        ComponentBuilder builder = new ComponentBuilder("\n");
        lst.stream().map(mapper).forEach(builder::append);
        return builder.create();
    }

    private ComponentBuilder builder(String text, ChatColor color) {
        return new ComponentBuilder(text).color(color);
    }

    private BaseComponent[] menu(CommandSender sender, ComponentBuilder title, BaseComponent... hoverComp) {
        if (hoverComp.length > 0) {
            if (sender instanceof ProxiedPlayer) {
                title.event(new HoverEvent(SHOW_TEXT, hoverComp));
            } else {
                title.append("\n");
                Stream.of(hoverComp)
                        .filter(comp -> comp.getExtra() == null)
                        .forEach(comp -> title.append("    ").append(new BaseComponent[]{comp}));
            }
        }

        return title.create();
    }

    private BaseComponent[] hoverMenu(ChatColor primaryColor, ChatColor secondaryColor, SubMenu... lines) {
        ComponentBuilder builder = new ComponentBuilder("\n");

        Stream.of(lines)
                .map(menu -> builder(menu.getTitle(), primaryColor)
                        .append(menu.getValue()).color(secondaryColor)
                        .append("\n")
                        .create())
                .forEach(builder::append);

        return builder.create();
    }

    private SubMenu sub(String displayName, Supplier<String> supplier) {
        String title = displayName;
        if (!title.isEmpty()) {
            title += ": ";
        }

        return new SubMenu(title, supplier);
    }

    private SubMenu sub(String displayName, BooleanSupplier supplier) {
        return sub(displayName, () -> valueOf(supplier.getAsBoolean()));
    }

    private SubMenu sub(String displayName, IntSupplier supplier) {
        return sub(displayName, () -> valueOf(supplier.getAsInt()));
    }
}
