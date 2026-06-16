package me.qmftm.asurajang.command;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.effect.BlackFlashEffect;
import me.qmftm.asurajang.game.GameManager;
import me.qmftm.asurajang.gui.DebugAugGiveGUI;
import me.qmftm.asurajang.gui.GameModeSelectGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AsurajangCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        Asurajang plugin = Asurajang.getInstance();

        // 권한 없이도 사용 가능한 커맨드
        if (args[0].equalsIgnoreCase("status")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("플레이어만 사용할 수 있습니다.", NamedTextColor.RED));
                return true;
            }
            plugin.openPlayerAugmentations(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("플레이어만 사용할 수 있습니다.", NamedTextColor.RED));
                return true;
            }
            if (args.length >= 2 && args[1].equalsIgnoreCase("prism")) {
                plugin.openPrismAugmentationList(player);
            } else {
                plugin.openAugmentationList(player);
            }
            return true;
        }

        if (!sender.hasPermission("asurajang.admin")) {
            sender.sendMessage(Component.text("권한이 없습니다.", NamedTextColor.RED));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(Component.text("플레이어만 사용할 수 있습니다.", NamedTextColor.RED));
                    return true;
                }
                if (plugin.getGameManager().getState() != GameManager.State.WAITING) {
                    sender.sendMessage(Component.text("이미 게임이 진행 중이거나 준비 중입니다.", NamedTextColor.RED));
                    return true;
                }
                new GameModeSelectGUI(plugin.getGameManager().getBaseMode(), plugin.getGameManager().isGuardianAttackEnabled()).open(player);
            }
            case "stop" -> {
                if (!plugin.getGameManager().stop()) {
                    sender.sendMessage(Component.text("진행 중인 게임이 없습니다.", NamedTextColor.RED));
                }
            }
            case "reload" -> {
                plugin.reloadConfig();
                plugin.reloadExtraConfigs();
                sender.sendMessage(Component.text("[Asurajang] 설정을 리로드했습니다.", NamedTextColor.GREEN));
            }
            case "debug" -> handleDebug(sender, args, plugin);
            default -> sendUsage(sender);
        }

        return true;
    }

    private void handleDebug(CommandSender sender, String[] args, Asurajang plugin) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("플레이어만 사용할 수 있습니다.", NamedTextColor.RED));
            return;
        }

        if (args.length < 2) {
            sendDebugUsage(sender);
            return;
        }

        switch (args[1].toLowerCase()) {
            case "aug", "aug_1" -> plugin.openAugmentationSelect(player);
            case "aug_2" -> new DebugAugGiveGUI(plugin.getAugmentationManager().getAll()).open(player);
            case "aug_3" -> new DebugAugGiveGUI(plugin.getAugmentationManager().getPrismAll()).open(player);
            case "proc" -> {
                BlackFlashEffect.debugProc = !BlackFlashEffect.debugProc;
                sender.sendMessage(Component.text(
                    "확률 100% 디버그 모드: " + (BlackFlashEffect.debugProc ? "ON" : "OFF"),
                    BlackFlashEffect.debugProc ? NamedTextColor.GREEN : NamedTextColor.RED
                ));
            }
            case "statvil" -> {
                plugin.getLevelUpManager().grantAnvilCharges(player.getUniqueId(), 1);
                sender.sendMessage(Component.text("[DEBUG] 능력치 모루 기회를 지급했습니다.", NamedTextColor.LIGHT_PURPLE));
            }
            case "add_ai" -> handleAddAi(sender, args, player, plugin);
            default -> sendDebugUsage(sender);
        }
    }

    private void handleAddAi(CommandSender sender, String[] args, Player player, Asurajang plugin) {
        if (args.length < 3) {
            sender.sendMessage(Component.text("사용법: /mayhem debug add_ai <red|blue>", NamedTextColor.YELLOW));
            return;
        }

        int team;
        switch (args[2].toLowerCase()) {
            case "red" -> team = 0;
            case "blue" -> team = 1;
            default -> {
                sender.sendMessage(Component.text("팀은 red 또는 blue 중 하나여야 합니다.", NamedTextColor.RED));
                return;
            }
        }

        plugin.getAiBotManager().spawnBot(player.getLocation(), team);
        sender.sendMessage(Component.text(
            "[DEBUG] " + (team == 0 ? "레드팀" : "블루팀") + " AI 봇을 소환했습니다.",
            NamedTextColor.LIGHT_PURPLE
        ));
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(Component.text("사용법: /mayhem <start|stop|reload|list [prism]|status|debug>", NamedTextColor.YELLOW));
    }

    private void sendDebugUsage(CommandSender sender) {
        sender.sendMessage(Component.text("사용법: /mayhem debug <aug_1|aug_2|aug_3|proc|statvil|add_ai>", NamedTextColor.YELLOW));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) return List.of("start", "stop", "reload", "list", "status", "debug");
        if (args.length == 2 && args[0].equalsIgnoreCase("list")) return List.of("prism");
        if (args.length == 2 && args[0].equalsIgnoreCase("debug")) return List.of("aug_1", "aug_2", "aug_3", "proc", "statvil", "add_ai");
        if (args.length == 3 && args[0].equalsIgnoreCase("debug") && args[1].equalsIgnoreCase("add_ai")) return List.of("red", "blue");
        return List.of();
    }
}
