ClickCrystals Utils
---
Useful terminal commands for ClickCrystals development.

### Commands
There will be more added in the future.

```yml
Name: module-table
Usage: module-table
Description: Generates and copies to your clipboard a markdown table of modules and their 
  descriptions by reading your local file system for 
  ClickCrystals module .java files.
```
```yml
Name: packets
Usage: packets -minecraftVersion
Description: Generates and copies to your clipboard a Java HashMap of Packet classes
  mapped to their respective names. This util requests data from maven.fabricmc.net
  with the specified minecraftVersion to get all possible packet names from that 
  Minecraft Version.
```
```yml
Name: packet-table
Usage: packet-table
Description: Generates and copies to your clipboard a markdown table of packets and their 
  scripting ID
```
```yml
Name: version-mappings
Usage: version-mappings
Description: Generates and copies to your clipboard a markdown table of Minecraft versions and their provided 
  ClickCrystals versions, linked to their releases tab
```
```yml
Name: entity-textures
Usage: entity-textures -minecraftVersion -raw
Description: Generates and copies to your clipboard a markdown table of Minecraft entities and their 8x8 head textures!
  These textures are saved as .png files in your 'src/main/resources/assets/clickcrystals/textures/display/icons/entities'
  folder. The last '-raw' argument is optional. Raw will let you view all available Base64 textures, while 
  executing this command without raw will give you are shorter concise list with no texture variants (only one texture per entity).
```
```yml
Name: entity-textures
Usage: entity-textures -minecraftVersion -code
Description: Generates and copies to your clipboard a Java HashMap of Entity classes mapped to their respective
  fetched head texture identifiers.
```


### How to Use?
(Optional, but it's recommended to create a batch file for this)

In the `.bat` file:
```
"%JAVA_HOME%\bin\java" -jar downloadedReleaseFilePath.jar %*
```

Then in terminal: 
```
./yourBatchFileName modules
./yourBatchFileName packets -1.21
```

Happy ClickCrystals coding!

