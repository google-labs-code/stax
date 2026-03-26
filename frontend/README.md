# Stax.AI

A modern web application built with [Next.js](https://nextjs.org/) and [React](https://react.dev/), featuring Mantine UI, Tailwind CSS, TypeScript, and a robust testing and linting setup.

---

## Table of Contents

- [Getting Started](#getting-started)
- [Available Scripts](#available-scripts)
- [Tech Stack](#tech-stack)
- [Code Quality](#code-quality)
- [Testing](#testing)
- [Formatting](#formatting)
- [Git Hooks](#git-hooks)
- [License](#license)

---

## Getting Started

1. **Install dependencies:**

   ```bash
   npm install
   ```

2. **Configure env variables:**

   Copy .env.template to .env.local in the root folder and change the configurations inside.

3. **Setup the login**

- If you want to disable the authentication, you can do it from /config/config.tsx
- If you want to use the authentication you need to add the google client id to .env.local
  `NEXT_PUBLIC_GOOGLE_CLIENT_ID=yourGoogleClientId`

4. **Run the development server:**

   ```bash
   npm run dev
   ```

   Open [http://localhost:3000](http://localhost:3000) to view the app.

(Optional) 5. **Build and run as production env:**

```bash
 npm run build
 npm run start
```

Open [http://localhost:3000](http://localhost:3000) to view the app.

---

## Available Scripts

| Script                   | Description                          |
| ------------------------ | ------------------------------------ |
| `npm run dev`            | Start the Next.js development server |
| `npm run build`          | Build the app for production         |
| `npm run start`          | Start the production server          |
| `npm run clean`          | Remove `node_modules`                |
| `npm run format`         | Format code with Prettier            |
| `npm run prettier:check` | Check code formatting                |
| `npm run test`           | Run tests with Jest                  |
| `npm run test:coverage`  | Run tests with coverage report       |
| `npm run test:watch`     | Run tests in watch mode              |
| `npm run lint:check`     | Run ESLint and fail on warnings      |
| `npm run lint:fix`       | Fix lint errors automatically        |
| `npm run githooks:init`  | Set up pre-commit git hook           |

---

## Tech Stack

- **Framework:** [Next.js](https://nextjs.org/), [React](https://react.dev/)
- **UI:** [Mantine](https://mantine.dev/), [Tailwind CSS](https://tailwindcss.com/)
- **State/Data:** [@tanstack/react-query](https://tanstack.com/query/latest), [axios](https://axios-http.com/)
- **Forms:** [@mantine/form](https://mantine.dev/form/)
- **Charts & Tables:** [@mantine/charts](https://mantine.dev/charts/), [mantine-react-table](https://www.mantine-react-table.com/)
- **Code Editor:** [@uiw/react-codemirror](https://uiwjs.github.io/react-codemirror/)
- **Internationalization:** [i18next](https://www.i18next.com/), [react-i18next](https://react.i18next.com/)
- **PDF/CSV:** [jspdf](https://github.com/parallax/jsPDF), [papaparse](https://www.papaparse.com/)
- **Date/Time:** [dayjs](https://day.js.org/), [@mantine/dates](https://mantine.dev/dates/)
- **Icons:** [react-icons](https://react-icons.github.io/react-icons/)
- **Markdown:** [react-markdown](https://github.com/remarkjs/react-markdown)
- **Syntax Highlighting:** [react-syntax-highlighter](https://github.com/react-syntax-highlighter/react-syntax-highlighter)
- **OAuth:** [@react-oauth/google](https://www.npmjs.com/package/@react-oauth/google)
- **Utilities:** [lodash](https://lodash.com/), [qs](https://github.com/ljharb/qs), [html2canvas](https://html2canvas.hertzen.com/)

---

## Code Quality

- **Linting:** [ESLint](https://eslint.org/) with [@typescript-eslint](https://typescript-eslint.io/), [eslint-plugin-react](https://github.com/jsx-eslint/eslint-plugin-react), and [eslint-plugin-unused-imports](https://github.com/sweepline/eslint-plugin-unused-imports)
- **Formatting:** [Prettier](https://prettier.io/) with [@trivago/prettier-plugin-sort-imports](https://github.com/trivago/prettier-plugin-sort-imports)
- **Type Checking:** [TypeScript](https://www.typescriptlang.org/)

---

## Testing

- **Test Runner:** [Jest](https://jestjs.io/)
- **Testing Utilities:** [@testing-library/react](https://testing-library.com/docs/react-testing-library/intro/), [@testing-library/jest-dom](https://github.com/testing-library/jest-dom), [@testing-library/user-event](https://testing-library.com/docs/user-event/intro/)
- **Mocking:** [jest-fetch-mock](https://www.npmjs.com/package/jest-fetch-mock)
- **Coverage:** Run `npm run test:coverage` for a coverage report.

---

## Formatting

- **Format code:**
  ```bash
  npm run format
  ```
- **Check formatting:**
  ```bash
  npm run prettier:check
  ```

---

## Git Hooks

- **Pre-commit hook:**  
  Set up with:
  ```bash
  npm run githooks:init
  ```
  This enforces code quality before commits.

---
