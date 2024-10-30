{ lib, stdenv, fetchFromGitHub, jdk, gradle, ... }:
let
  self = stdenv.mkDerivation rec {
    pname = "opvaultfx";
    version = "0.4.0";

    src = fetchFromGitHub {
      owner = "ingon";
      repo = "opvaultfx";
      rev = "master";
      hash = "sha256-h/HNRZHUG4HUJZFBxwPgU6hRwbEbY3kPpDkB5ib+R04=";
    };

    mitmCache = gradle.fetchDeps {
      pkg = self;
      data = ./package-deps.json;
    };

    nativeBuildInputs = [
      gradle
    ];

    buildInputs = [
      jdk
    ];

    installPhase = ''
      runHook preInstall

      mkdir -p $out/bin $out/lib
      tar --extract --verbose --directory build/distributions --file build/distributions/${pname}-${version}.tar
      cp build/distributions/${pname}-${version}/bin/* $out/bin
      cp build/distributions/${pname}-${version}/lib/* $out/lib
      rm $out/lib/javafx-base-17.jar $out/lib/javafx-graphics-17.jar

      runHook postInstall
    '';

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
