package me.athlaeos.valhallammo.trading.services.type_implementations;

import me.athlaeos.valhallammo.dom.Action;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.dom.Question;
import me.athlaeos.valhallammo.dom.Questionnaire;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.trading.CustomMerchantManager;
import me.athlaeos.valhallammo.trading.dom.MerchantData;
import me.athlaeos.valhallammo.trading.menu.*;
import me.athlaeos.valhallammo.trading.services.Service;
import me.athlaeos.valhallammo.trading.services.ServiceRegistry;
import me.athlaeos.valhallammo.trading.services.ServiceType;
import me.athlaeos.valhallammo.trading.services.service_implementations.UpgradeService;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class UpgradingService extends ServiceType {
    private final DynamicButton button = Catch.catchOrElse(() -> new DynamicButton(CustomMerchantManager.getTradingConfig().getString("service_button_type_training", "")), null);

    @Override
    public String getID() {
        return "UPGRADING";
    }

    @Override
    public void onServiceSelect(InventoryClickEvent e, ServiceMenu menu, Service service, MerchantData data) {
        List<UpgradeService> services = new ArrayList<>();
        for (Service s : menu.getServices()){
            if (s instanceof UpgradeService us) services.add(us);
        }

        new ServiceUpgradingMenu(menu.getPlayerMenuUtility(), services, data).open();
    }

    @Override
    public ItemStack getButtonIcon(ServiceMenu menu, Service service, MerchantData data) {
        String name = TranslationManager.translatePlaceholders(CustomMerchantManager.getTradingConfig().getString("service_button_name_upgrading"));
        return button.get(ButtonSize.defaultFromButtonCount(menu.getServices().size()))
                .name(name == null ? "" : name)
                .lore(TranslationManager.translateListPlaceholders(
                        CustomMerchantManager.getTradingConfig().getStringList("service_button_description_upgrading"))
                ).get();
    }

    @Override
    public void onTypeConfigurationSelect(InventoryClickEvent e, Service service, MerchantServicesMenu menu) {
        if (service == null) {
            e.getWhoClicked().closeInventory();
            Questionnaire questionnaire = new Questionnaire((Player) e.getWhoClicked(), null, null,
                    new Question("&fWhat should the upgrading service's ID be? (type in chat, or 'cancel' to cancel)", s -> ServiceRegistry.getService(s.replaceAll(" ", "_").toLowerCase(java.util.Locale.US)) == null, "&cService with this key already exists! Try again")
            ) {
                @Override
                public Action<Player> getOnFinish() {
                    if (getQuestions().isEmpty()) return super.getOnFinish();
                    Question question = getQuestions().get(0);
                    if (question.getAnswer() == null) return super.getOnFinish();
                    return (p) -> {
                        String answer = question.getAnswer().replaceAll(" ", "_").toLowerCase(java.util.Locale.US);
                        if (answer.contains("cancel")) menu.open();
                        else if (ServiceRegistry.getService(answer) != null)
                            Utils.sendMessage(getWho(), "&cThe given type already exists!");
                        else {
                            UpgradeService upgradeService = new UpgradeService(answer);
                            ServiceRegistry.registerService(upgradeService);
                            menu.getType().getServices().add(answer);
                            new UpgradingServiceConfigurationMenu(menu.getPlayerMenuUtility(), menu, upgradeService).open();
                        }
                    };
                }
            };
            Questionnaire.startQuestionnaire((Player) e.getWhoClicked(), questionnaire);
        } else {
            if (!(service instanceof UpgradeService upgradeService)) return;
            new UpgradingServiceConfigurationMenu(menu.getPlayerMenuUtility(), menu, upgradeService).open();
        }
    }

    @Override
    public ItemStack getDefaultButton() {
        return new ItemBuilder(Material.ANVIL)
                .name("&eUpgrading")
                .lore("&7Allows players to pay merchants",
                        "&7to upgrade their items")
                .get();
    }

    @Override
    public boolean singularButton() {
        return true;
    }
}
