# RtChanger

Override any crafting recipe using a simple config file. Changes take effect on startup.

RtChanger lets you change crafting recipes through a simple configuration file. Instead of digging through asset files, you just define your changes in `rtchanger.json`.

The mod applies these overrides during startup. It automatically syncs with the in-game workbench so players see the updated costs.

**Example Config:**
File: `mods/tiffy/rtchanger.json`
```json
{
    "overrides": [
        {
            "targetItemId": "Bench_Campfire",
            "ingredients": [
                { "id": "Ingredient_Bar_Iron", "amount": 100 },
                { "id": "Ingredient_Bar_Copper", "amount": 25 }
            ]
        },
        {
            "targetItemId": "Survival_Trap_Snapjaw",
            "ingredients": [
                { "id": "Ingredient_Bar_Iron", "amount": 999 },
                { "id": "Ingredient_Bar_Copper", "amount": 999 }
            ]
        }
    ]
}
```

**How to use:**
1. Put the mod in your `mods/` folder.
2. Edit `mods/tiffy/rtchanger.json` with the Item IDs you want to change.
3. Restart your server.

It works for both vanilla Hytale items and items added by other mods.
