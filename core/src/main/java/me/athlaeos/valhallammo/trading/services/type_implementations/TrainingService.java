package me.athlaeos.valhallammo.trading.services.type_implementations;

import me.athlaeos.valhallammo.dom.Action;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.dom.Question;
import me.athlaeos.valhallammo.dom.Questionnaire;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.skills.skills.SkillRegistry;
import me.athlaeos.valhallammo.trading.CustomMerchantManager;
import me.athlaeos.valhallammo.trading.dom.MerchantData;
import me.athlaeos.valhallammo.trading.menu.*;
import me.athlaeos.valhallammo.trading.services.Service;
import me.athlaeos.valhallammo.trading.services.ServiceRegistry;
import me.athlaeos.valhallammo.trading.services.ServiceType;
import me.athlaeos.valhallammo.trading.services.service_implementations.OrderService;
import me.athlaeos.valhallammo.trading.services.service_implementations.TrainService;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TrainingService extends ServiceType {
    private final DynamicButton button = Catch.catchOrElse(() -> new DynamicButton(CustomMerchantManager.getTradingConfig().getString("service_button_type_training", "")), null);

    @Override
    public String getID() {
        return "TRAINING";
    }

    @Override
    public void onServiceSelect(InventoryClickEvent e, ServiceMenu menu, Service service, MerchantData data) {
        List<TrainService> services = new ArrayList<>();
        for (Service s : menu.getServices()){
            if (s instanceof TrainService ts) services.add(ts);
        }

        new ServiceTrainingMenu(menu.getPlayerMenuUtility(), services, data).open();
    }

    @Override
    public ItemStack getButtonIcon(ServiceMenu menu, Service service, MerchantData data) {
        String name = TranslationManager.translatePlaceholders(CustomMerchantManager.getTradingConfig().getString("service_button_name_training"));
        return button.get(ButtonSize.defaultFromButtonCount(menu.getServices().size()))
                .name(name == null ? "" : name)
                .lore(TranslationManager.translateListPlaceholders(
                        CustomMerchantManager.getTradingConfig().getStringList("service_button_description_training"))
                ).get();
    }

    @Override
    public void onTypeConfigurationSelect(InventoryClickEvent e, Service service, MerchantServicesMenu menu) {
        if (service == null) {
            e.getWhoClicked().closeInventory();
            Questionnaire questionnaire = new Questionnaire((Player) e.getWhoClicked(), null, null,
                    new Question("&fWhat should the training service's ID be? (type in chat, or 'cancel' to cancel)", s -> ServiceRegistry.getService(s.replaceAll(" ", "_").toLowerCase(java.util.Locale.US)) == null, "&cService with this key already exists! Try again")
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
                            TrainService orderService = new TrainService(answer);
                            ServiceRegistry.registerService(orderService);
                            menu.getType().getServices().add(answer);
                            new TrainingServiceConfigurationMenu(menu.getPlayerMenuUtility(), menu, orderService).open();
                        }
                    };
                }
            };
            Questionnaire.startQuestionnaire((Player) e.getWhoClicked(), questionnaire);
        } else {
            if (!(service instanceof TrainService trainService)) return;
            new TrainingServiceConfigurationMenu(menu.getPlayerMenuUtility(), menu, trainService).open();
        }
    }

    @Override
    public ItemStack getDefaultButton() {
        return new ItemBuilder(Material.WRITABLE_BOOK)
                .name("&eTraining")
                .lore("&7Allows players to purchase",
                        "&7skill levels or experience",
                        "&7in exchange for items")
                .get();
    }

    @Override
    public boolean singularButton() {
        return true;
    }
}
