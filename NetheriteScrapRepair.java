package kr.netheritescrap;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.view.AnvilView;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 모루에서 네더라이트 파편으로 네더라이트 장비를 수리한다.
 * 파편 1개당 수리 비율은 config.yml 의 repair-percent (기본 45 = 바닐라 25% + 20%p).
 */
public class NetheriteScrapRepair extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent e) {
        AnvilInventory inv = e.getInventory();
        ItemStack base = inv.getFirstItem();
        ItemStack mat = inv.getSecondItem();

        if (base == null || mat == null) return;
        if (mat.getType() != Material.NETHERITE_SCRAP) return;
        if (!base.getType().name().startsWith("NETHERITE_")) return;

        int max = base.getType().getMaxDurability();
        if (max <= 0) return; // 내구도 없는 아이템(잉곳, 업그레이드 등) 제외
        if (!(base.getItemMeta() instanceof Damageable meta) || meta.getDamage() <= 0) return;

        double percent = Math.max(1.0, getConfig().getDouble("repair-percent", 45.0));
        int perUnit = (int) Math.floor(max * percent / 100.0);
        if (perUnit < 1) return;

        int need = (int) Math.ceil(meta.getDamage() / (double) perUnit);
        int units = Math.min(need, mat.getAmount());

        ItemStack result = base.clone();
        Damageable resultMeta = (Damageable) result.getItemMeta();
        resultMeta.setDamage(Math.max(0, meta.getDamage() - units * perUnit));
        result.setItemMeta(resultMeta);

        e.setResult(result);
        AnvilView view = e.getView();
        view.setRepairCost(units * getConfig().getInt("level-cost-per-scrap", 1));
        view.setRepairItemCountCost(units);
    }
}
