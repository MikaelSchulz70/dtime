import { toDateString, formatAmount } from '../utils';

describe('utils', () => {
  describe('toDateString', () => {
    it('should convert Date object to ISO date string', () => {
      const date = new Date('2023-01-15T10:30:00Z');
      const result = toDateString(date);
      
      expect(result).toMatch(/^\d{4}-\d{2}-\d{2}$/);
      expect(result).toBe('2023-01-15');
    });

    it('should handle different timezones correctly', () => {
      const date = new Date('2023-12-25T23:00:00Z');
      const result = toDateString(date);
      
      expect(result).toMatch(/^\d{4}-\d{2}-\d{2}$/);
    });

    it('should return empty string for non-Date objects', () => {
      expect(toDateString(null)).toBe('');
      expect(toDateString(undefined)).toBe('');
      expect(toDateString('2023-01-15')).toBe('');
      expect(toDateString(123456789)).toBe('');
      expect(toDateString({})).toBe('');
    });

    it('should handle invalid dates', () => {
      const invalidDate = new Date('invalid');
      
      // Invalid dates should throw error or return empty string
      expect(() => toDateString(invalidDate)).toThrow('Invalid time value');
    });
  });

  describe('formatAmount', () => {
    it('should format positive numbers correctly', () => {
      const result = formatAmount(1234.56);
      expect(result).toContain('1');
      expect(result).toContain('234');
      expect(result).toContain('56');
      expect(formatAmount(1000)).toMatch(/1\s000/);
      expect(formatAmount(999)).toBe('999');
    });

    it('should format negative numbers correctly', () => {
      const result = formatAmount(-1234.56);
      expect(result).toContain('1');
      expect(result).toContain('234');
      expect(result).toContain('56');
      expect(formatAmount(-1000)).toMatch(/1\s000/);
    });

    it('should format zero correctly', () => {
      expect(formatAmount(0)).toBe('0');
      expect(formatAmount(0.0)).toBe('0');
    });

    it('should handle decimal numbers correctly', () => {
      expect(formatAmount(1.5)).toBe('1,5');
      expect(formatAmount(1.0)).toBe('1');
      expect(formatAmount(0.123)).toBe('0,123');
    });

    it('should return empty string for null/undefined', () => {
      expect(formatAmount(null)).toBe('');
      expect(formatAmount(undefined)).toBe('');
    });

    it('should handle large numbers correctly', () => {
      const result = formatAmount(1234567.89);
      expect(result).toContain('1');
      expect(result).toContain('234');
      expect(result).toContain('567');
      expect(result).toContain('89');
      expect(formatAmount(1000000)).toMatch(/1\s000\s000/);
    });

    it('should handle very small numbers correctly', () => {
      expect(formatAmount(0.01)).toBe('0,01');
      expect(formatAmount(0.001)).toBe('0,001');
    });
  });
});