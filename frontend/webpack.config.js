const webpack = require('webpack');
const HtmlWebpackPlugin = require('html-webpack-plugin');

module.exports = {
  entry: './src/index.js',
  module: {
    rules: [
      {
        test: /\.(js|jsx)$/,
        exclude: /node_modules/,
        use: ['babel-loader']
      },
      {
        test: /\.css$/,
        use: [{ loader: 'style-loader' }, { loader: 'css-loader' }],
      },
      {
        test: /\.(png|jpe?g|gif|svg)$/i,
        type: 'asset/resource',
        generator: {
          filename: 'assets/[name].[contenthash][ext]'
        }
      },
    ],
  },
  resolve: {
    extensions: ['*', '.js', '.jsx']
  },
  output: {
    path: __dirname + '/dist',
    publicPath: '/',
    filename: '[name].[contenthash].js',
    clean: true
  },
  optimization: {
    splitChunks: {
      chunks: 'all',
      cacheGroups: {
        vendor: {
          test: /[\\/]node_modules[\\/]/,
          name: 'vendors',
          chunks: 'all',
        },
        common: {
          minChunks: 2,
          chunks: 'all',
          enforce: true
        }
      }
    }
  },
  performance: {
    hints: 'warning',
    maxAssetSize: 1500000, // 1.5MB - accommodate large logo files
    maxEntrypointSize: 1500000, // 1.5MB
    assetFilter: function(assetFilename) {
      // Don't warn about large logo files
      return !assetFilename.includes('logo');
    }
  },
  plugins: [
    new webpack.HotModuleReplacementPlugin(),
    new HtmlWebpackPlugin({
      template: './public/index.html',
      filename: 'index.html'
    })
  ],
  devServer: {
    static: {
      directory: './public',
      publicPath: '/'
    },
    port: 9000,
    hot: true,
    server: 'https',
    historyApiFallback: {
      index: '/index.html'
    },
    proxy: [
      {
        context: ['/api', '/logout', '/perform_login', '/pub'],
        target: process.env.REACT_APP_BACKEND_URL || 'https://localhost:8443',
        secure: false,
        changeOrigin: true,
        cookieDomainRewrite: 'localhost',
        cookiePathRewrite: '/',
        onProxyReq: (proxyReq, req, res) => {
          // Ensure cookies are properly forwarded
          if (req.headers.cookie) {
            proxyReq.setHeader('Cookie', req.headers.cookie);
          }
        },
        onProxyRes: (proxyRes, req, res) => {
          // Ensure Set-Cookie headers are properly handled
          if (proxyRes.headers['set-cookie']) {
            proxyRes.headers['set-cookie'] = proxyRes.headers['set-cookie'].map(cookie => {
              return cookie.replace(/Domain=[^;]+;?\s*/i, '').replace(/Path=[^;]+;?\s*/i, 'Path=/; ');
            });
          }
        }
      }
    ]
  }
};