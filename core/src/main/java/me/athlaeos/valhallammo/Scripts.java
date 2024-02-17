package me.athlaeos.valhallammo;

public class Scripts {
//    private Map<String, Map<String, Material>> results = new HashMap<>();
//    private Map<String, String[]> shapes = new HashMap<>();
//    private Map<String, String[]> armorShapes = new HashMap<>();
//    private Map<String, Double> weaponDamageScaling = new HashMap<>();
//    private Map<String, Integer> matTier = Map.of("wooden", 0, "leather", 0, "stone", 1, "chainmail", 1,
//            "iron", 2, "golden", 2, "diamond", 3);
//    private Map<String, Integer> matValue = Map.of("wooden", 50, "leather", 150, "stone", 100, "chainmail", 25,
//            "iron", 200, "golden", 250, "diamond", 1600);
//    private Map<String, Material> toolMats = Map.of("wooden", Material.OAK_PLANKS, "stone", Material.COBBLESTONE,
//            "golden", Material.GOLD_INGOT, "iron", Material.IRON_INGOT, "diamond", Material.DIAMOND);
//    private Map<String, Double> weaponBaseDamage = new HashMap<>();
//    private Map<String, Double> weaponBaseSpeed = Map.of("rapier", 1.8D, "dagger", 1.8D,
//            "warhammer", 1D, "greataxe", 0.5D, "mace", 1.6D, "spear", 0.8D);
//    private Collection<String> blunt = Set.of("mace", "warhammer");
//    private Collection<String> tools = Set.of("axe", "pickaxe", "hoe", "shovel");
//    private Map<String, Material> armorMats = Map.of("leather", Material.LEATHER, "chainmail", Material.IRON_NUGGET,
//            "golden", Material.GOLD_INGOT, "iron", Material.IRON_INGOT, "diamond", Material.DIAMOND);
//    private Map<String, Integer> modelDatas = Map.of("rapier", 1981822, "mace", 1981821, "dagger", 1981820,
//            "warhammer", 1981825, "spear", 1981826, "greataxe", 1981827);
//    private Map<String, Integer> toolIds = Map.of("rapier", 102, "mace", 101, "dagger", 100,
//            "warhammer", 105, "spear", 106, "greataxe", 107);
//    private Collection<String> weapons = Set.of("sword", "rapier", "dagger", "mace", "spear", "warhammer", "greataxe");
//
//    {
//        results.put("diamond",
//                Map.of(
//                        "helmet", Material.DIAMOND_HELMET,
//                        "chestplate", Material.DIAMOND_CHESTPLATE,
//                        "leggings", Material.DIAMOND_LEGGINGS,
//                        "boots", Material.DIAMOND_BOOTS,
//                        "axe", Material.DIAMOND_AXE,
//                        "pickaxe", Material.DIAMOND_PICKAXE,
//                        "hoe", Material.DIAMOND_HOE,
//                        "shovel", Material.DIAMOND_SHOVEL,
//                        "weapon", Material.DIAMOND_SWORD
//                )
//        );
//        results.put("iron",
//                Map.of(
//                        "helmet", Material.IRON_HELMET,
//                        "chestplate", Material.IRON_CHESTPLATE,
//                        "leggings", Material.IRON_LEGGINGS,
//                        "boots", Material.IRON_BOOTS,
//                        "axe", Material.IRON_AXE,
//                        "pickaxe", Material.IRON_PICKAXE,
//                        "hoe", Material.IRON_HOE,
//                        "shovel", Material.IRON_SHOVEL,
//                        "weapon", Material.IRON_SWORD
//                )
//        );
//        results.put("golden",
//                Map.of(
//                        "helmet", Material.GOLDEN_HELMET,
//                        "chestplate", Material.GOLDEN_CHESTPLATE,
//                        "leggings", Material.GOLDEN_LEGGINGS,
//                        "boots", Material.GOLDEN_BOOTS,
//                        "axe", Material.GOLDEN_AXE,
//                        "pickaxe", Material.GOLDEN_PICKAXE,
//                        "hoe", Material.GOLDEN_HOE,
//                        "shovel", Material.GOLDEN_SHOVEL,
//                        "weapon", Material.GOLDEN_SWORD
//                )
//        );
//        results.put("chainmail",
//                Map.of(
//                        "helmet", Material.CHAINMAIL_HELMET,
//                        "chestplate", Material.CHAINMAIL_CHESTPLATE,
//                        "leggings", Material.CHAINMAIL_LEGGINGS,
//                        "boots", Material.CHAINMAIL_BOOTS
//                )
//        );
//        results.put("leather",
//                Map.of(
//                        "helmet", Material.LEATHER_HELMET,
//                        "chestplate", Material.LEATHER_CHESTPLATE,
//                        "leggings", Material.LEATHER_LEGGINGS,
//                        "boots", Material.LEATHER_BOOTS
//                )
//        );
//        results.put("stone",
//                Map.of(
//                        "axe", Material.STONE_AXE,
//                        "pickaxe", Material.STONE_PICKAXE,
//                        "hoe", Material.STONE_HOE,
//                        "shovel", Material.STONE_SHOVEL,
//                        "weapon", Material.STONE_SWORD
//                )
//        );
//        results.put("wooden",
//                Map.of(
//                        "axe", Material.WOODEN_AXE,
//                        "pickaxe", Material.WOODEN_PICKAXE,
//                        "hoe", Material.WOODEN_HOE,
//                        "shovel", Material.WOODEN_SHOVEL,
//                        "weapon", Material.WOODEN_SWORD
//                )
//        );
//
//        shapes.put("sword", new String[]{" M ", " M ", " S "});
//        shapes.put("rapier", new String[]{"  M", " M ", "S  "});
//        shapes.put("dagger", new String[]{"   ", " M ", "S  "});
//        shapes.put("warhammer", new String[]{"MMM", " SM", "S  "});
//        shapes.put("greataxe", new String[]{"M M", "MSM", " S "});
//        shapes.put("mace", new String[]{" MM", " SM", "S  "});
//        shapes.put("spear", new String[]{"  M", " S ", "S  "});
//        shapes.put("axe", new String[]{" MM", " SM", " S "});
//        shapes.put("pickaxe", new String[]{"MMM", " S ", " S "});
//        shapes.put("shovel", new String[]{" M ", " S ", " S "});
//        shapes.put("hoe", new String[]{" MM", " S ", " S "});
//
//        armorShapes.put("helmet", new String[]{"MMM", "M M", "   "});
//        armorShapes.put("chestplate", new String[]{"M M", "MMM", "MMM"});
//        armorShapes.put("leggings", new String[]{"MMM", "M M", "M M"});
//        armorShapes.put("boots", new String[]{"M M", "M M", "   "});
//
//        weaponDamageScaling.put("rapier", 0.5D);
//        weaponDamageScaling.put("dagger", 1D);
//        weaponDamageScaling.put("warhammer", 1D);
//        weaponDamageScaling.put("greataxe", 1.5D);
//        weaponDamageScaling.put("mace", 1D);
//        weaponDamageScaling.put("spear", 1D);
//        weaponDamageScaling.put("hoe", 0D);
//
//        weaponBaseDamage.put("rapier", 2D);
//        weaponBaseDamage.put("dagger", 4D);
//        weaponBaseDamage.put("warhammer", 6D);
//        weaponBaseDamage.put("greataxe", 9D);
//        weaponBaseDamage.put("mace", 2.5D);
//        weaponBaseDamage.put("spear", 5D);
//    }

//                for (DynamicGridRecipe recipe : CustomRecipeRegistry.getGridRecipes().values()){
////                    if (recipe.getName().startsWith("salvage_")){
////                        recipe.setDisplayName("<lang.recipe-displayname-salvage>");
////                    }
//                    for (DynamicItemModifier mod : new ArrayList<>(recipe.getModifiers())){
//                        if (mod instanceof ItemType i) {
//                            Material originalMat = i.getMaterial();
//                            if (originalMat == Material.IRON_INGOT) originalMat = Material.IRON_NUGGET;
//                            if (originalMat == Material.GOLD_INGOT) originalMat = Material.GOLD_NUGGET;
//                            ItemReplaceKeepingAmount newMod = new ItemReplaceKeepingAmount("replace_keep_amount");
//                            newMod.setReplaceBy(new ItemStack(originalMat));
//                            newMod.setPriority(ModifierPriority.LATER);
//                            recipe.getModifiers().add(newMod);
//                            recipe.getModifiers().remove(mod);
//                        }
//                    }
//                    DynamicItemModifier.sortModifiers(recipe.getModifiers());
//                }
    //.recipe recipename
//                for (String s : toolMats.keySet()){
//                    for (String tool : shapes.keySet()){
//                        List<DynamicItemModifier> modifiers = new ArrayList<>();
//                        FlagCustomAdd d1 = new FlagCustomAdd("flag_display_attributes", CustomFlag.DISPLAY_ATTRIBUTES);
//                        d1.setPriority(ModifierPriority.SOONEST);
//                        modifiers.add(d1);
//
//                        FlagVanillaAdd d2 = new FlagVanillaAdd("flag_hide_attributes", ItemFlag.HIDE_ATTRIBUTES);
//                        d2.setPriority(ModifierPriority.SOONEST);
//                        modifiers.add(d2);
//
//                        SmithingQualityScale d3 = new SmithingQualityScale("smithing_quality_scale");
//                        d3.setSkillEfficiency(1);
//                        d3.setPriority(ModifierPriority.SOON);
//                        modifiers.add(d3);
//
//                        SmithingNeutralQualitySet d4 = new SmithingNeutralQualitySet("smithing_neutral_quality_set");
//                        d4.setPriority(ModifierPriority.SOONEST);
//                        d4.setNeutral(50 + (matTier.getOrDefault(s, 0) * 30));
//                        modifiers.add(d4);
//
//                        double baseDamage = weaponBaseDamage.getOrDefault(tool, -1D);
//                        double scaling = weaponDamageScaling.getOrDefault(tool, 1D);
//                        double damage = baseDamage < 0 ? -1 : baseDamage + (matTier.get(s) * scaling);
//                        if (blunt.contains(tool) && damage >= 0){
//                            DefaultAttributeAdd d5 = new DefaultAttributeAdd("attribute_add_extra_bludgeoning_damage", "EXTRA_BLUDGEONING_DAMAGE", 0.1, 1, Material.COBBLESTONE);
//                            d5.setPriority(ModifierPriority.SOON);
//                            d5.setValue(damage);
//                            modifiers.add(d5);
//
//                            DefaultAttributeRemove d6 = new DefaultAttributeRemove("attribute_remove_generic_attack_damage", "GENERIC_ATTACK_DAMAGE", Material.IRON_SWORD);
//                            d6.setPriority(ModifierPriority.SOON);
//                            modifiers.add(d6);
//
//                            DefaultAttributeScale d7 = new DefaultAttributeScale("attribute_scale_extra_bludgeoning_damage", "EXTRA_BLUDGEONING_DAMAGE", Material.COBBLESTONE);
//                            d7.setSkillRange(275 - (matTier.get(s) * 30));
//                            d7.setMode(Scaling.ScalingMode.ADD_ON_DEFAULT);
//                            d7.setAmplifier(4 + (scaling * matTier.get(s)));
//                            d7.setRangeOffset(25 + (matTier.get(s) * 30));
//                            d7.setPriority(ModifierPriority.NEUTRAL);
//                            d7.setMinimum(0);
//                            modifiers.add(d7);
//                        } else {
//                            if (damage >= 0){
//                                DefaultAttributeAdd d5 = new DefaultAttributeAdd("attribute_add_generic_attack_damage", "GENERIC_ATTACK_DAMAGE", 0.1, 1, Material.IRON_SWORD);
//                                d5.setPriority(ModifierPriority.SOON);
//                                d5.setValue(damage);
//                                modifiers.add(d5);
//                            }
//
//                            if (scaling > 0){
//                                DefaultAttributeScale d7 = new DefaultAttributeScale("attribute_scale_generic_attack_damage", "GENERIC_ATTACK_DAMAGE", Material.IRON_SWORD);
//                                d7.setSkillRange(275 - (matTier.get(s) * 30));
//                                d7.setMode(Scaling.ScalingMode.ADD_ON_DEFAULT);
//                                d7.setAmplifier(4 + (scaling * matTier.get(s)));
//                                d7.setRangeOffset(25 + (matTier.get(s) * 30));
//                                d7.setPriority(ModifierPriority.NEUTRAL);
//                                d7.setMinimum(0);
//                                modifiers.add(d7);
//                            }
//                        }
//                        if (weaponBaseSpeed.containsKey(tool)){
//                            DefaultAttributeAdd d5 = new DefaultAttributeAdd("attribute_add_generic_attack_speed", "GENERIC_ATTACK_SPEED", 0.1, 1, Material.GOLDEN_SWORD);
//                            d5.setPriority(ModifierPriority.SOON);
//                            d5.setValue(weaponBaseSpeed.get(tool));
//                            modifiers.add(d5);
//                        }
//
//                        DefaultAttributeScale d7 = new DefaultAttributeScale("attribute_scale_generic_attack_speed", "GENERIC_ATTACK_SPEED", Material.GOLDEN_SWORD);
//                        d7.setSkillRange(275 - (matTier.get(s) * 30));
//                        d7.setMode(Scaling.ScalingMode.ADD_ON_DEFAULT);
//                        d7.setAmplifier(0.2);
//                        d7.setRangeOffset(25 + (matTier.get(s) * 30));
//                        d7.setMinimum(0);
//                        d7.setPriority(ModifierPriority.NEUTRAL);
//                        modifiers.add(d7);
//
//                        if (tools.contains(tool)){
//                            DefaultAttributeAdd d5 = new DefaultAttributeAdd("attribute_add_dig_speed", "DIG_SPEED", 0.01, 0.1, Material.DIAMOND_PICKAXE);
//                            d5.setPriority(ModifierPriority.SOON);
//                            d5.setValue(0);
//                            modifiers.add(d5);
//
//                            DefaultAttributeScale d8 = new DefaultAttributeScale("attribute_scale_dig_speed", "DIG_SPEED", Material.DIAMOND_PICKAXE);
//                            d8.setSkillRange(275 - (matTier.get(s) * 30));
//                            d8.setMode(Scaling.ScalingMode.ADD_ON_DEFAULT);
//                            d8.setAmplifier(0.5);
//                            d8.setRangeOffset(25 + (matTier.get(s) * 30));
//                            d8.setMinimum(0);
//                            d8.setPriority(ModifierPriority.LATER);
//                            modifiers.add(d8);
//                        }
//                        ItemBuilder result = new ItemBuilder(Material.BARRIER);
//                        if (modelDatas.containsKey(tool)){
//                            CustomModelDataSet set = new CustomModelDataSet("custom_model_data");
//                            set.setCustomModelData(modelDatas.get(tool));
//                            set.setPriority(ModifierPriority.SOONEST);
//                            modifiers.add(set);
//                            result.data(modelDatas.get(tool));
//
//                            result.name("&r<lang.material-" + s + "-" + tool + ">");
//                        }
//                        if (toolIds.containsKey(tool)){
//                            CustomIDSet id = new CustomIDSet("custom_id");
//                            id.setCustomID(toolIds.get(tool));
//                            id.setPriority(ModifierPriority.SOONEST);
//                            modifiers.add(id);
//
//                            CustomID.setID(result.getMeta(), toolIds.get(tool));
//                        }
//                        if (weapons.contains(tool)) result.type(results.getOrDefault(s, new HashMap<>()).getOrDefault("weapon", Material.BARRIER));
//                        else result.type(results.getOrDefault(s, new HashMap<>()).getOrDefault(tool, Material.BARRIER));
//
//                        DurabilityScale d9 = new DurabilityScale("durability_scale");
//                        d9.setSkillRange(275 - (matTier.get(s) * 30));
//                        d9.setMode(Scaling.ScalingMode.MULTIPLIER);
//                        d9.setAmplifier(2);
//                        d9.setRangeOffset(25 + (matTier.get(s) * 30));
//                        d9.setMinimum(1);
//                        d9.setPriority(ModifierPriority.LATER);
//                        modifiers.add(d9);
//
//                        Map<Integer, SlotEntry> items = new HashMap<>();
//                        String[] shape = shapes.get(tool);
//                        int put = 0;
//                        int index = 0;
//                        for (String value : shape) {
//                            for (int o = 0; o < value.length(); o++) {
//                                char c = value.charAt(o);
//                                if (c == ' ') {
//                                    index++;
//                                    continue;
//                                }
//                                SlotEntry toPut;
//                                if (c == 'S')
//                                    toPut = new SlotEntry(new ItemStack(Material.STICK), new MaterialChoice());
//                                else if (c == 'M') {
//                                    toPut = new SlotEntry(new ItemStack(toolMats.get(s)), toolMats.get(s) == Material.OAK_PLANKS || toolMats.get(s) == Material.COBBLESTONE ? new SimilarMaterialsChoice() : new MaterialChoice());
//                                    put++;
//                                } else continue;
//                                items.put(index, toPut);
//                                index++;
//                            }
//                        }
//                        SkillExperience d8 = new SkillExperience("reward_smithing_experience", "SMITHING");
//                        d8.setAmount(put * matValue.getOrDefault(s, 0));
//                        d8.setPriority(ModifierPriority.LAST);
//                        if (put * matValue.getOrDefault(s, 0) > 0) modifiers.add(d8);
//
//                        DynamicItemModifier.sortModifiers(modifiers);
//
//                        String name = "craft_" + s + "_" + tool;
//                        DynamicGridRecipe recipe = new DynamicGridRecipe(name);
//                        recipe.setUnlockedForEveryone(!weaponBaseDamage.containsKey(tool));
//                        recipe.setModifiers(modifiers);
//                        recipe.setItems(items);
//                        recipe.setResult(result.get());
//
//                        CustomRecipeRegistry.register(recipe, true);
//
//                        List<DynamicItemModifier> salvageMods = new ArrayList<>();
//                        AmountSet m2 = new AmountSet("amount_set");
//                        m2.setPriority(ModifierPriority.SOONEST);
//                        m2.setAmount(put);
//                        salvageMods.add(m2);
//
//                        AmountScale m1 = new AmountScale("amount_scale");
//                        m1.setMinimumFraction(0);
//                        m1.setDamagePenalty(true);
//                        m1.setSkillEfficiency(1);
//                        m1.setPriority(ModifierPriority.NEUTRAL);
//                        salvageMods.add(m1);
//
//                        ItemType m3 = new ItemType("material");
//                        m3.setPriority(ModifierPriority.LAST);
//                        m3.setMaterial(toolMats.get(s));
//                        salvageMods.add(m3);
//
//                        DynamicItemModifier.sortModifiers(salvageMods);
//
//                        Map<Integer, SlotEntry> i = new HashMap<>(Map.of(4, new SlotEntry(result.get(),
//                                weapons.contains(tool) ? new MaterialWithIDChoice() : new MaterialChoice())));
//
//                        String n = "salvage_" + s + "_" + tool;
//                        DynamicGridRecipe r = new DynamicGridRecipe(n);
//                        r.setUnlockedForEveryone(false);
//                        r.setModifiers(salvageMods);
//                        r.setItems(i);
//                        r.setRequireValhallaTools(true);
//                        r.setTinker(true);
//                        CustomRecipeRegistry.register(r, true);
//                    }
//                }
//                for (DynamicGridRecipe recipe : CustomRecipeRegistry.getGridRecipes().values()){
//                    if (recipe.getName().contains("dagger")){
//                        DefaultAttributeAdd crit = new DefaultAttributeAdd("attribute_add_crit_chance", "CRIT_CHANCE", 0.01, 0.1, Material.GOLDEN_SWORD);
//                        crit.setValue(0.2);
//                        crit.setPriority(ModifierPriority.SOON);
//                        recipe.getModifiers().add(crit);
//
//                        DefaultAttributeAdd range = new DefaultAttributeAdd("attribute_add_attack_reach", "ATTACK_REACH", 0.1, 1, Material.ENDER_PEARL);
//                        range.setValue(-1);
//                        range.setPriority(ModifierPriority.SOON);
//                        recipe.getModifiers().add(range);
//                    } else if (recipe.getName().contains("mace")){
//                        DefaultAttributeAdd m1 = new DefaultAttributeAdd("attribute_add_stun_chance", "STUN_CHANCE", 0.01, 0.1, Material.IRON_BLOCK);
//                        m1.setValue(0.2);
//                        m1.setPriority(ModifierPriority.SOON);
//                        recipe.getModifiers().add(m1);
//                    } else if (recipe.getName().contains("rapier")){
//                        DefaultAttributeAdd m1 = new DefaultAttributeAdd("attribute_add_immunity_reduction", "IMMUNITY_REDUCTION", 0.01, 0.1, Material.WITHER_ROSE);
//                        m1.setValue(0.5);
//                        m1.setPriority(ModifierPriority.SOON);
//                        recipe.getModifiers().add(m1);
//
//                        DefaultAttributeAdd m2 = new DefaultAttributeAdd("attribute_add_armor_penetration_fraction", "ARMOR_PENETRATION_FRACTION", 0.01, 0.1, Material.LEATHER_CHESTPLATE);
//                        m2.setValue(0.3);
//                        m2.setPriority(ModifierPriority.SOON);
//                        recipe.getModifiers().add(m2);
//
//                        DefaultAttributeAdd m3 = new DefaultAttributeAdd("attribute_add_knockback", "KNOCKBACK", 0.01, 0.1, Material.SLIME_BLOCK);
//                        m3.setValue(-2);
//                        m3.setPriority(ModifierPriority.SOON);
//                        recipe.getModifiers().add(m3);
//                    } else if (recipe.getName().contains("warhammer")){
//                        DefaultAttributeAdd m1 = new DefaultAttributeAdd("attribute_add_stun_chance", "STUN_CHANCE", 0.01, 0.1, Material.IRON_BLOCK);
//                        m1.setValue(0.2);
//                        m1.setPriority(ModifierPriority.SOON);
//                        recipe.getModifiers().add(m1);
//
//                        DefaultAttributeAdd m3 = new DefaultAttributeAdd("attribute_add_knockback", "KNOCKBACK", 0.01, 0.1, Material.SLIME_BLOCK);
//                        m3.setValue(0.5);
//                        m3.setPriority(ModifierPriority.SOON);
//                        recipe.getModifiers().add(m3);
//                    } else if (recipe.getName().contains("spear")){
//                        DefaultAttributeAdd m1 = new DefaultAttributeAdd("attribute_add_velocity_damage", "VELOCITY_DAMAGE", 0.01, 0.1, Material.IRON_BLOCK);
//                        m1.setValue(0.3);
//                        m1.setPriority(ModifierPriority.SOON);
//                        recipe.getModifiers().add(m1);
//
//                        DefaultAttributeAdd range = new DefaultAttributeAdd("attribute_add_attack_reach", "ATTACK_REACH", 0.1, 1, Material.ENDER_PEARL);
//                        range.setValue(1);
//                        range.setPriority(ModifierPriority.SOON);
//                        recipe.getModifiers().add(range);
//                    } else if (recipe.getName().contains("greataxe")){
//                        DefaultAttributeAdd crit = new DefaultAttributeAdd("attribute_add_crit_chance", "CRIT_CHANCE", 0.01, 0.1, Material.GOLDEN_SWORD);
//                        crit.setValue(0.2);
//                        crit.setPriority(ModifierPriority.SOON);
//                        recipe.getModifiers().add(crit);
//                    }
//                    DynamicItemModifier.sortModifiers(recipe.getModifiers());
//                }
//                for (String s : armorMats.keySet()){
//                    for (String armor : armorShapes.keySet()){
//                        List<DynamicItemModifier> modifiers = new ArrayList<>();
//                        FlagCustomAdd d1 = new FlagCustomAdd("flag_display_attributes", CustomFlag.DISPLAY_ATTRIBUTES);
//                        d1.setPriority(ModifierPriority.SOONEST);
//                        modifiers.add(d1);
//
//                        FlagVanillaAdd d2 = new FlagVanillaAdd("flag_hide_attributes", ItemFlag.HIDE_ATTRIBUTES);
//                        d2.setPriority(ModifierPriority.SOONEST);
//                        modifiers.add(d2);
//
//                        SmithingQualityScale d3 = new SmithingQualityScale("smithing_quality_scale");
//                        d3.setSkillEfficiency(1);
//                        d3.setPriority(ModifierPriority.SOON);
//                        modifiers.add(d3);
//
//                        SmithingNeutralQualitySet d4 = new SmithingNeutralQualitySet("smithing_neutral_quality_set");
//                        d4.setPriority(ModifierPriority.SOONEST);
//                        d4.setNeutral(50 + (matTier.getOrDefault(s, 0) * 30));
//                        modifiers.add(d4);
//
//                        ItemBuilder result = new ItemBuilder(Material.BARRIER);
//                        result.type(results.getOrDefault(s, new HashMap<>()).getOrDefault(armor, Material.BARRIER));
//                        DefaultAttributeScale d7 = new DefaultAttributeScale("attribute_scale_generic_armor", "GENERIC_ARMOR", Material.IRON_CHESTPLATE);
//                        d7.setSkillRange(275 - (matTier.get(s) * 30));
//                        d7.setMode(Scaling.ScalingMode.MULTIPLIER);
//                        d7.setAmplifier(0.5);
//                        d7.setRangeOffset(25 + (matTier.get(s) * 30));
//                        d7.setMinimum(1);
//                        d7.setPriority(ModifierPriority.NEUTRAL);
//                        modifiers.add(d7);
//                        if (s.equals("diamond")){
//                            DefaultAttributeScale d8 = new DefaultAttributeScale("attribute_scale_generic_armor_toughness", "GENERIC_ARMOR_TOUGHNESS", Material.DIAMOND_CHESTPLATE);
//                            d8.setSkillRange(275 - (matTier.get(s) * 30));
//                            d8.setMode(Scaling.ScalingMode.MULTIPLIER);
//                            d8.setAmplifier(1);
//                            d8.setRangeOffset(25 + (matTier.get(s) * 30));
//                            d8.setMinimum(1);
//                            d8.setPriority(ModifierPriority.NEUTRAL);
//                            modifiers.add(d8);
//                        }
//
//                        DurabilityScale d8 = new DurabilityScale("durability_scale");
//                        d8.setSkillRange(275 - (matTier.get(s) * 30));
//                        d8.setMode(Scaling.ScalingMode.MULTIPLIER);
//                        d8.setAmplifier(2);
//                        d8.setRangeOffset(25 + (matTier.get(s) * 30));
//                        d8.setMinimum(1);
//                        d8.setPriority(ModifierPriority.LATER);
//                        modifiers.add(d8);
//
//                        Map<Integer, SlotEntry> items = new HashMap<>();
//                        String[] shape = armorShapes.get(armor);
//                        int put = 0;
//                        for (int i = 0; i < shape.length; i++){
//                            for (int o = 0; o < shape[i].length(); o++){
//                                char c = shape[i].charAt(o);
//                                if (c == ' ') continue;
//                                int index = (i + 1) * (o + 1) - 1;
//                                SlotEntry toPut;
//                                if (c == 'M') {
//                                    toPut = new SlotEntry(new ItemStack(armorMats.get(s)), new MaterialChoice());
//                                    put++;
//                                } else continue;
//                                items.put(index, toPut);
//                            }
//                        }
//                        SkillExperience d9 = new SkillExperience("reward_smithing_experience", "SMITHING");
//                        d9.setAmount(put * matValue.getOrDefault(s, 0));
//                        d9.setPriority(ModifierPriority.LAST);
//                        if (put * matValue.getOrDefault(s, 0) > 0) modifiers.add(d9);
//
//                        DynamicItemModifier.sortModifiers(modifiers);
//
//                        String name = "craft_" + s + "_" + armor;
//                        DynamicGridRecipe recipe = new DynamicGridRecipe(name);
//                        recipe.setUnlockedForEveryone(true);
//                        recipe.setModifiers(modifiers);
//                        recipe.setItems(items);
//                        recipe.setResult(result.get());
//
//                        CustomRecipeRegistry.register(recipe, true);
//
//                        List<DynamicItemModifier> salvageMods = new ArrayList<>();
//                        AmountSet m2 = new AmountSet("amount_set");
//                        m2.setPriority(ModifierPriority.SOONEST);
//                        m2.setAmount(put);
//                        salvageMods.add(m2);
//
//                        AmountScale m1 = new AmountScale("amount_scale");
//                        m1.setMinimumFraction(0);
//                        m1.setDamagePenalty(true);
//                        m1.setSkillEfficiency(1);
//                        m1.setPriority(ModifierPriority.NEUTRAL);
//                        salvageMods.add(m1);
//
//                        ItemType m3 = new ItemType("material");
//                        m3.setPriority(ModifierPriority.LAST);
//                        m3.setMaterial(armorMats.get(s));
//                        salvageMods.add(m3.copy());
//
//                        DynamicItemModifier.sortModifiers(salvageMods);
//
//                        Map<Integer, SlotEntry> i = new HashMap<>(Map.of(4, new SlotEntry(result.get(), new MaterialChoice())));
//
//                        String n = "salvage_" + s + "_" + armor;
//                        DynamicGridRecipe r = new DynamicGridRecipe(n);
//                        r.setUnlockedForEveryone(false);
//                        r.setModifiers(salvageMods);
//                        r.setItems(i);
//                        r.setRequireValhallaTools(true);
//                        r.setTinker(true);
//                        CustomRecipeRegistry.register(r, true);
//                    }
//                }
}
