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
import me.qmftm.asurajang.game.LevelUpManager;
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
import me.qmftm.asurajang.listener.PlayerMenuListener;
import me.qmftm.asurajang.listener.StatAnvilListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class Asurajang extends JavaPlugin {

    public static NamespacedKey PRISM_AUG_KEY;
    public static NamespacedKey CONSUMABLE_AUG_KEY;
    public static NamespacedKey STAT_ANVIL_KEY;
    public static NamespacedKey PLAYER_MENU_KEY;

    private static Asurajang instance;
    private YamlResource augmentDescriptionConfig;
    private YamlResource augmentSettingConfig;
    private YamlResource prismDescriptionConfig;
    private YamlResource prismSettingConfig;
    private YamlResource prismItemConfig;
    private YamlResource gamemodeConfig;
    private AugmentationManager augmentationManager;
    private PrismItemManager prismItemManager;
    private BattlefieldManager battlefieldManager;
    private GameManager gameManager;
    private GameScoreboardManager scoreboardManager;
    private MaxHealthManager maxHealthManager;
    private LevelUpManager levelUpManager;
    private StatAnvilListener statAnvilListener;

    public static Asurajang getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        PRISM_AUG_KEY = new NamespacedKey(this, "prism_aug_id");
        CONSUMABLE_AUG_KEY = new NamespacedKey(this, "consumable_aug_id");
        STAT_ANVIL_KEY = new NamespacedKey(this, "stat_anvil");
        PLAYER_MENU_KEY = new NamespacedKey(this, "player_menu");

        saveDefaultConfig();
        augmentDescriptionConfig = new YamlResource(this, "augment/description.yml");
        augmentSettingConfig     = new YamlResource(this, "augment/config.yml");
        prismDescriptionConfig   = new YamlResource(this, "prism/description.yml");
        prismSettingConfig       = new YamlResource(this, "prism/config.yml");
        prismItemConfig          = new YamlResource(this, "prism/item.yml");
        gamemodeConfig           = new YamlResource(this, "gamemode.yml");

        augmentationManager = new AugmentationManager(augmentDescriptionConfig.get(), prismDescriptionConfig.get());
        prismItemManager    = new PrismItemManager(prismItemConfig.get());
        battlefieldManager  = new BattlefieldManager();
        gameManager         = new GameManager();
        scoreboardManager   = new GameScoreboardManager();
        maxHealthManager    = new MaxHealthManager();
        levelUpManager      = new LevelUpManager();
        statAnvilListener   = new StatAnvilListener();

        getServer().getPluginManager().registerEvents(new AugmentationSelectListener(), this);
        getServer().getPluginManager().registerEvents(new AugmentationEffectListener(), this);
        getServer().getPluginManager().registerEvents(new GameModeSelectListener(), this);
        getServer().getPluginManager().registerEvents(new ShopListener(), this);
        getServer().getPluginManager().registerEvents(new HotbarButtonListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerMenuListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);
        getServer().getPluginManager().registerEvents(new PrismAugItemListener(), this);
        getServer().getPluginManager().registerEvents(new RewardMessageListener(), this);
        getServer().getPluginManager().registerEvents(statAnvilListener, this);
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
        Set<String> owned = augmentationManager.getActiveEffects(player.getUniqueId()).keySet();
        List<PrismChoice> pool = new ArrayList<>();

        augmentationManager.getPrismAll().stream()
            .filter(aug -> aug.isActive() || !owned.contains(aug.getId()))
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

    // augment/, prism/, gamemode.yml을 다시 읽어들인다
    public void reloadExtraConfigs() {
        augmentDescriptionConfig.reload();
        augmentSettingConfig.reload();
        prismDescriptionConfig.reload();
        prismSettingConfig.reload();
        prismItemConfig.reload();
        gamemodeConfig.reload();
        augmentationManager.reload(augmentDescriptionConfig.get(), prismDescriptionConfig.get());
        prismItemManager.reload(prismItemConfig.get());
    }

    public FileConfiguration getAugmentDescriptionConfig() {
        return augmentDescriptionConfig.get();
    }

    public FileConfiguration getAugmentSettingConfig() {
        return augmentSettingConfig.get();
    }

    public FileConfiguration getGamemodeConfig() {
        return gamemodeConfig.get();
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

    public LevelUpManager getLevelUpManager() {
        return levelUpManager;
    }

    public StatAnvilListener getStatAnvilListener() {
        return statAnvilListener;
    }

    public static ItemStack createStatAnvilItem() {
        ItemStack item = new ItemStack(Material.ANVIL);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("능력치 모루", NamedTextColor.DARK_PURPLE));
        meta.lore(List.of(Component.text("우클릭으로 능력치를 강화합니다.", NamedTextColor.GRAY)));
        meta.getPersistentDataContainer().set(STAT_ANVIL_KEY, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }
}
