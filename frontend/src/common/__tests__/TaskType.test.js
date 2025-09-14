import { TaskType, TaskTypeLabels } from '../TaskType';

describe('TaskType', () => {
  describe('TaskType enum', () => {
    it('should have correct task type values', () => {
      expect(TaskType.NORMAL).toBeDefined();
      expect(typeof TaskType.NORMAL).toBe('string');
    });

    it('should have all required task types', () => {
      expect(TaskType).toHaveProperty('NORMAL');
      // Add other task types as they are defined
    });
  });

  describe('TaskTypeLabels', () => {
    it('should have labels for all task types', () => {
      Object.keys(TaskType).forEach(key => {
        const taskTypeValue = TaskType[key];
        expect(TaskTypeLabels[taskTypeValue]).toBeDefined();
        expect(typeof TaskTypeLabels[taskTypeValue]).toBe('string');
        expect(TaskTypeLabels[taskTypeValue].length).toBeGreaterThan(0);
      });
    });

    it('should have human-readable labels', () => {
      Object.values(TaskTypeLabels).forEach(label => {
        expect(typeof label).toBe('string');
        expect(label.trim()).not.toBe('');
        // Labels should be readable (contain letters)
        expect(label).toMatch(/[a-zA-Z]/);
      });
    });
  });

  describe('TaskType and TaskTypeLabels consistency', () => {
    it('should have matching keys between TaskType and TaskTypeLabels', () => {
      const taskTypeValues = Object.values(TaskType);
      const labelKeys = Object.keys(TaskTypeLabels);
      
      taskTypeValues.forEach(value => {
        expect(labelKeys).toContain(value);
      });
    });
  });
});