with import <nixpkgs> {}; {
  chutneyEnv = stdenv.mkDerivation {
    name = "chutney-ui";
    buildInputs = [
      nodejs
      geckodriver
    ];
  };
}
