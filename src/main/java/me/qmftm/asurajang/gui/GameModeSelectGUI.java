package me.qmftm.asurajang.gui;

import me.qmftm.asurajang.game.GameManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GameModeSelectGUI implements InventoryHolder {

    public static final int TEAM_SLOT = 11;
    public static final int SOLO_SLOT = 15;
    public static final int BASE_MODE_SLOT = 22;
    public static final int GUARDIAN_ATTACK_SLOT = 23;

    private static final ItemStack BACKGROUND = buildBackground();

    private final Inventory inventory;

    public GameModeSelectGUI(GameManager.BaseMode baseMode, boolean guardianAttackEnabled) {
        this.inventory = Bukkit.createInventory(this, 36, Component.text("게임 모드 선택"));
        fillBackground();
        populate(baseMode, guardianAttackEnabled);
    }

    public static ItemStack backgroundItem() {
        return BACKGROUND;
    }

    private static ItemStack buildBackground() {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        meta.displayName(Component.empty());
        pane.setItemMeta(meta);
        return pane;
    }

    private void fillBackground() {
        for (int i = 0; i < 36; i++) {
            inventory.setItem(i, BACKGROUND);
        }
    }

    private void populate(GameManager.BaseMode baseMode, boolean guardianAttackEnabled) {
        inventory.setItem(TEAM_SLOT, buildChoice(
            Material.MUSIC_DISC_PRECIPICE,
            Component.text("팀전", NamedTextColor.GREEN),
            List.of(
                Component.text("레드팀과 블루팀으로 나뉘어 싸웁니다.", NamedTextColor.GRAY),
                Component.text("같은 팀끼리는 서로 공격할 수 없습니다.", NamedTextColor.GRAY),
                Component.text("기지 모드를 함께 켤 수 있습니다.", NamedTextColor.DARK_GRAY)
            )
        ));
        inventory.setItem(SOLO_SLOT, buildChoice(
            Material.BLADE_POTTERY_SHERD,
            Component.text("개인전", NamedTextColor.GOLD),
            List.of(
                Component.text("모두가 적이 되어 싸웁니다.", NamedTextColor.GRAY),
                Component.text("전장 곳곳에 무작위로 흩어져 시작합니다.", NamedTextColor.GRAY)
            )
        ));
        inventory.setItem(BASE_MODE_SLOT, buildBaseModeItem(baseMode));
        // 거점 공격 버튼은 기지 모드일 때만 노출
        if (baseMode == GameManager.BaseMode.BASE) {
            inventory.setItem(GUARDIAN_ATTACK_SLOT, buildGuardianAttackItem(guardianAttackEnabled));
        }
    }

    public static ItemStack buildBaseModeItem(GameManager.BaseMode mode) {
        Material icon = switch (mode) {
            case BASE -> Material.BEACON;
            case WILD -> Material.SHORT_GRASS;
            case OFF  -> Material.GRASS_BLOCK;
        };
        Component stateLabel = switch (mode) {
            case BASE -> Component.text("기지 모드",    NamedTextColor.GREEN);
            case WILD -> Component.text("야생 모드",    NamedTextColor.GREEN);
            case OFF  -> Component.text("기본 게임모드", NamedTextColor.GREEN);
        };
        return buildChoice(
            icon,
            Component.text("기지 모드", NamedTextColor.AQUA),
            List.of(
                Component.text("기지 모드: 팀 진영에 거점과 가디언이 세워집니다.", NamedTextColor.GRAY),
                Component.text("야생 모드: 거점 없이 팀전이 진행됩니다.", NamedTextColor.GRAY),
                Component.text("팀전을 선택했을 때만 적용됩니다.", NamedTextColor.DARK_GRAY),
                Component.empty(),
                Component.text("현재: ", NamedTextColor.GRAY).append(stateLabel)
            )
        );
    }

    public static ItemStack buildGuardianAttackItem(boolean enabled) {
        return buildChoice(
            enabled ? Material.SPECTRAL_ARROW : Material.ARROW,
            Component.text("거점 공격", NamedTextColor.LIGHT_PURPLE),
            List.of(
                Component.text("거점 가디언이 범위 안에 들어온 상대팀을", NamedTextColor.GRAY),
                Component.text("투사체로 직접 공격할지 정합니다.", NamedTextColor.GRAY),
                Component.text("기지 모드를 켰을 때만 적용됩니다.", NamedTextColor.DARK_GRAY),
                Component.empty(),
                Component.text("현재: ", NamedTextColor.GRAY)
                    .append(enabled
                        ? Component.text("켜짐", NamedTextColor.GREEN)
                        : Component.text("꺼짐", NamedTextColor.RED))
            )
        );
    }

    private static ItemStack buildChoice(Material material, Component name, List<Component> loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name.decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
        meta.lore(loreLines.stream()
            .map(line -> line.decoration(TextDecoration.ITALIC, false))
            .toList());
        item.setItemMeta(meta);
        return item;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
