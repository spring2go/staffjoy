# Manager Microservice

## Configuring for Webpack Dev Server Development

Running the full Staffjoy stack is too demanding for most frontend development flows.
TBD - local commit necessary to configure uat as API endpoint.

## Cutting Build for Production

Prior to commiting a feature, if you used the webpack dev server workflow, you will have to create a new bundle for distribution. The generated bundle exists in `/dist`.

Run `webpack` to generate the new dist files.

Commit the generated files along with your feature.

## Storybook Setup

If @kadira/storybook fails to install locally, try running `npm install` in your vagrant box.

To run Storybook, use this npm script:

`npm run storybook` 
