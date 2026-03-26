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

import MaterialIcon from "@/components/MaterialIcon";
import {
  Accordion,
  Badge,
  Button,
  Drawer,
  JsonInput,
  Modal,
  PasswordInput,
  Pill,
  ScrollArea,
  Select,
  Tabs,
  TextInput,
  Textarea,
  Tooltip,
  createTheme,
} from "@mantine/core";
import { createElement } from "react";

const generateColorSpectrum = (colorName: string) => {
  const shades = [50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 950];

  return shades.map(
    (shadeNumber) => `rgb(var(--color-${colorName}-${shadeNumber}))`,
  ) as [
    string,
    string,
    string,
    string,
    string,
    string,
    string,
    string,
    string,
    string,
  ];
};

export const theme = createTheme({
  fontFamily: '"Google Sans", sans-serif',
  primaryColor: "brand",
  primaryShade: { light: 5 },
  components: {
    Accordion: Accordion.extend({
      classNames(_, __, ___) {
        return {
          control: "px-0 py-0",
          label: "text-title-14 text-primary py-[8px]",
          panel: "px-0 py-[8px]",
          content: "px-0 py-0",
          item: "py-[8px] min-h-[52px]",
          chevron: "stroke-neutrals-900",
        };
      },
    }),
    Button: Button.extend({
      defaultProps: {
        variant: "primary",
        size: "md",
        loaderProps: { type: "dots" },
      },
      classNames(_, props, __) {
        const size = props.size ?? "md";

        const isLargeSize = size === "lg";
        const isExtraLarge = size === "xl";
        const textVariantClassName =
          isLargeSize || isExtraLarge ? "text-title-14" : "text-title-12";
        const heightClassName =
          isLargeSize || isExtraLarge ? "h-[44px]" : "h-[34px]";
        const widthClassName = isExtraLarge ? "w-[160px]" : null;
        let radiusClassName = isLargeSize ? "rounded-sm" : "rounded-xs";
        let horizontalPaddingClassName = "px-[24px]";
        if (props.leftSection && props.rightSection) {
          horizontalPaddingClassName = "px-[16px]";
        } else if (props.leftSection && props.variant === "default") {
          horizontalPaddingClassName = "pl-[12px] pr-[12px]";
        } else if (props.leftSection) {
          horizontalPaddingClassName = "pl-[16px] pr-[24px]";
        } else if (props.rightSection) {
          horizontalPaddingClassName = "pl-[24px] pr-[16px]";
        }

        const variant = props.variant;
        if (variant === "icon") {
          horizontalPaddingClassName = "px-0";
          radiusClassName = "rounded-lg";
        }

        const defaultClassNames = {
          label: textVariantClassName,
          root: `${isExtraLarge ? widthClassName : null} ${heightClassName} ${radiusClassName} ${horizontalPaddingClassName}`,
          section: "mr-[6px]",
        };

        if (
          variant &&
          [
            "primary",
            "secondary",
            "danger",
            "icon",
            "default",
            "defaultDisabled",
            "outlineLight",
          ].includes(variant)
        ) {
          const isDisabled = !!props.disabled;
          const isFullWidth = !!props.fullWidth;
          let backgroundColorClassName = isDisabled ? "bg-disabled" : null;
          let backgroundHoverColorClassName = isDisabled ? "bg-disabled" : null;
          let borderColorClassName: string | null = null;
          let borderWidthClassName = "border-0";
          let textColorClassName = isDisabled
            ? "text-secondary"
            : "text-inverted";
          let buttonWidthClassName = isFullWidth ? "w-[100%]" : "w-[unset]";

          switch (variant) {
            case "secondary":
              backgroundColorClassName = "bg-white";
              backgroundHoverColorClassName = isDisabled
                ? "bg-white"
                : "bg-primary";
              borderColorClassName = "border-borderColor";
              borderWidthClassName = "border-[1px]";
              textColorClassName = isDisabled
                ? "text-secondary"
                : "text-primary";
              break;
            case "danger":
              backgroundColorClassName = "bg-white";
              backgroundHoverColorClassName = isDisabled
                ? "bg-white"
                : "bg-supporting-red-50";
              borderColorClassName = isDisabled
                ? "border-supporting-red-300"
                : "border-accent-red";
              borderWidthClassName = "border-[1px]";
              textColorClassName = isDisabled
                ? "text-supporting-red-300"
                : "text-accent-red";
              break;
            case "icon":
              backgroundColorClassName = "bg-white";
              backgroundHoverColorClassName = isDisabled
                ? "bg-white"
                : "bg-primary";
              borderColorClassName = "border-borderColor";
              borderWidthClassName = "border-[1px]";
              buttonWidthClassName = "w-[38px]";
              textColorClassName = isDisabled
                ? "text-secondary"
                : "text-primary";
              break;
            case "default":
              backgroundColorClassName = "bg-neutrals-100";
              backgroundHoverColorClassName = "none";
              borderColorClassName = "border-borderColor rounded-sm";
              borderWidthClassName = "border-[1px]";
              horizontalPaddingClassName = "pl-[12px] pr-[12px]";
              textColorClassName = isDisabled
                ? "text-secondary"
                : "text-primary";
              break;
            case "defaultDisabled":
              if (isDisabled) {
                backgroundColorClassName = "!bg-disabled";
                textColorClassName = "text-white";
              }
              break;
            case "outlineLight":
              backgroundColorClassName = "bg-white !rounded-sm";
              backgroundHoverColorClassName = "none";
              borderColorClassName = "border-borderColor";
              borderWidthClassName = "border-[1px]";
              horizontalPaddingClassName = "pl-[12px] pr-[12px]";
              buttonWidthClassName = "!h-[44px]";
              textColorClassName = "text-secondaryDark";
              break;
          }

          return {
            ...defaultClassNames,
            root: `
              ${defaultClassNames.root} ${backgroundColorClassName}
              hover:${backgroundHoverColorClassName} ${borderColorClassName}
              ${borderWidthClassName} ${buttonWidthClassName} ${isExtraLarge ? "min-w-[160px]" : "min-w-[120px]"}
            `,
            inner: textColorClassName,
            loader: textColorClassName,
          };
        }

        return defaultClassNames;
      },
    }),
    Pill: Pill.extend({
      classNames(_, props, __) {
        const variant = props.variant;
        if (
          variant &&
          ["info", "in-progress", "success", "failed"].includes(variant)
        ) {
          const hasRemoveIcon = !!props.withRemoveButton;
          const rightPaddingClassName = hasRemoveIcon ? "pr-[5px]" : "";

          const textColorClassName =
            variant === "info" ? "text-primary" : "text-white";
          let backgroundColorClassName = "bg-primary";
          switch (variant) {
            case "in-progress":
              backgroundColorClassName = "bg-accent-yellow";
              break;
            case "success":
              backgroundColorClassName = "bg-accent-green";
              break;
            case "failed":
              backgroundColorClassName = "bg-accent-red";
              break;
          }

          return {
            root: `${backgroundColorClassName} p-[6px] ${rightPaddingClassName} h-[unset] box-border rounded-[6px] ${variant === "info" ? "border-borderColor border-[1px]" : ""}`,
            label: `text-caps-10 ${textColorClassName}`,
            section: "ms-[2px]",
            remove: "outline-neutrals-600",
          };
        }

        return {};
      },
    }),
    Badge: Badge.extend({
      classNames() {
        return {
          label: "text-title-12",
          root: "py-2.5 px-2",
        };
      },
    }),
    ScrollArea: ScrollArea.extend({
      classNames: { scrollbar: "color-neutral-200" },
    }),
    Drawer: Drawer.extend({
      defaultProps: {
        overlayProps: {
          className: "bg-neutrals-950 !opacity-[80%]",
        },
        closeButtonProps: {
          className: "m-0 text-title-20",
          iconSize: "40px",
        },
      },
      classNames() {
        return {
          content: "rounded-2xl w-[462px]",
          header: "p-[20px] flex-col-reverse items-start gap-xl",
          body: "p-[20px]",
          inner: "p-[10px]",
          title: "text-title-20 gap text-primary",
        };
      },
    }),
    Select: Select.extend({
      defaultProps: {
        rightSection: createElement(MaterialIcon, {
          name: "keyboard_arrow_down",
        }),
        classNames: {
          option:
            "h-[unset] px-[20px] py-[12px] [&>*]:text-title-14 [&>*]:text-primary rounded-sm hover:bg-primary",
          dropdown: "rounded-sm border-default",
        },
      },
      classNames(_, props, __) {
        const isDisabled = !!props.disabled;
        const isError = !!props.error;
        const borderWidthClassName = isDisabled ? "border-0" : "border-[1px]";
        const backgroundColorClassName = isDisabled ? "bg-disabled" : "";
        const borderColorClassName = isError
          ? "border-supporting-red"
          : "border-borderColor focus:border-brand";

        return {
          input: `${borderWidthClassName} ${backgroundColorClassName} ${borderColorClassName}`,
        };
      },
    }),
    Modal: Modal.extend({
      classNames(_, props, __) {
        const defaultContentClassnames = "rounded-lg";
        const defaultOpacityClassnames = "bg-neutrals-950 !opacity-[80%]";
        const defaultTitleClassnames = "text-title-20 text-primary";

         const closeButtonClassnames = "[&>svg]:pointer-events-none"; 

        if (props.variant === "modern") {
          return {
            content: `${defaultContentClassnames} p-[24px]`,
            overlay: defaultOpacityClassnames,
            title: defaultTitleClassnames,
            header: "p-0 min-h-fit",
            body: "p-0",
            close: closeButtonClassnames,
          };
        }

        return {
          content: defaultContentClassnames,
          overlay: defaultOpacityClassnames,
          title: defaultTitleClassnames,
          close: closeButtonClassnames,
        };
      },
    }),
    TextInput: TextInput.extend({
      classNames(_, props, __) {
        const isDisabled = !!props.disabled;
        const isError = !!props.error;
        const borderWidthClassName = isDisabled ? "border-0" : "border-[1px]";
        const backgroundColorClassName = isDisabled ? "bg-disabled" : "";
        const borderColorClassName = isError
          ? "border-supporting-red"
          : "border-borderColor focus:border-brand";
        let horizontalPaddingClassName = "px-[16px]";

        if (props.leftSection && props.rightSection) {
          horizontalPaddingClassName = "px-[32px]";
        } else if (props.leftSection) {
          horizontalPaddingClassName = "pl-[32px] pr-[18px]";
        } else if (props.rightSection) {
          horizontalPaddingClassName = "pl-[18px] pr-[32px]";
        }

        return {
          label: "text-title-14 text-primary mb-[6px]",
          input: `text-title-14 text-primary placeholder:text-title-14 !placeholder-resting ${borderWidthClassName} border-solid
          rounded-sm transition-[border-color] duration-100 ease-[ease] h-[100%] disabled:text-body-16 ${horizontalPaddingClassName} ${borderColorClassName}`,
          wrapper: `${backgroundColorClassName} rounded-sm h-[40px]`,
          error: "text-title-14 text-supporting-red",
          required: "text-title-14 text-supporting-red",
        };
      },
    }),
    PasswordInput: PasswordInput.extend({
      classNames(_, props, __) {
        if (props.variant === "modern") {
          const isDisabled = !!props.disabled;
          const isError = !!props.error;
          const borderWidthClassName = isDisabled ? "border-0" : "border-[1px]";
          const backgroundColorClassName = isDisabled ? "bg-disabled" : "";
          const borderColorClassName = isError
            ? "border-supporting-red"
            : "border-borderColor focus:border-neutrals-900";
          let horizontalPaddingClassName = "px-[18px]";

          if (props.leftSection && props.rightSection) {
            horizontalPaddingClassName = "px-[32px]";
          } else if (props.leftSection) {
            horizontalPaddingClassName = "pl-[32px] pr-[18px]";
          } else if (props.rightSection) {
            horizontalPaddingClassName = "pl-[18px] pr-[32px]";
          }

          return {
            label: "text-title-14 text-primary mb-[6px]",
            input: `rounded-sm border-none h-[44px] ${horizontalPaddingClassName}`,
            innerInput: `text-title-14 text-primary placeholder:text-title-14 placeholder:text-secondary ${borderWidthClassName} border-solid
          rounded-sm transition-[border-color] duration-100 ease-[ease] disabled:text-body-16 h-[100%] ${borderColorClassName} ${horizontalPaddingClassName}`,
            wrapper: `${backgroundColorClassName} rounded-sm`,
            error: "text-title-14 text-supporting-red",
            required: "text-title-14 text-supporting-red",
          };
        }

        return {};
      },
    }),
    Textarea: Textarea.extend({
      classNames(_, props, __) {
        const isDisabled = !!props.disabled;
        const isError = !!props.error;
        const borderWidthClassName = isDisabled ? "border-0" : "border-[1px]";
        const backgroundColorClassName = isDisabled ? "bg-disabled" : "";
        const borderColorClassName = isError
          ? "border-supporting-red"
          : "border-borderColor focus:border-brand";

        return {
          label: "text-title-14 text-primary mb-[6px]",
          input: `text-title-14 text-primary rounded-sm placeholder:text-title-14 placeholder:text-secondary ${borderWidthClassName} border-solid
        transition-[border-color] duration-100 ease-[ease] disabled:text-body-16 px-[18px] ${borderColorClassName} focus:border-brand`,
          wrapper: `${backgroundColorClassName} rounded-sm`,
          error: "text-title-14 text-supporting-red",
          required: "text-title-14 text-supporting-red",
        };
      },
    }),
    JsonInput: JsonInput.extend({
      classNames(_, props, __) {
        const isDisabled = !!props.disabled;
        const isError = !!props.error;
        const borderWidthClassName = isDisabled ? "border-0" : "border-[1px]";
        const backgroundColorClassName = isDisabled ? "bg-disabled" : "";
        const borderColorClassName = isError
          ? "border-supporting-red"
          : "border-borderColor focus:border-neutrals-900";

        return {
          label: "text-title-14 text-primary mb-[6px]",
          input: `text-title-14 text-primary rounded-sm placeholder:text-title-14 placeholder:text-secondary ${borderWidthClassName} border-solid
        transition-[border-color] duration-100 ease-[ease] disabled:text-body-16 px-[12px] py-[10px] ${borderColorClassName}`,
          wrapper: `${backgroundColorClassName} rounded-sm`,
          error: "text-title-14 text-supporting-red",
          required: "text-title-14 text-supporting-red",
        };
      },
    }),
    Tabs: Tabs.extend({
      classNames(_, props, ___) {
        if (props.variant === "modern") {
          return {
            list: "p-[4px] bg-brand-50 border-brand-100 rounded-sm border border-solid gap-lg",
            tabLabel:
              "text-title-14 text-primary group-data-[active=true]:text-neutrals-50",
            tab: `rounded-sm py-[6px] data-[active=true]:bg-brand hover:bg-neutrals-50 group min-w-[93px]`,
          };
        }

        return {};
      },
    }),
    Tooltip: Tooltip.extend({
      classNames(_, __, ___) {
        return {
          tooltip: "bg-secondary text-body-12",
        };
      },
    }),
  },

  colors: {
    brand: generateColorSpectrum("brand"),
    neutrals: generateColorSpectrum("neutrals"),
    supportingGreen: generateColorSpectrum("supporting-green"),
    supportingYellow: generateColorSpectrum("supporting-yellow"),
    supportingRed: generateColorSpectrum("supporting-red"),
  },
});
