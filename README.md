# opvaultfx

`opvaultfx` is a [1password vault](https://support.1password.com/opvault-design/) reader implemented in Java & JavaFX, with the following features:
* Security. It attempts to achive that by:
    - Zero copy/allocation JSON parsing. OPVault uses JSON for representing things, and by using https://github.com/ingon/json-zero we
    extract only the data we need, after which the in-memory data is zeroed out.
  * All passwords are stored encrypted in memory, decrypted only when needed (for copy/fill)
  * Every bit of information is kept in memory for a minimum time, then zeroed out.
* Works on linux
* Design. Attempts to implement google's material design (with mixed results)
* Quick filter to find the items
* Supports TOTP (HOTP is not supported since it requires write access to the vault)

## Running

To run `opvaultfx` (on linux)* you need to:
* Install OpenJDK13 (may work with other versions)**
* Download and extract `dist.zip`
* Run `./bin/opvaultfx`
  
&ast; Builds for MacOS and Windows are in works, try running from source on these OSes

** Native installers are also in works, depending on jpackage
  
To test the app, you can use [OPVault sample data](https://cache.agilebits.com/security-kb/)

### From source

To run from source, clone the repository and run `./gradlew run`. Tested with OpenJDK 13 on PopOS 18.04.

## Building

Clone the repository and run `./gradlew build`
