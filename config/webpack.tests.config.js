const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');

module.exports = {
  mode: 'development',
  entry: ['./src/test/mocha/all.js'],
  output: {
    path: path.resolve(__dirname, '../src/main/resources/webapp/test'),
    filename: 'test.bundle.js',
    publicPath: '/omatsivut/test/'
  },
  module: {
    rules: [
      {
        test: /\.js$/,
        loaders: ['babel-loader']
      },
      {
        test: /(\.css|\.less)$/,
        loader: 'null-loader',
        exclude: [
          /build/
        ]
      },
      {
        test: /(\.jpg|\.jpeg|\.png|\.gif)$/,
        loader: 'null-loader'
      }
    ]
  },
  // Suppress fatal error: Cannot resolve module 'fs'
  // @relative https://github.com/pugjs/pug-loader/issues/8
  // @see https://github.com/webpack/docs/wiki/Configuration#node
  node: {
    fs: 'empty',
  },
  plugins: [
    new HtmlWebpackPlugin({
      cache: true,
      filename: path.resolve(__dirname, 'src/main/webapp/test') + "runner.html",
      showErrors: true,
      template: "./src/test/index.html",
      title: "Mocha Browser Tests",
    }),
  ]
};
