const HtmlWebpackPlugin = require('html-webpack-plugin');

module.exports = (env, argv) => {
  const isProduction = argv.mode === 'production' || process.env.NODE_ENV === 'production';

  return {
    entry: './src/index.js',
    module: {
      rules: [
        {
          test: /\.(js|jsx)$/,
          exclude: /node_modules/,
          use: ['babel-loader'],
        },
        {
          test: /\.css$/,
          use: [{ loader: 'style-loader' }, { loader: 'css-loader' }],
        },
        {
          test: /\.(png|jpe?g|gif|svg)$/i,
          type: 'asset/resource',
          generator: {
            filename: 'assets/[name].[contenthash][ext]',
          },
        },
      ],
    },
    resolve: {
      extensions: ['*', '.js', '.jsx'],
    },
    output: {
      path: __dirname + '/dist',
      publicPath: '/',
      filename: '[name].[contenthash].js',
      clean: true,
    },
    optimization: {
      runtimeChunk: 'single',
      splitChunks: {
        chunks: 'all',
        maxSize: 244000,
        cacheGroups: {
          vendor: {
            test: /[\\/]node_modules[\\/]/,
            name: 'vendors',
            chunks: 'all',
          },
          common: {
            minChunks: 2,
            chunks: 'all',
            enforce: true,
          },
        },
      },
    },
    performance: {
      hints: isProduction ? 'warning' : false,
      maxAssetSize: 1500000,
      maxEntrypointSize: 1500000,
      assetFilter: function (assetFilename) {
        return !assetFilename.includes('logo');
      },
    },
    plugins: [
      new HtmlWebpackPlugin({
        template: './public/index.html',
        filename: 'index.html',
      }),
    ],
    devServer: {
      static: {
        directory: './public',
        publicPath: '/',
      },
      port: Number(process.env.FRONTEND_PORT || 3000),
      hot: true,
      server: 'https',
      historyApiFallback: {
        index: '/index.html',
      },
      proxy: [
        {
          context: ['/api', '/logout', '/perform_login', '/pub', '/oauth2', '/login/oauth2'],
          target: process.env.REACT_APP_BACKEND_URL || 'https://localhost:8443',
          secure: false,
          changeOrigin: true,
          cookieDomainRewrite: 'localhost',
          cookiePathRewrite: '/',
          onProxyReq: (proxyReq, req) => {
            if (req.headers.cookie) {
              proxyReq.setHeader('Cookie', req.headers.cookie);
            }
          },
          onProxyRes: (proxyRes) => {
            if (proxyRes.headers['set-cookie']) {
              proxyRes.headers['set-cookie'] = proxyRes.headers['set-cookie'].map((cookie) => {
                return cookie.replace(/Domain=[^;]+;?\s*/i, '').replace(/Path=[^;]+;?\s*/i, 'Path=/; ');
              });
            }
          },
        },
      ],
    },
  };
};
