// for creating cache-safe files
var path = require('path');
var HtmlWebpackPlugin = require('html-webpack-plugin');
var FaviconsWebpackPlugin = require('favicons-webpack-plugin');
var hash = Date.now();
var bundleName = "bundle-" + hash + ".js";

// for cleanup of other builds
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
                    "node_modules/",
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
                loader: "react-hot!babel"
            },
            {
                test: /\.scss$/,
                loaders: ["style", "css", "sass"]
            },
            {
                test: /\.(jpg|png)$/,
                loader: 'file?name=assets/[name].[hash].[ext]'
            },
        ]
    },
    resolve: {
        extensions: ["", ".js", ".jsx"],
        root: [path.resolve(__dirname, 'node_modules')]
    },
    resolveLoader: {
        root: path.join(__dirname, "node_modules")
    },
    output: {
        path: path.join(__dirname, "/dist"),
        publicPath: "/",
        filename: bundleName,
    },
    devServer: {
        contentBase: "./dist",
        hot: true,
        disableHostCheck: true,
    },
    eslint: {
        configFile: "./.eslintrc"
    },
    plugins: [
        new HtmlWebpackPlugin({
            title: "Staffjoy | My Account",
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
