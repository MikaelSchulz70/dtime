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
    ],
  },
  resolve: {
    extensions: ['*', '.js', '.jsx']
  },
  output: {
    path: __dirname + '/dist',
    publicPath: '/',
    filename: 'bundle.js'
  },
  plugins: [
    new webpack.HotModuleReplacementPlugin(),
    new HtmlWebpackPlugin({
      template: './public/index.html',
      filename: 'index.html'
    })
  ],
  devServer: {
    static: false,
    port: 9000,
    hot: true,
    historyApiFallback: {
      index: '/index.html'
    },
    proxy: [
      {
        context: ['/api', '/login', '/logout', '/perform_login', '/pub'],
        target: 'http://localhost:8080',
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