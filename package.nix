{ lib, stdenv, jdk, gradle, makeWrapper, makeDesktopItem, copyDesktopItems, ... }:
let
  self = stdenv.mkDerivation rec {
    pname = "opvaultfx";
    version = "0.4.0";

    src = lib.fileset.toSource {
      root = ./.;
      fileset = lib.fileset.unions [
        ./src
        ./build.gradle
        ./settings.gradle
        ./LICENSE
      ];
    };

    mitmCache = gradle.fetchDeps {
      pkg = self;
      data = ./package-deps.json;
    };

    nativeBuildInputs = [
      gradle
      makeWrapper
      copyDesktopItems
    ];

    buildInputs = [
      jdk
    ];

    installPhase = ''
      runHook preInstall

      mkdir -p $out/bin $out/lib
      tar --extract --verbose --directory build/distributions --file build/distributions/${pname}-${version}.tar
      cp build/distributions/${pname}-${version}/bin/opvaultfx $out/bin/opvaultfx-unwrapped
      cp build/distributions/${pname}-${version}/lib/* $out/lib
      rm $out/lib/javafx-base-17.jar $out/lib/javafx-graphics-17.jar
      makeWrapper $out/bin/opvaultfx-unwrapped $out/bin/opvaultfx --set JAVA_HOME ${jdk}
  
      runHook postInstall
    '';

    desktopItem = makeDesktopItem {
      name = pname;
      desktopName = pname;
      exec = pname;
      comment = "JavaFX application to read 1password's opvaults";
      categories = [ "Utility" ];
    };
    desktopItems = [ desktopItem ];

    meta = with lib; {
      description = "JavaFX application to read 1password's opvaults";
      mainProgram = "opvaultfx";
      homepage = "https://github.com/ingon/opvaultfx";
      license = licenses.mit;
      platforms = platforms.all;
      sourceProvenance = with sourceTypes; [
        fromSource
        binaryBytecode
      ];
    };
  };
in
self
