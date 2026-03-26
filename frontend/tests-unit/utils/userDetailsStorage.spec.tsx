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

import LocalStorage from '@/utils/LocalStorage';
import { User } from '@/types';
import { UserDetails } from '@/utils/userDetailsStorage';

jest.mock('@/utils/LocalStorage', () => ({
  get: jest.fn(),
  set: jest.fn(),
  remove: jest.fn()
}));

describe('UserDetails', () => {
  const mockUser: User = {
    firstName: 'Test',
    lastName: 'User',
    email: 'test@example.com'
  };
  
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('get()', () => {
    it('returns null when no user details exist', () => {
      (LocalStorage.get as jest.Mock).mockReturnValue(null);
      
      const result = UserDetails.get();
      
      expect(result).toBeNull();
      expect(LocalStorage.get).toHaveBeenCalledWith('userDetails');
    });

    it('returns parsed User object when details exist', () => {
      (LocalStorage.get as jest.Mock).mockReturnValue(JSON.stringify(mockUser));
      
      const result = UserDetails.get();
      
      expect(result).toEqual(mockUser);
      expect(LocalStorage.get).toHaveBeenCalledWith('userDetails');
    });

    it('handles JSON parse errors gracefully', () => {
      (LocalStorage.get as jest.Mock).mockReturnValue('invalid-json');
      
      expect(() => {
        UserDetails.get();
      }).toThrow();
    });
  });

  describe('set()', () => {
    it('stores user details in localStorage', () => {
      UserDetails.set(mockUser);
      
      expect(LocalStorage.set).toHaveBeenCalledWith(
        'userDetails',
        JSON.stringify(mockUser)
      );
    });
  });

  describe('remove()', () => {
    it('removes user details from localStorage', () => {
      UserDetails.remove();
      
      expect(LocalStorage.remove).toHaveBeenCalledWith('userDetails');
    });
  });

  describe('integration tests', () => {
    it('set() followed by get() returns the same user details', () => {
      (LocalStorage.get as jest.Mock).mockImplementation((key) => {
        if (key === 'userDetails') return JSON.stringify(mockUser);
        
        return null;
      });
      
      UserDetails.set(mockUser);
      const result = UserDetails.get();
      
      expect(result).toEqual(mockUser);
      expect(result?.firstName).toBe('Test');
      expect(result?.lastName).toBe('User');
      expect(result?.email).toBe('test@example.com');
    });

    it('remove() followed by get() returns null', () => {
      (LocalStorage.get as jest.Mock).mockReturnValue(null);
      
      UserDetails.remove();
      const result = UserDetails.get();
      
      expect(result).toBeNull();
    });
  });

  describe('STORAGE_KEY', () => {
    it('has the correct storage key value', () => {
      expect(UserDetails.STORAGE_KEY).toBe('userDetails');
    });
  });
});