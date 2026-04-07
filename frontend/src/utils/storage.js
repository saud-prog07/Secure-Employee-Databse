/**
 * Utility to manage both localStorage and sessionStorage
 */
export const storage = {
  /**
   * Sets an item in either localStorage or sessionStorage
   */
  set: (key, value, remember = false) => {
    const stringValue = typeof value === 'string' ? value : JSON.stringify(value);
    if (remember) {
      localStorage.setItem(key, stringValue);
    } else {
      sessionStorage.setItem(key, stringValue);
    }
  },

  /**
   * Gets an item from either localStorage or sessionStorage
   */
  get: (key) => {
    return localStorage.getItem(key) || sessionStorage.getItem(key);
  },

  /**
   * Removes an item from both storage locations
   */
  remove: (key) => {
    localStorage.removeItem(key);
    sessionStorage.removeItem(key);
  },

  /**
   * Clears all authentication-related items from both storage locations
   */
  clearAuth: () => {
    const authKeys = ['token', 'roles', 'username', 'pendingUsername'];
    authKeys.forEach(key => {
      localStorage.removeItem(key);
      sessionStorage.removeItem(key);
    });
  }
};
