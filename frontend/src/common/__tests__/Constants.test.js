import * as Constants from '../Constants';

describe('Constants', () => {
  describe('Status Constants', () => {
    it('should have correct status values', () => {
      expect(Constants.ACTIVE_STATUS).toBe('ACTIVE');
      expect(Constants.INACTIVE_STATUS).toBe('INACTIVE');
    });
  });

  describe('User Role Constants', () => {
    it('should have correct user role values', () => {
      expect(Constants.USER_ROLE).toBe('USER');
      expect(Constants.ADMIN_ROLE).toBe('ADMIN');
    });
  });

  describe('View Constants', () => {
    it('should have correct view values', () => {
      expect(Constants.WEEK_VIEW).toBe('WEEK');
      expect(Constants.MONTH_VIEW).toBe('MONTH');
      expect(Constants.YEAR_VIEW).toBe('YEAR');
    });
  });

  describe('Report Type Constants', () => {
    it('should have correct report type values', () => {
      expect(Constants.USER_REPORT).toBe('USER');
      expect(Constants.USER_TASK_REPORT).toBe('USER_TASK');
      expect(Constants.TASK_REPORT).toBe('TASK');
      expect(Constants.ACCOUNT_REPORT).toBe('ACCOUNT');
    });
  });

  describe('Color Constants', () => {
    it('should have correct color values', () => {
      expect(Constants.DAY_COLOR).toBe('#ffffff');
      expect(Constants.WEEKEND_COLOR).toBe('#f8f9fa');
      expect(Constants.ALERT_COLOR).toBe('#28a745');
      expect(Constants.BRAND_PRIMARY).toBe('#28a745');
      expect(Constants.BRAND_SECONDARY).toBe('#6c757d');
    });

    it('should have all required color constants defined', () => {
      const requiredColors = [
        'DAY_COLOR',
        'WEEKEND_COLOR',
        'MAJOR_HOLIDAY_COLOR',
        'HALF_DAY_COLOR',
        'CLOSED_COLOR',
        'ALERT_COLOR',
        'BRAND_PRIMARY',
        'BRAND_SECONDARY',
        'BRAND_SUCCESS',
        'BRAND_INFO',
        'BRAND_WARNING',
        'BRAND_DANGER',
        'BRAND_LIGHT',
        'BRAND_DARK'
      ];

      requiredColors.forEach(colorName => {
        expect(Constants[colorName]).toBeDefined();
        expect(typeof Constants[colorName]).toBe('string');
        expect(Constants[colorName]).toMatch(/^#[0-9a-fA-F]{6}$/);
      });
    });
  });

  describe('Text and Background Colors', () => {
    it('should have correct text color values', () => {
      expect(Constants.TEXT_PRIMARY).toBe('#212529');
      expect(Constants.TEXT_SECONDARY).toBe('#6c757d');
      expect(Constants.TEXT_WHITE).toBe('#ffffff');
    });

    it('should have correct background color values', () => {
      expect(Constants.BG_PRIMARY).toBe('#ffffff');
      expect(Constants.BG_SECONDARY).toBe('#f8f9fa');
      expect(Constants.BG_ACCENT).toBe('#e8f5e8');
    });
  });
});