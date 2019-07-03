# myaccount
front end app for my accounts

### Getting Started

```npm install```

Installs all devDependencies and dependencies. We may add a flag for prod only dependencies in the future

### Development

```npm start```

Starts a webpack-dev-server to locally run the application.

### Deployment

```npm run build```

Compiles and builds a `bundle-<timestamp>.js` and a `index.html` file inside `dist/`. These are teh only files we need to host. In future we may add fonts to be self-hosted, which will then be in `dist/public/`.
