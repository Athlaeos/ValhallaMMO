Integrate ValhallaMMO into your project with Maven
```
<repositories>
    <repository>
        <id>repsy</id>
        <url>https://repo.repsy.io/mvn/athlaeos/valhallammo</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>me.athlaeos</groupId>
        <artifactId>valhallammo-dist</artifactId>
        <version>dev-0.4</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```


# Welcome to ValhallaMMO
ValhallaMMO is a large plugin with many features, but its main goal is to overhaul regular gameplay to be more involved and immersive.
Your game impact is now heavily influenced by how skilled/levelled you are at something. This can mean stronger and more durable tools from crafting, stronger potions and enchantments, greater damage when fighting, special abilities, etc.
I tried my best to also encourage different playstyles so players have to decide what path they choose to take. 

It is plug-and-play, meaning it is completely featured right out the box and little to not further additional steps are required to start playing, but it also comes with many tools to customize the plugin to make it more unique to your server.
In the future, it will have several configuration presets you can choose from, but the default one will be the closest to vanilla and easiest to learn.

Features include
- Leaderboards to compare your progress with others
- Party system with progression
- Loot table editors 
  - Available for Mob Drops, Containers, Fishing, Archaeology(requires 1.20+), and Piglin Bartering
  - Entirely GUI-based
  - Items may also be turned into sort of "loot bags" which open up when interacted with
  - Drop chances may be weighted, affected by luck and/or fortune/looting, and have lots of environmental conditions available. 
- Recipe editors
  - Entirely GUI-based
  - Powerful functions to edit items based on player progress, or to apply conditions to recipes. These functions are available to any item-based feature in ValhallaMMO
- New recipe types
  - Immersive, requiring the player to spend some time hammering away at a crafting station of choice to craft something
  - Cooking, requiring the player to combine a number of ingredients within a cauldron and either letting it boil for a given amount of time, or to trigger the recipe with a catalyst
- Many new item and player stats
- Customizable skill trees you can navigate through, allowing a virtually infinitely sized skill tree (not that this is recommended)
- New potion effects with an effect indicator
- Enemy progression, gaining increased stats as you level up
- Global effect boosters, which you may sell in your server shops as EULA-friendly server perks
- Likewise, the plugin by default includes damage indicators to tell you how much damage you're doing (Requires "Decent Holograms" to be installed on 1.19, 1.20+ uses TextDisplay)
  - By default only enabled on "dummies", which are armor stands wearing an item marked with the "dummy" tag
  - Display the damage type that is dealt
  - Display DPS (Damage Per Second) as well as critical hits

I know it's a lot to take in, but I've done my best to design its default features to not be overwhelming for the average player. 
All this customization is completely optional and so you will probably not even need to touch it. But if you want to customize the plugin, it's there for you to use.

The wiki is not yet finished, and will be worked on once BETA is released.

To install it, download the jar in the releases and insert it into your plugins folder.
If you want the resource pack, execute the "/val resourcepack setup" command to install it on your server.
The plugin works on Spigot and its forks, on version 1.19 and above. 

# Maven
```
not implemented yet
```
