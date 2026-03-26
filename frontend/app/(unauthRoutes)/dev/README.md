# Development Directory

This directory contains components and utilities that are used exclusively for development purposes. These components are not included in production builds and should not be relied upon for production functionality.

## Build Exclusion

This directory is automatically excluded from production builds through the following mechanisms:

1. **Webpack Configuration**: The `next.config.js` file includes a webpack rule that uses `ignore-loader` to exclude files in this directory during production builds.

2. **Page Extensions Filtering**: The Next.js configuration filters out pages with "dev" in their extension in production mode.

To modify this behavior, see the webpack configuration in the root `next.config.js` file.
