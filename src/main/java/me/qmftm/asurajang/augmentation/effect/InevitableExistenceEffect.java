package me.qmftm.asurajang.augmentation.effect;

import me.qmftm.asurajang.Asurajang;
import me.qmftm.asurajang.augmentation.AugmentSettings;
import me.qmftm.asurajang.game.GameManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

// 적 처치 시 일정 확률로 6종의 스톤 중 하나를 지급하고,
// 모두 모으면 건틀릿(벌집 조각)을 우클릭해 1회 한정으로 모든 적을 즉사시킨다.
// 각 스톤은 왼손(보조 손)에 들고 있는 동안에만 효과가 발동한다.
public class InevitableExistenceEffect implements AugmentationEffect {

    private static final NamespacedKey STONE_KEY    = new NamespacedKey(Asurajang.getInstance(), "inevitable_stone");
    private static final NamespacedKey GAUNTLET_KEY = new NamespacedKey(Asurajang.getInstance(), "inevitable_gauntlet");
    private static final NamespacedKey SPACE_SPEED_KEY       = new NamespacedKey(Asurajang.getInstance(), "inevitable_space_speed");
    private static final NamespacedKey TIME_ATTACK_SPEED_KEY = new NamespacedKey(Asurajang.getInstance(), "inevitable_time_attack_speed");

    private enum Stone {
        POWER  ("파워 스톤",    Material.AMETHYST_SHARD, NamedTextColor.LIGHT_PURPLE, "공격력 +100%"),
        SPACE  ("스페이스 스톤", Material.LAPIS_LAZULI,   NamedTextColor.BLUE,         "이동 속도 +100%"),
        REALITY("리얼리티 스톤", Material.REDSTONE,       NamedTextColor.RED,          "투명화"),
        SOUL   ("소울 스톤",    Material.RESIN_CLUMP,    NamedTextColor.GOLD,         "공격의 50%를 고정 피해로 적용"),
        TIME   ("타임 스톤",    Material.EMERALD,        NamedTextColor.GREEN,        "공격 속도 +50%"),
        MIND   ("마인드 스톤",  Material.RAW_GOLD,        NamedTextColor.YELLOW,       "공격한 적에게 5초간 매혹 부여");

        final String displayName;
        final Material material;
        final NamedTextColor color;
        final String description;

        Stone(String displayName, Material material, NamedTextColor color, String description) {
            this.displayName = displayName;
            this.material = material;
            this.color = color;
            this.description = description;
        }
    }

    private BukkitTask offHandTask;
    private Stone activeStone;
    private boolean used = false;

    @Override
    public void onActivate(Player player) {
        used = false;
        activeStone = null;
        giveGauntlet(player);

        player.sendMessage(Component.text("[필연적인 존재] ", NamedTextColor.GOLD)
            .append(Component.text("건틀릿을 획득했습니다. 적을 처치해 스톤을 모으세요!", NamedTextColor.YELLOW)));

        long interval = AugmentSettings.getLong("InevitableExistence", "offhand-check-interval-ticks", 5L);
        offHandTask = Bukkit.getScheduler().runTaskTimer(Asurajang.getInstance(), () -> tickOffHand(player), 0L, interval);
    }

    @Override
    public void onDeactivate(Player player) {
        if (offHandTask != null) { offHandTask.cancel(); offHandTask = null; }
        clearStoneEffect(player, activeStone);
        activeStone = null;
    }

    // ── 왼손 스톤 효과 ─────────────────────────────────────────────────────

    private void tickOffHand(Player player) {
        if (!player.isOnline()) return;

        Stone held = getStone(player.getInventory().getItemInOffHand());
        if (held != activeStone) {
            clearStoneEffect(player, activeStone);
            activeStone = held;
            if (held == Stone.SPACE) addSpeedModifier(player);
            else if (held == Stone.TIME) addAttackSpeedModifier(player);
        }

        if (held == Stone.REALITY) {
            long interval = AugmentSettings.getLong("InevitableExistence", "offhand-check-interval-ticks", 5L);
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, (int) interval + 5, 0, true, false));
        }
    }

    private void clearStoneEffect(Player player, Stone stone) {
        if (stone == Stone.SPACE) removeSpeedModifier(player);
        else if (stone == Stone.TIME) removeAttackSpeedModifier(player);
        else if (stone == Stone.REALITY) player.removePotionEffect(PotionEffectType.INVISIBILITY);
    }

    @Override
    public void onDamageAsAttacker(Player player, EntityDamageByEntityEvent event) {
        Stone held = getStone(player.getInventory().getItemInOffHand());
        if (held == null) return;

        switch (held) {
            case POWER -> {
                double multiplier = AugmentSettings.getDouble("InevitableExistence", "power-damage-multiplier", 2.0);
                event.setDamage(event.getDamage() * multiplier);
            }
            case SOUL -> applySoulStone(event);
            case MIND -> applyMindStone(player, event);
            default -> {}
        }
    }

    private void applySoulStone(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        double ratio = AugmentSettings.getDouble("InevitableExistence", "soul-fixed-damage-ratio", 0.5);
        double fixed = event.getDamage() * ratio;
        event.setDamage(event.getDamage() - fixed);

        // 방어력 계산이 끝난 다음 틱에 체력을 직접 깎아 방어력을 무시하는 고정 피해로 적용
        Bukkit.getScheduler().runTask(Asurajang.getInstance(), () -> {
            if (event.isCancelled() || victim.isDead() || !victim.isValid()) return;
            victim.setHealth(Math.max(0.0, victim.getHealth() - fixed));
        });
    }

    private void applyMindStone(Player player, EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        long duration = AugmentSettings.getLong("InevitableExistence", "mind-charm-duration-ticks", 100L);
        double slow = AugmentSettings.getDouble("InevitableExistence", "mind-charm-slow-amount", 0.5);
        CharmEffect.applyCharm(victim, player, duration, slow);
    }

    private static void addSpeedModifier(Player player) {
        AttributeInstance attr = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (attr == null) return;
        removeModifier(attr, SPACE_SPEED_KEY);
        double bonus = AugmentSettings.getDouble("InevitableExistence", "space-speed-bonus", 1.0);
        attr.addModifier(new AttributeModifier(SPACE_SPEED_KEY, bonus, AttributeModifier.Operation.ADD_SCALAR));
    }

    private static void removeSpeedModifier(Player player) {
        AttributeInstance attr = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (attr != null) removeModifier(attr, SPACE_SPEED_KEY);
    }

    private static void addAttackSpeedModifier(Player player) {
        AttributeInstance attr = player.getAttribute(Attribute.ATTACK_SPEED);
        if (attr == null) return;
        removeModifier(attr, TIME_ATTACK_SPEED_KEY);
        double bonus = AugmentSettings.getDouble("InevitableExistence", "time-attack-speed-bonus", 0.5);
        attr.addModifier(new AttributeModifier(TIME_ATTACK_SPEED_KEY, bonus, AttributeModifier.Operation.ADD_SCALAR));
    }

    private static void removeAttackSpeedModifier(Player player) {
        AttributeInstance attr = player.getAttribute(Attribute.ATTACK_SPEED);
        if (attr != null) removeModifier(attr, TIME_ATTACK_SPEED_KEY);
    }

    private static void removeModifier(AttributeInstance attr, NamespacedKey key) {
        attr.getModifiers().stream()
            .filter(m -> m.getKey().equals(key))
            .findFirst()
            .ifPresent(attr::removeModifier);
    }

    // ── 스톤 획득 ──────────────────────────────────────────────────────────

    @Override
    public void onKillEnemy(Player player, Player victim) {
        double chance = AugmentSettings.getDouble("InevitableExistence", "stone-drop-chance", 0.5);
        if (ThreadLocalRandom.current().nextDouble() >= chance) return;

        List<Stone> missing = new ArrayList<>();
        for (Stone stone : Stone.values()) {
            if (!hasStone(player, stone)) missing.add(stone);
        }
        if (missing.isEmpty()) return;

        Stone granted = missing.get(ThreadLocalRandom.current().nextInt(missing.size()));
        ItemStack item = createStoneItem(granted);
        if (!player.getInventory().addItem(item).isEmpty()) {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }

        player.sendMessage(Component.text("[필연적인 존재] ", NamedTextColor.GOLD)
            .append(Component.text(granted.displayName + "을(를) 획득했습니다!", NamedTextColor.YELLOW)));
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.2f);

        if (missing.size() == 1) {
            player.sendMessage(Component.text("[필연적인 존재] ", NamedTextColor.GOLD)
                .append(Component.text("6종의 스톤을 모두 모았습니다! 건틀릿을 우클릭하세요.", NamedTextColor.LIGHT_PURPLE)));
        }
    }

    // ── 건틀릿 발동 ────────────────────────────────────────────────────────

    @Override
    public void onRightClick(Player player, PlayerInteractEvent event) {
        ItemStack main = player.getInventory().getItemInMainHand();
        if (main.getType().isAir()) return;

        ItemMeta meta = main.getItemMeta();
        if (meta == null) return;
        if (!"InevitableExistence".equals(meta.getPersistentDataContainer().get(GAUNTLET_KEY, PersistentDataType.STRING))) return;

        event.setCancelled(true);

        if (used) {
            player.sendMessage(Component.text("[필연적인 존재] ", NamedTextColor.GOLD)
                .append(Component.text("이미 사용한 건틀릿입니다.", NamedTextColor.RED)));
            return;
        }

        if (!hasAllStones(player)) {
            player.sendMessage(Component.text("[필연적인 존재] ", NamedTextColor.GOLD)
                .append(Component.text("아직 모든 스톤을 모으지 못했습니다.", NamedTextColor.RED)));
            return;
        }

        used = true;
        removeAllStones(player);
        snap(player);
    }

    private void snap(Player player) {
        GameManager gm = Asurajang.getInstance().getGameManager();
        int myTeam = gm.getTeam(player.getUniqueId());
        boolean team = gm.getGameMode() == GameManager.GameMode.TEAM;

        List<Player> targets = new ArrayList<>();
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(player)) continue;
            if (other.getGameMode() == GameMode.SPECTATOR) continue;
            if (team && gm.getTeam(other.getUniqueId()) == myTeam) continue;
            targets.add(other);
        }

        List<Mannequin> botTargets = new ArrayList<>();
        for (Map.Entry<UUID, Integer> entry : Asurajang.getInstance().getAiBotManager().getBotTeams().entrySet()) {
            if (team && entry.getValue() == myTeam) continue;
            Entity entity = Bukkit.getServer().getEntity(entry.getKey());
            if (entity instanceof Mannequin mannequin && mannequin.isValid()) botTargets.add(mannequin);
        }

        player.getWorld().spawnParticle(Particle.SOUL, player.getLocation().add(0, 1, 0), 100, 1, 1, 1, 0.1);
        player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 0.6f);
        player.sendMessage(Component.text("[필연적인 존재] ", NamedTextColor.GOLD)
            .append(Component.text("모든 적이 소멸합니다...", NamedTextColor.LIGHT_PURPLE)));

        for (Player target : targets) {
            target.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, target.getLocation().add(0, 1, 0), 60, 0.4, 0.6, 0.4, 0.05);
            target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_DEATH, 1.0f, 0.7f);
            target.damage(10_000.0, player);
        }

        for (Mannequin target : botTargets) {
            target.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, target.getLocation().add(0, 1, 0), 60, 0.4, 0.6, 0.4, 0.05);
            target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_DEATH, 1.0f, 0.7f);
            target.damage(10_000.0, player);
        }
    }

    // ── 아이템 헬퍼 ────────────────────────────────────────────────────────

    private static void giveGauntlet(Player player) {
        ItemStack item = new ItemStack(Material.HONEYCOMB);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("필연적인 존재", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
            Component.text("6종의 스톤을 모두 모으면", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
            Component.text("우클릭하여 모든 적을 즉사시킵니다.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
            Component.text("(1회 한정)", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false)
        ));
        meta.getPersistentDataContainer().set(GAUNTLET_KEY, PersistentDataType.STRING, "InevitableExistence");
        item.setItemMeta(meta);
        player.getInventory().addItem(item);
    }

    private static ItemStack createStoneItem(Stone stone) {
        ItemStack item = new ItemStack(stone.material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(stone.displayName, stone.color).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
            Component.text(stone.description, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
            Component.text("왼손에 들고 있어야 효과가 발동합니다.", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false)
        ));
        meta.getPersistentDataContainer().set(STONE_KEY, PersistentDataType.STRING, stone.name());
        item.setItemMeta(meta);
        return item;
    }

    private static Stone getStone(ItemStack item) {
        if (item == null || item.getType().isAir()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        String id = meta.getPersistentDataContainer().get(STONE_KEY, PersistentDataType.STRING);
        if (id == null) return null;
        try {
            return Stone.valueOf(id);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static boolean hasStone(Player player, Stone stone) {
        PlayerInventory inv = player.getInventory();
        for (ItemStack item : inv.getContents()) {
            if (getStone(item) == stone) return true;
        }
        return getStone(inv.getItemInOffHand()) == stone;
    }

    private static boolean hasAllStones(Player player) {
        for (Stone stone : Stone.values()) {
            if (!hasStone(player, stone)) return false;
        }
        return true;
    }

    private static void removeAllStones(Player player) {
        PlayerInventory inv = player.getInventory();
        ItemStack[] contents = inv.getContents();
        for (int i = 0; i < contents.length; i++) {
            if (getStone(contents[i]) != null) inv.clear(i);
        }
        if (getStone(inv.getItemInOffHand()) != null) inv.setItemInOffHand(null);
    }
}
