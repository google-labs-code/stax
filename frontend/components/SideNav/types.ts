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

export type NavLinkData = {
  readonly link: string;
  readonly key: string;
  readonly label: string;
  readonly icon?: React.ComponentType | string;
  readonly target?: string;
  childLinks?: NavLinkData[];
};

export interface SideNavContentProps {
  readonly protectedRoutes: string[];
}

export interface NavigationLinkData {
  readonly link: string;
  readonly key: string;
  readonly label: string;
  readonly icon?: any;
  childLinks?: NavigationLinkData[];
  readonly isExpanded?: boolean;
}

export interface SidenavLinkProps {
  readonly isDisabled?: boolean;
  readonly listItem: NavLinkData;
  readonly isSidenavOpen: boolean;
  readonly onLinkClick?: () => void;
  readonly isLoading?: boolean;
  readonly className?: string;
  readonly iconClassName?: string;
  hoverTextColor?: string;
}

export type IExpandedMenuItems = {
  [key: string]: boolean;
};

export interface UserButtonProps {
  readonly isSidenavOpen: boolean;
}
