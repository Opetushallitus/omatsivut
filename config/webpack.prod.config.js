const path = require('path');
const webpack = require('webpack');
const UglifyJSPlugin = require('uglifyjs-webpack-plugin');

module.exports = {
  mode: 'production',
  devtool: 'source-map',
  entry: {
    index: './src/main/js/app.js'
  },
  output: {
    path: path.resolve(__dirname, '../src/main/webapp/'),
    filename: '[name].bundle.js',
    chunkFilename: '[name].bundle.js',
    publicPath: '/omatsivut/',
  },
  optimization: {
    minimize: false,
    splitChunks: {
      cacheGroups: {
        vendor: {
          test: /node_modules/,
          chunks: 'initial',
          name: 'vendor',
          enforce: true
        },
        styles: {
          name: 'styles',
          test: /\.css$/,
          chunks: 'all',
          enforce: true
        }
      }
    }
  },
  module: {
    rules: [
      {
        test: /\.html$/,
        use: {
          loader: "raw-loader"
        }
      },
      {
        test: /\.less$/,
        use: [
          {
            loader: "file-loader",
            options: {
              name: "css/[name].css",
            },
          },
          {
            loader: "extract-loader",
          },
          {
            loader: "css-loader",
            options: {
              sourceMap: true,
              modules: false
            }
          },
          {
            loader: "less-loader"
          }
]      },
      {
        test: /\.css$/,
        use: [
          "css-loader"
        ]
      },
      {
        test: /\.(png|jp(e*)g|svg|gif)$/,
        use: [{
          loader: 'url-loader',
        }]
      }
    ]
  },
  plugins: [
    new webpack.ContextReplacementPlugin(
      /moment[\/\\]locale$/,
      /fi|sv|en-gb/
    ),
    new UglifyJSPlugin({
      sourceMap: true,
      test: 'vendor.bundle.js'
    })
  ]
}
