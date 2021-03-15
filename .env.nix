{ nixpkgs ? import <nixpkgs> {} }:
with nixpkgs;
let
  jdk = openjdk11;
  mvn = maven.override { jdk  = jdk; };
in
mkShell {

  buildInputs = [
    nodejs-12_x
    chromium
    geckodriver
    jdk
    mvn
  ];

  NPM_CONFIG_PROXY = builtins.getEnv "http_proxy";
  CHROME_BIN = "${chromium}/bin/chromium";
  JAVA_HOME="${jdk}/lib/openjdk";
  M2_HOME="${mvn}/maven";
}
