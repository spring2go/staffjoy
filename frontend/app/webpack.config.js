// for creating cache-safe files
var path = require('path');
var HtmlWebpackPlugin = require('html-webpack-plugin');
var FaviconsWebpackPlugin = require('favicons-webpack-plugin');
var hash = Date.now();
var bundleName = "bundle-" + hash + ".js";

// for cleaning up old files upon building
var WebpackCleanupPlugin = require('webpack-cleanup-plugin');


module.exports = {
    entry: [
        "./src/index.js"
    ],
    module: {
        preLoaders: [
            {
                test: /\.js$/,
                include: /src/,
                exclude: [
                    "/node_modules/",
                    "../third_party/node/"
                ],
                loaders: ['eslint-loader']
            }
        ],
        loaders: [
            {
                test: /\.jsx?/,
                exclude: /node_modules/,
                include: path.join(__dirname, 'src'),
                loader: "babel"
            },
            {
                test: /\.scss$/,
                loaders: ["style", "css", "sass"]
            },
            {
                test: /\.(jpg|png|svg)$/,
                loader: 'file?name=assets/[name].[hash].[ext]'
            },
            {
                test: /\.json$/,
                loader: "json-loader"
            }
        ]
    },
    resolve: {
        extensions: ["", ".js", ".jsx"],
        root: [
          path.resolve(__dirname, './node_modules'),
          path.resolve(__dirname, './src'),
        ],
    },
    resolveLoader: {
        root: path.join(__dirname + "node_modules")
    },
    output: {
        path: __dirname + "/dist",
        publicPath: "/",
        filename: bundleName,
    },
    devServer: {
        contentBase: "./dist",
        hot: true,
        historyApiFallback: true,
        disableHostCheck: true,
    },
    eslint: {
        configFile: "./.eslintrc"
    },
    plugins: [
        new HtmlWebpackPlugin({
            template: "index.template.ejs",
            inject: "body",
        }),
        new FaviconsWebpackPlugin({
            logo: './staffjoy-favicon.png',
            prefix: 'assets/icons/',
            emitStats: false,
            // Inject the html into the html-webpack-plugin
            inject: true,
            // favicon background color
            background: '#fff',
            // favicon app title
            title: 'Staffjoy | App',

            // which icons should be generated
            icons: {
              android: true,
              appleIcon: true,
              appleStartup: true,
              coast: false,
              favicons: true,
              firefox: true,
              opengraph: true,
              twitter: true,
              yandex: false,
              windows: true
            }
        }),
        new WebpackCleanupPlugin({
            exclude: ["README.md", "assets/**/*"],
        })
    ]
};
