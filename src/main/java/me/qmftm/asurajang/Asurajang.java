package me.qmftm.asurajang;

import me.qmftm.asurajang.augmentation.Augmentation;
import me.qmftm.asurajang.augmentation.AugmentationManager;
import me.qmftm.asurajang.command.AsurajangCommand;
import me.qmftm.asurajang.game.BattlefieldManager;
import me.qmftm.asurajang.game.GameManager;
import me.qmftm.asurajang.game.GameScoreboardManager;
import me.qmftm.asurajang.gui.AugmentationListGUI;
import me.qmftm.asurajang.gui.AugmentationSelectGUI;
import me.qmftm.asurajang.listener.AugmentationEffectListener;
import me.qmftm.asurajang.listener.AugmentationSelectListener;
import me.qmftm.asurajang.listener.GameModeSelectListener;
import me.qmftm.asurajang.listener.HotbarButtonListener;
import me.qmftm.asurajang.listener.PlayerDeathListener;
import me.qmftm.asurajang.listener.ShopListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class Asurajang extends JavaPlugin {

    private static Asurajang instance;
    private AugmentationManager augmentationManager;
    private BattlefieldManager battlefieldManager;
    private GameManager gameManager;
    private GameScoreboardManager scoreboardManager;

    public static Asurajang getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        augmentationManager  = new AugmentationManager(getConfig());
        battlefieldManager   = new BattlefieldManager();
        gameManager          = new GameManager();
        scoreboardManager    = new GameScoreboardManager();

        getServer().getPluginManager().registerEvents(new AugmentationSelectListener(), this);
        getServer().getPluginManager().registerEvents(new AugmentationEffectListener(), this);
        getServer().getPluginManager().registerEvents(new GameModeSelectListener(), this);
        getServer().getPluginManager().registerEvents(new ShopListener(), this);
        getServer().getPluginManager().registerEvents(new HotbarButtonListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);

        AsurajangCommand cmd = new AsurajangCommand();
        Objects.requireNonNull(getCommand("mayhem")).setExecutor(cmd);
        Objects.requireNonNull(getCommand("mayhem")).setTabCompleter(cmd);

        getLogger().info("Asurajang v" + getPluginMeta().getVersion() + " 활성화되었습니다.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Asurajang 비활성화되었습니다.");
        instance = null;
    }

    // 이미 보유한 증강은 제외하고 선택 GUI 오픈
    public void openAugmentationSelect(Player player) {
        Set<String> owned = augmentationManager.getActiveEffects(player.getUniqueId()).keySet();
        List<Augmentation> available = augmentationManager.getAll().stream()
            .filter(aug -> !owned.contains(aug.getId()))
            .toList();
        new AugmentationSelectGUI(available).open(player);
    }

    public void openAugmentationList(Player player) {
        new AugmentationListGUI(augmentationManager.getAll()).open(player);
    }

    // 플레이어가 보유한 증강 확인 GUI
    public void openPlayerAugmentations(Player player) {
        List<Augmentation> active = augmentationManager.getActiveEffects(player.getUniqueId())
            .keySet().stream()
            .map(augmentationManager::get)
            .filter(Objects::nonNull)
            .toList();

        if (active.isEmpty()) {
            player.sendMessage(Component.text("보유한 증강이 없습니다.", NamedTextColor.GRAY));
            return;
        }

        new AugmentationListGUI(active, Component.text("내 증강", NamedTextColor.LIGHT_PURPLE)).open(player);
    }

    public AugmentationManager getAugmentationManager() {
        return augmentationManager;
    }

    public BattlefieldManager getBattlefieldManager() {
        return battlefieldManager;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public GameScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }
}
