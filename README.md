JCPlugin [![JCPlugin](https://github.com/Jomcraft-Network/JCPlugin/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/Jomcraft-Network/JCPlugin/actions/workflows/build.yml)
=============

JCPlugin is a Core Plugin Loader for the DefaultSettings Minecraft mod, created by the Jomcraft Network development team. Official downloads and further information can be found on our [curseforge page](https://www.curseforge.com/minecraft/mc-mods/jcplugin).

## Usage
Simply add this to your build.gradle (Forge 1.12.2 - 1.19.x / Fabric 1.16.x - 1.19.x) or your dependencies block once you added Cursemaven:

```md
dependencies {
    implementation fg.deobf("curse.maven:jcp-659192:[file-id]")
}
```

Replace `[file-id]` with the curseforge ID of the file you need.

## License
This project is licensed under the **Apache License Version 2.0** license. We do not grant any type of warranty.
