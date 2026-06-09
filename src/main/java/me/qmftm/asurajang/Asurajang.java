package me.qmftm.asurajang;

import me.qmftm.asurajang.augmentation.Augmentation;
import me.qmftm.asurajang.augmentation.AugmentationManager;
import me.qmftm.asurajang.augmentation.PrismChoice;
import me.qmftm.asurajang.augmentation.PrismItemManager;
import me.qmftm.asurajang.command.AsurajangCommand;
import me.qmftm.asurajang.config.YamlResource;
import me.qmftm.asurajang.game.BattlefieldManager;
import me.qmftm.asurajang.game.GameManager;
import me.qmftm.asurajang.game.GameScoreboardManager;
import me.qmftm.asurajang.game.MaxHealthManager;
import me.qmftm.asurajang.gui.AugmentationListGUI;
import me.qmftm.asurajang.gui.AugmentationSelectGUI;
import me.qmftm.asurajang.gui.PrismAugmentationSelectGUI;
import me.qmftm.asurajang.listener.AugmentationEffectListener;
import me.qmftm.asurajang.listener.AugmentationSelectListener;
import me.qmftm.asurajang.listener.GameModeSelectListener;
import me.qmftm.asurajang.listener.HotbarButtonListener;
import me.qmftm.asurajang.listener.PlayerDeathListener;
import me.qmftm.asurajang.listener.PrismAugItemListener;
import me.qmftm.asurajang.listener.RewardMessageListener;
import me.qmftm.asurajang.listener.ShopListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Asurajang extends JavaPlugin {

    public static NamespacedKey PRISM_AUG_KEY;

    private static Asurajang instance;
    private YamlResource augmentDescriptionConfig;
    private YamlResource augmentSettingConfig;
    private YamlResource prismDescriptionConfig;
    private YamlResource prismSettingConfig;
    private YamlResource prismItemConfig;
    private YamlResource nexusConfig;
    private AugmentationManager augmentationManager;
    private PrismItemManager prismItemManager;
    private BattlefieldManager battlefieldManager;
    private GameManager gameManager;
    private GameScoreboardManager scoreboardManager;
    private MaxHealthManager maxHealthManager;

    public static Asurajang getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        PRISM_AUG_KEY = new NamespacedKey(this, "prism_aug_id");

        saveDefaultConfig();
        augmentDescriptionConfig = new YamlResource(this, "augment/description.yml");
        augmentSettingConfig     = new YamlResource(this, "augment/config.yml");
        prismDescriptionConfig   = new YamlResource(this, "prism/description.yml");
        prismSettingConfig       = new YamlResource(this, "prism/config.yml");
        prismItemConfig          = new YamlResource(this, "prism/item.yml");
        nexusConfig              = new YamlResource(this, "nexus.yml");

        augmentationManager = new AugmentationManager(augmentDescriptionConfig.get(), prismDescriptionConfig.get());
        prismItemManager    = new PrismItemManager(prismItemConfig.get());
        battlefieldManager  = new BattlefieldManager();
        gameManager         = new GameManager();
        scoreboardManager   = new GameScoreboardManager();
        maxHealthManager    = new MaxHealthManager();

        getServer().getPluginManager().registerEvents(new AugmentationSelectListener(), this);
        getServer().getPluginManager().registerEvents(new AugmentationEffectListener(), this);
        getServer().getPluginManager().registerEvents(new GameModeSelectListener(), this);
        getServer().getPluginManager().registerEvents(new ShopListener(), this);
        getServer().getPluginManager().registerEvents(new HotbarButtonListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);
        getServer().getPluginManager().registerEvents(new PrismAugItemListener(), this);
        getServer().getPluginManager().registerEvents(new RewardMessageListener(), this);
        getServer().getPluginManager().registerEvents(battlefieldManager, this);

        AsurajangCommand cmd = new AsurajangCommand();
        Objects.requireNonNull(getCommand("mayhem")).setExecutor(cmd);
        Objects.requireNonNull(getCommand("mayhem")).setTabCompleter(cmd);

        getLogger().info("Asurajang v" + getPluginMeta().getVersion() + " 이 활성화되었습니다.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Asurajang 이 비활성화되었습니다.");
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

    public void openPrismAugmentationSelect(Player player) {
        List<PrismChoice> pool = new ArrayList<>();

        augmentationManager.getPrismAll().stream()
            .map(PrismChoice.Aug::new)
            .forEach(pool::add);

        pool.addAll(prismItemManager.getAll());

        new PrismAugmentationSelectGUI(pool).open(player);
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

    // augment/, prism/, nexus.yml을 다시 읽어들인다
    public void reloadExtraConfigs() {
        augmentDescriptionConfig.reload();
        augmentSettingConfig.reload();
        prismDescriptionConfig.reload();
        prismSettingConfig.reload();
        prismItemConfig.reload();
        nexusConfig.reload();
        augmentationManager.reload(augmentDescriptionConfig.get(), prismDescriptionConfig.get());
        prismItemManager.reload(prismItemConfig.get());
    }

    public FileConfiguration getAugmentDescriptionConfig() {
        return augmentDescriptionConfig.get();
    }

    public FileConfiguration getAugmentSettingConfig() {
        return augmentSettingConfig.get();
    }

    public FileConfiguration getNexusConfig() {
        return nexusConfig.get();
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

    public MaxHealthManager getMaxHealthManager() {
        return maxHealthManager;
    }
}
