/**
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

const plugin = require("tailwindcss/plugin");

const withOpacity = (colorVariableName) => {
  return ({ opacityValue }) => {
    if (opacityValue !== undefined) {
      return `rgba(var(${colorVariableName}), ${opacityValue})`;
    }

    return `rgb(var(${colorVariableName}))`;
  };
};

const createColorShadesMap = (colorName) => {
  const shades = [50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 950];
  const shadesMap = {};
  shades.forEach((shade) => {
    const colorVariableName = `--color-${colorName}-${shade}`;
    shadesMap[shade] = withOpacity(colorVariableName);
  });

  // Set default shade for each color.
  shadesMap["DEFAULT"] = shadesMap[500];

  return shadesMap;
};

const googleSans = "var(--font-google-sans)";
const googleSansText = "var(--font-google-sans-text)";
// const googleSansMono = "var(--font-google-sans-mono)";
const googleSansCode = "var(--font-google-sans-code)";
const googleSansDisplay = "var(--font-google-sans-display)";
const materialSymbols = "var(--font-material-symbols-rounded)";
const materialSymbolsOutlined = "var(--font-material-symbols-outlined)";

/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./app/**/*.{js,ts,jsx,tsx,mdx}",
    "./components/**/*.{js,ts,jsx,tsx,mdx}",
    "./config/**/*.{js,ts,tsx}",
    "./pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./styles/**/*.{css,scss,sass}",
  ],
  corePlugins: { preflight: false },
  darkMode: "class",
  theme: {
    extend: {
      colors: {
        brand: createColorShadesMap("brand"),
        neutrals: createColorShadesMap("neutrals"),
        supporting: {
          green: createColorShadesMap("supporting-green"),
          yellow: createColorShadesMap("supporting-yellow"),
          red: createColorShadesMap("supporting-red"),
        },

        primary: withOpacity("--color-background-primary"),

        accent: {
          green: withOpacity("--color-accent-green"),
          yellow: withOpacity("--color-accent-yellow"),
          red: withOpacity("--color-accent-red"),
        },

        // For text only. Put here so that SVG attributes (stroke, fill, etc) can use this.
        inverted: withOpacity("--color-text-inverted"),
        green: "var(--color-green)",
        lightGreen: "var(--color-light-green)",
        mediumGreen: "var(--color-medium-green)",
        borderColor: "var(--color-border)",
        lightBlue: "var(--color-light-blue)",
        resting: "var(--color-resting)",
        secondary: "var(--color-secondary)",
        secondaryDark: "var(--color-secondary-dark)",
        lightSilver: "var(--color-light-silver)",
        veryLightSilver: "var(--color-very-light-silver)",
        white: "var(--color-white)",
        disabled: "var(--color-disabled)",
        veryLightRed: "var(--color-very-light-red)",
        darkRed: "var(--color-dark-red)",
        darkGreen: "var(--color-dark-green)",
        red: "var(--color-red)",
        orange: "var(--color-orange)",
        yellow: "var(--color-yellow)",
        pink: "var(--color-pink)",
        purple: "var(--color-purple)",
        blue: "var(--color-blue)",
        grey: "var(--color-grey)",
        black: "var(--color-black)",
        darkBlue: "var(--color-dark-blue)",
        lime: "var(--color-lime)",
        greenBg: "var(--color-green-bg)",
        redBg: "var(--color-red-bg)",
        limeBg: "var(--color-lime-bg)",
        orangeBg: "var(--color-orange-bg)",
        blueBg: "var(--color-blue-bg)",
        violetBg: "var(--color-violet-bg)",
        purpleBg: "var(--color-purple-bg)",
        pinkBg: "var(--color-pink-bg)",
        greyBg: "var(--color-grey-bg)",
        violet: "var(--color-violet)",
        secondaryBg: "var(--color-secondary-bg)",
      },
      textColor: {
        primary: withOpacity("--color-text-primary"),
      },
      fontFamily: {
        custom: ["Google Sans"],
      },
      fontSize: {
        12: "12px",
        14: "14px",
        16: "16px",
        20: "20px",
        24: "24px",
        32: "32px",
        40: "40px",
        56: "56px",
      },
      borderRadius: {
        xs: "4px",
        sm: "8px",
        md: "12px",
        lg: "16px",
        xl: "24px",
        100: "100px",
      },
      gap: {
        xs: "2px",
        sm: "4px",
        md: "8px",
        lg: "12px",
        xl: "16px",
        "2xl": "20px",
        "3xl": "24px",
        "4xl": "32px",
      },
    },
  },
  plugins: [
    plugin(({ addUtilities }) => {
      addUtilities({
        ".text-body-10": {
          fontSize: "0.625rem",
          fontWeight: "400",
          lineHeight: "16px",
          letterSpacing: "0.1px",
          fontFamily: googleSansText,
        },
        ".text-body-11": {
          fontSize: "0.7rem",
          fontWeight: "400",
          lineHeight: "16px",
          letterSpacing: "0.1px",
          fontFamily: googleSansText,
        },
        ".text-body-12": {
          fontSize: "0.75rem",
          fontWeight: "400",
          lineHeight: "16px",
          letterSpacing: "0.1px",
          fontFamily: googleSansText,
        },
        ".text-body-14": {
          fontSize: "0.875rem",
          fontWeight: "400",
          lineHeight: "20px",
          letterSpacing: "0",
          fontFamily: googleSansText,
        },
        ".text-body-16": {
          fontSize: "1rem",
          fontWeight: "400",
          lineHeight: "24px",
          letterSpacing: "0",
          fontFamily: googleSansText,
        },
        ".text-body-18": {
          fontSize: "1.125rem",
          fontWeight: "400",
          lineHeight: "24px",
          letterSpacing: "0",
          fontFamily: googleSansText,
        },
        ".text-body-20": {
          fontSize: "1.25rem",
          fontWeight: "400",
          lineHeight: "24px",
          letterSpacing: "0",
          fontFamily: googleSansText,
        },
        ".text-body-22": {
          fontSize: "1.375rem",
          fontWeight: "400",
          lineHeight: "24px",
          letterSpacing: "0",
          fontFamily: googleSansText,
        },
        ".text-body-28": {
          fontSize: "1.75rem",
          fontWeight: "400",
          lineHeight: "24px",
          letterSpacing: "0",
          fontFamily: googleSansText,
        },
        ".text-caps-8": {
          fontSize: "0.4rem",
          fontWeight: "bold",
          lineHeight: "12px",
          letterSpacing: "0",
          textTransform: "uppercase",
        },
        ".text-caps-10": {
          fontSize: "0.5rem",
          fontWeight: "bold",
          lineHeight: "12px",
          letterSpacing: "0",
          textTransform: "uppercase",
        },
        ".text-caps-11": {
          fontSize: "0.625rem",
          fontWeight: "500",
          lineHeight: "13px",
          letterSpacing: "0",
          textTransform: "uppercase",
        },
        ".text-caps-12": {
          fontSize: "0.6rem",
          fontWeight: "bold",
          lineHeight: "12px",
          letterSpacing: "0",
          textTransform: "uppercase",
        },
        ".text-title-11": {
          fontSize: "0.688rem",
          fontWeight: "500",
          lineHeight: "16px",
          letterSpacing: "0.1px",
          fontFamily: googleSansText,
        },
        ".text-title-12": {
          fontSize: "0.75rem",
          fontWeight: "500",
          lineHeight: "16px",
          letterSpacing: "0.1px",
          fontFamily: googleSansText,
        },
        ".text-title-14": {
          fontSize: "0.875rem",
          fontWeight: "500",
          lineHeight: "20px",
          letterSpacing: "0",
          fontFamily: googleSansText,
        },
        ".text-title-16": {
          fontSize: "1rem",
          fontWeight: "500",
          lineHeight: "24px",
          letterSpacing: "0",
          fontFamily: googleSansText,
        },
        ".text-title-18": {
          fontSize: "1.125rem",
          fontWeight: "500",
          lineHeight: "24px",
          letterSpacing: "0",
          fontFamily: googleSansText,
        },
        ".text-title-19": {
          fontSize: "1.125rem",
          fontWeight: "400",
          lineHeight: "28px",
          letterSpacing: "0",
          fontFamily: googleSansText,
        },
        ".text-title-20": {
          fontSize: "1.25rem",
          fontWeight: "600",
          lineHeight: "28px",
          letterSpacing: "0",
        },
        ".text-title-21": {
          fontSize: "1.25rem",
          fontWeight: "500",
          lineHeight: "28px",
          letterSpacing: "0",
        },
        ".text-title-22": {
          fontSize: "1.375rem",
          fontWeight: "400",
          lineHeight: "28px",
          letterSpacing: "0",
        },
        ".text-title-24": {
          fontSize: "1.5rem",
          fontWeight: "400",
          lineHeight: "32px",
          letterSpacing: "0",
        },
        ".text-title-28": {
          fontSize: "1.75rem",
          fontWeight: "400",
          lineHeight: "36px",
          letterSpacing: "0",
        },
        ".text-title-32": {
          fontSize: "2rem",
          fontWeight: "400",
          lineHeight: "40px",
          letterSpacing: "0",
        },
        ".text-title-36": {
          fontSize: "2.25rem",
          fontWeight: "400",
          lineHeight: "44px",
          letterSpacing: "0",
          fontFamily: googleSansText,
        },
        ".text-title-40": {
          fontSize: "2.5rem",
          fontWeight: "400",
          lineHeight: "48px",
          letterSpacing: "0",
        },
        ".text-title-44": {
          fontSize: "2.813rem",
          fontWeight: "400",
          lineHeight: "52px",
          letterSpacing: "0",
        },
        ".text-title-56": {
          fontSize: "3.5rem",
          fontWeight: "400",
          lineHeight: "64px",
          letterSpacing: "0",
        },
        ".text-prompt-body-15": {
          fontSize: "0.938rem",
          fontWeight: "400",
          lineHeight: "22px",
          letterSpacing: "0",
          fontFamily: googleSansText,
        },
        ".text-code-12": {
          fontSize: "0.75rem",
          fontWeight: "400",
          lineHeight: "20px",
          letterSpacing: "0",
          fontFamily: googleSansCode,
        },
        ".text-code-14": {
          fontSize: "0.875rem",
          fontWeight: "400",
          lineHeight: "20px",
          letterSpacing: "0",
          fontFamily: googleSansCode,
        },
        ".text-code-title-14": {
          fontSize: "0.875rem",
          fontWeight: "400",
          lineHeight: "20px",
          letterSpacing: "0",
          fontFamily: googleSansCode,
        },
        ".text-analytics-12": {
          fontSize: "0.75rem",
          fontWeight: "500",
          lineHeight: "24px",
          letterSpacing: "0",
        },
        ".material-symbols": {
          fontFamily: materialSymbols,
          fontWeight: "normal",
          fontStyle: "normal",
          fontSize: "24px",
        },
        ".material-symbols-filled": {
          fontFamily: materialSymbolsOutlined,
          fontWeight: "400",
          fontStyle: "normal",
          fontSize: "24px",
          ["font-variation-settings"]: '"FILL" 1',
        },
        ".text-body-sans-12": {
          fontSize: "0.75rem",
          fontWeight: "400",
          lineHeight: "18px",
          letterSpacing: "0",
          fontFamily: googleSans,
        },
        ".small-btn": {
          minWidth: "120px",
        },
        ".large-btn": {
          minWidth: "160px",
        },
        ".text-logo": {
          fontSize: "24px",
          fontWeight: "500",
          fontWidth: "2%",
          lineHeight: "25.307px",
          letterSpacing: "0.48px",
          fontFamily: googleSansDisplay,
        },
        ".modal-form-two-rows-inputs": {
          gap: "16px",
        },
        ".border-default": {
          borderColor: "var(--color-border)",
          borderWidth: "1px",
          borderStyle: "solid",
        },
        ".border-b-default": {
          borderBottomColor: "var(--color-border)",
          borderBottomWidth: "1px",
          borderBottomStyle: "solid",
        },
        ".border-r-default": {
          borderRightColor: "var(--color-border)",
          borderRightWidth: "1px",
          borderRightStyle: "solid",
        },
        ".border-l-default": {
          borderLeftColor: "var(--color-border)",
          borderLeftWidth: "1px",
          borderLeftStyle: "solid",
        },
        ".border-t-default": {
          borderTopColor: "var(--color-border)",
          borderTopWidth: "1px",
          borderTopStyle: "solid",
        },
      });
    }),
  ],
};
