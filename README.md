# RtChanger - Hytale Runtime Recipe Changer

RtChanger is a lightweight, standalone utility mod for Hytale that allows server owners to modify crafting recipes dynamically at runtime via a simple JSON configuration.

## Features
- **Dynamic Overrides**: Modify any crafting recipe (custom or vanilla) without restarting the server or modifying asset files.
- **Auto-Detection**: Automatically identifies generated recipes for a given Item ID.
- **Sync Technology**: Uses the internal Hytale AssetStore synchronization to ensure UI (Workbench) and logic are always in sync.
- **Flexible Config**: Define multiple ingredients and amounts per recipe.

## Installation
1. Place `RtChanger.jar` into your `mods/` directory.
2. After the first start, a configuration file is created at `mods/tiffy/rt_overrides.json`.
3. Edit the JSON and restart (or wait for the mod's auto-retry interval).

## Configuration Example
File: `mods/tiffy/rt_overrides.json`
```json
{
    "overrides": [
        {
            "targetItemId": "Jewelry_Fly_Ring",
            "ingredients": [
                { "id": "Ingredient_Bar_Iron", "amount": 1500 },
                { "id": "Ingredient_Bar_Gold", "amount": 250 }
            ]
        }
    ]
}
```

## Credits
Developed by tiffy.
