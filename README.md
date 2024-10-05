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



