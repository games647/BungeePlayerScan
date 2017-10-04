package com.github.games647.playerscan;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
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
import net.md_5.bungee.api.connection.ProxiedPlayer.ChatMode;
import net.md_5.bungee.api.plugin.Command;

import static java.lang.String.valueOf;
import static net.md_5.bungee.api.ChatColor.*;
import static net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT;

class ScanCommand extends Command {

    ScanCommand(String pluginName) {
        super(pluginName, pluginName + ".command");

        System.out.println(getName());
        System.out.println(getPermission());
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
        sender.sendMessage(menu(builder("Player: ", GOLD).append(player.getName()).color(GREEN),
                hoverMenu(LIGHT_PURPLE, DARK_PURPLE,
                        sub("", () -> player.getUniqueId().toString()),
                        sub("Display name", player::getDisplayName)
                )));

        //connection data
        String serverName = player.getServer().getInfo().getName();
        boolean onlineMode = player.getPendingConnection().isOnlineMode();
        int protocolVersion = player.getPendingConnection().getVersion();
        int ping = player.getPing();
        InetSocketAddress address = player.getAddress();

        sender.sendMessage(menu(builder("Connection: ", GRAY).append(serverName).color(DARK_BLUE),
                hoverMenu(YELLOW, DARK_GREEN,
                        sub("Protocol", () -> valueOf(protocolVersion)),
                        sub("Online mode", () -> valueOf(onlineMode)),
                        sub("Address", address::getHostName),
                        sub("Ping", () -> valueOf(ping))
                )));

        //permissions
        sender.sendMessage(mapPerm("Permissions", player.getPermissions()));
        sender.sendMessage(mapPerm("Groups", player.getGroups()));

        //forge
        sender.sendMessage(menu(builder("Forge User: ", YELLOW).append(valueOf(player.isForgeUser())).color(BLUE),
                map(player.getModList().entrySet(), mod -> {
                    String modName = mod.getKey();
                    String modVersion = mod.getValue();

                    return builder("Name: ", YELLOW).append(modName).color(BLUE)
                            .append(" Version: ").color(YELLOW).append(modVersion).color(BLUE)
                            .create();
                })));

        //Client settings
        byte viewDistance = player.getViewDistance();
        ChatMode chatMode = player.getChatMode();
        Locale locale = player.getLocale();
        SkinConfiguration skinParts = player.getSkinParts();
        boolean chatColors = player.hasChatColors();

        sender.sendMessage(menu(builder("Client settings", GRAY),
                hoverMenu(GREEN, DARK_GREEN,
                        sub("View distance", () -> valueOf(viewDistance)),
                        sub("Locale", locale::getDisplayName),
                        sub("Chat mode", chatMode::name),
                        sub("Chat color support", () -> valueOf(chatColors)),
                        sub("Displays cape", () -> valueOf(skinParts.hasCape())),
                        sub("Displays hat", () -> valueOf(skinParts.hasHat())),
                        sub("Displays jacket", () -> valueOf(skinParts.hasJacket())),
                        sub("Displays left pants", () -> valueOf(skinParts.hasLeftPants())),
                        sub("Displays right pants", () -> valueOf(skinParts.hasRightPants())),
                        sub("Displays left sleeve", () -> valueOf(skinParts.hasLeftSleeve())),
                        sub("Displays right sleeve", () -> valueOf(skinParts.hasRightSleeve()))
                )));
    }

    private BaseComponent[] mapPerm(String category, Collection<String> permInfo) {
        return menu(builder(category + ": ", DARK_AQUA).append(valueOf(permInfo.size())).color(DARK_GRAY),
                map(permInfo, info -> builder(info, DARK_GREEN).append("\n").create()));
    }

    private <T> BaseComponent[] map(Collection<T> lst, Function<T, BaseComponent[]> mapper) {
        if (lst.isEmpty()) {
            return new BaseComponent[]{};
        }

        ComponentBuilder builder = new ComponentBuilder("\n");
        lst.stream().map(mapper).forEach(builder::append);
        return builder.create();
    }

    private ComponentBuilder builder(String text, ChatColor color) {
        return new ComponentBuilder(text).color(color);
    }

    private BaseComponent[] menu(ComponentBuilder title, BaseComponent... hoverComp) {
        ComponentBuilder builder = title;
        if (hoverComp.length > 0) {
            builder = title.event(new HoverEvent(SHOW_TEXT, hoverComp));
        }

        return builder.create();
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
}
