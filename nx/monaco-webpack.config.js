const MonacoWebpackPlugin = require('monaco-editor-webpack-plugin');

module.exports = {
  // target should only be specified when including component in Electron app
  module: {
    rules: [
      {
        test: /\.css$/,
        use: ['style-loader'],
      },
      {
        test: /\.ttf$/,
        use: ['file-loader'],
      },
    ],
  },
  plugins: [
    new MonacoWebpackPlugin({
      languages: ['json', 'css', 'html', 'javascript', 'typescript', 'yaml'],
      features: ['contextmenu', 'clipboard', 'find'],
    }),
  ],
};
