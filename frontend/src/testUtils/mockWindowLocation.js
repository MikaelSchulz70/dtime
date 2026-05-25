/**
 * Reset and stub window.location for tests (@happy-dom/jest-environment).
 * @param {{ href?: string }} [options]
 * @returns {{ reload: jest.Mock, getHref: () => string }}
 */
export function installMockWindowLocation(options = {}) {
  window.location.href = options.href ?? 'http://localhost/';
  const reload = jest.fn();
  window.location.reload = reload;

  return {
    reload,
    getHref: () => window.location.href,
    restore: () => {
      window.location.href = 'http://localhost/';
    },
  };
}
