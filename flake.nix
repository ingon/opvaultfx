{
  description = "A flake for keihaya.com project";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs {
          inherit system;
        };
        fxjdk = (pkgs.jdk.override { enableJavaFX = true; });
        fxgradle = (pkgs.gradle_7.override { java = fxjdk; });
      in
      {
        formatter = pkgs.nixpkgs-fmt;
        devShell = pkgs.mkShell {
          buildInputs = [
            fxjdk
            fxgradle
          ];
        };
      });
}
