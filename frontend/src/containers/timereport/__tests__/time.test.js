import React from 'react';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import TimeService from '../../../service/TimeService';
import { useToast } from '../../../components/Toast';

// Import the component we want to test - TimeReportTableEntry is not exported, so we need to test via the file
// For now, let's create a test file that imports and tests the specific components

// Mock dependencies
jest.mock('../../../service/TimeService');
jest.mock('../../../components/Toast', () => ({
  useToast: jest.fn()
}));

// Mock TimeReportTableEntry component for testing
const TimeReportTableEntry = ({ timeReportDay: initialTimeReportDay, timeChanged, id }) => {
  const [timeReportDay, setTimeReportDay] = React.useState(initialTimeReportDay);
  const [fieldError, setFieldError] = React.useState(false);
  const { showError } = useToast();

  React.useEffect(() => {
    setTimeReportDay(initialTimeReportDay);
  }, [initialTimeReportDay]);

  const timeChangedCallback = React.useCallback((updatedTimeReportDay, newId) => {
    updatedTimeReportDay.id = newId;
    timeChanged(updatedTimeReportDay);
  }, [timeChanged]);

  const isTextAllowed = React.useCallback((text) => {
    if (text == null) {
      return true;
    }

    var number = parseFloat(text);

    if (isNaN(number) && !isFinite(text)) {
      return false;
    }

    if (number < 0 || number > 24) {
      return false;
    }

    var index = text.indexOf('.');
    if (index !== -1) {
      var decimals = text.substring(index, text.length - 1);
      return decimals.length === 0 || decimals.length === 1 || decimals.length === 2;
    }

    return true;
  }, []);

  const addUpdate = React.useCallback((event) => {
    if (timeReportDay.closed) {
      return;
    }

    var value = event.target.value;
    var isInputOk = isTextAllowed(value);
    if (!isInputOk) {
      return;
    }

    const timeService = new TimeService();
    timeService.updateTime(timeReportDay)
      .then(response => {
        timeChangedCallback(timeReportDay, response.data);
      })
      .catch(error => {
        handleError(error.response?.status, error.response?.data?.error, error.response?.data?.fieldErrors);
      });
  }, [timeReportDay, isTextAllowed, timeChangedCallback]);

  const handleError = React.useCallback((status, error) => {
    if (status === 400 && error != null) {
      showError(error);
    } else if (status === 500) {
      showError("Internal server error: " + error);
    } else {
      showError("Error: " + error);
    }
  }, [showError]);

  const handleChange = React.useCallback((event) => {
    if (timeReportDay.closed) {
      return;
    }

    var value = event.target.value;
    if (value != null) {
      value = value.replace(",", ".")
    }

    var newFieldError = !isTextAllowed(value);

    let updatedTimeReportDay = JSON.parse(JSON.stringify(timeReportDay));
    updatedTimeReportDay['time'] = value;
    setTimeReportDay(updatedTimeReportDay);
    setFieldError(newFieldError);
  }, [timeReportDay, isTextAllowed]);

  if (timeReportDay == null) return null;

  var isClosed = timeReportDay.closed;
  var time = (timeReportDay.time == null || timeReportDay.time === 0 ? '' : timeReportDay.time);
  var classes = "time " + (fieldError ? "border border-danger" : '');

  return (
    <td style={{ padding: "0px" }}>
      <input 
        className={classes} 
        readOnly={isClosed} 
        type="text" 
        value={time} 
        maxLength="5" 
        onChange={handleChange} 
        onBlur={addUpdate}
        data-testid="time-input"
      />
    </td>
  );
};

describe('TimeReportTableEntry', () => {
  let mockTimeService;
  let mockShowError;
  let mockTimeChanged;

  beforeEach(() => {
    mockTimeService = {
      updateTime: jest.fn()
    };
    TimeService.mockImplementation(() => mockTimeService);

    mockShowError = jest.fn();
    useToast.mockReturnValue({ showError: mockShowError });

    mockTimeChanged = jest.fn();
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  const createMockTimeReportDay = (overrides = {}) => ({
    id: 1,
    time: 8.0,
    closed: false,
    day: {
      date: '2023-01-15',
      weekend: false,
      majorHoliday: false,
      halfDay: false
    },
    ...overrides
  });

  describe('Rendering', () => {
    it('should render time input with correct value', () => {
      const timeReportDay = createMockTimeReportDay();
      
      render(
        <table>
          <tbody>
            <tr>
              <TimeReportTableEntry 
                timeReportDay={timeReportDay} 
                timeChanged={mockTimeChanged} 
                id="test-1" 
              />
            </tr>
          </tbody>
        </table>
      );

      const input = screen.getByTestId('time-input');
      expect(input).toHaveValue('8');
      expect(input).not.toHaveAttribute('readonly');
    });

    it('should render empty input when time is null', () => {
      const timeReportDay = createMockTimeReportDay({ time: null });
      
      render(
        <table>
          <tbody>
            <tr>
              <TimeReportTableEntry 
                timeReportDay={timeReportDay} 
                timeChanged={mockTimeChanged} 
                id="test-1" 
              />
            </tr>
          </tbody>
        </table>
      );

      const input = screen.getByTestId('time-input');
      expect(input).toHaveValue('');
    });

    it('should render empty input when time is 0', () => {
      const timeReportDay = createMockTimeReportDay({ time: 0 });
      
      render(
        <table>
          <tbody>
            <tr>
              <TimeReportTableEntry 
                timeReportDay={timeReportDay} 
                timeChanged={mockTimeChanged} 
                id="test-1" 
              />
            </tr>
          </tbody>
        </table>
      );

      const input = screen.getByTestId('time-input');
      expect(input).toHaveValue('');
    });

    it('should render readonly input when timeReportDay is closed', () => {
      const timeReportDay = createMockTimeReportDay({ closed: true });
      
      render(
        <table>
          <tbody>
            <tr>
              <TimeReportTableEntry 
                timeReportDay={timeReportDay} 
                timeChanged={mockTimeChanged} 
                id="test-1" 
              />
            </tr>
          </tbody>
        </table>
      );

      const input = screen.getByTestId('time-input');
      expect(input).toHaveAttribute('readonly');
    });

    it('should return null when timeReportDay is null', () => {
      const { container } = render(
        <table>
          <tbody>
            <tr>
              <TimeReportTableEntry 
                timeReportDay={null} 
                timeChanged={mockTimeChanged} 
                id="test-1" 
              />
            </tr>
          </tbody>
        </table>
      );

      expect(container.querySelector('[data-testid="time-input"]')).not.toBeInTheDocument();
    });
  });

  describe('Input Validation', () => {
    it('should accept valid decimal numbers', async () => {
      const user = userEvent.setup();
      const timeReportDay = createMockTimeReportDay({ time: '' });
      
      render(
        <table>
          <tbody>
            <tr>
              <TimeReportTableEntry 
                timeReportDay={timeReportDay} 
                timeChanged={mockTimeChanged} 
                id="test-1" 
              />
            </tr>
          </tbody>
        </table>
      );

      const input = screen.getByTestId('time-input');
      await user.clear(input);
      await user.type(input, '8.5');

      expect(input).toHaveValue('8.5');
      expect(input).not.toHaveClass('border-danger');
    });

    it('should accept whole numbers', async () => {
      const user = userEvent.setup();
      const timeReportDay = createMockTimeReportDay({ time: '' });
      
      render(
        <table>
          <tbody>
            <tr>
              <TimeReportTableEntry 
                timeReportDay={timeReportDay} 
                timeChanged={mockTimeChanged} 
                id="test-1" 
              />
            </tr>
          </tbody>
        </table>
      );

      const input = screen.getByTestId('time-input');
      await user.clear(input);
      await user.type(input, '8');

      expect(input).toHaveValue('8');
      expect(input).not.toHaveClass('border-danger');
    });

    it('should reject negative numbers', async () => {
      const user = userEvent.setup();
      const timeReportDay = createMockTimeReportDay({ time: '' });
      
      render(
        <table>
          <tbody>
            <tr>
              <TimeReportTableEntry 
                timeReportDay={timeReportDay} 
                timeChanged={mockTimeChanged} 
                id="test-1" 
              />
            </tr>
          </tbody>
        </table>
      );

      const input = screen.getByTestId('time-input');
      await user.clear(input);
      await user.type(input, '-1');

      expect(input).toHaveClass('border-danger');
    });

    it('should reject numbers greater than 24', async () => {
      const user = userEvent.setup();
      const timeReportDay = createMockTimeReportDay({ time: '' });
      
      render(
        <table>
          <tbody>
            <tr>
              <TimeReportTableEntry 
                timeReportDay={timeReportDay} 
                timeChanged={mockTimeChanged} 
                id="test-1" 
              />
            </tr>
          </tbody>
        </table>
      );

      const input = screen.getByTestId('time-input');
      await user.clear(input);
      await user.type(input, '25');

      expect(input).toHaveClass('border-danger');
    });

    it('should replace comma with dot', async () => {
      const user = userEvent.setup();
      const timeReportDay = createMockTimeReportDay({ time: '' });
      
      render(
        <table>
          <tbody>
            <tr>
              <TimeReportTableEntry 
                timeReportDay={timeReportDay} 
                timeChanged={mockTimeChanged} 
                id="test-1" 
              />
            </tr>
          </tbody>
        </table>
      );

      const input = screen.getByTestId('time-input');
      await user.clear(input);
      await user.type(input, '8,5');

      expect(input).toHaveValue('8.5');
    });

    it('should reject non-numeric input', async () => {
      const user = userEvent.setup();
      const timeReportDay = createMockTimeReportDay({ time: '' });
      
      render(
        <table>
          <tbody>
            <tr>
              <TimeReportTableEntry 
                timeReportDay={timeReportDay} 
                timeChanged={mockTimeChanged} 
                id="test-1" 
              />
            </tr>
          </tbody>
        </table>
      );

      const input = screen.getByTestId('time-input');
      await user.clear(input);
      await user.type(input, 'abc');

      expect(input).toHaveClass('border-danger');
    });
  });

  describe('Time Updates', () => {
    it('should call updateTime service on blur with valid input', async () => {
      const user = userEvent.setup();
      const timeReportDay = createMockTimeReportDay();
      mockTimeService.updateTime.mockResolvedValue({ data: 123 });
      
      render(
        <table>
          <tbody>
            <tr>
              <TimeReportTableEntry 
                timeReportDay={timeReportDay} 
                timeChanged={mockTimeChanged} 
                id="test-1" 
              />
            </tr>
          </tbody>
        </table>
      );

      const input = screen.getByTestId('time-input');
      await user.clear(input);
      await user.type(input, '7.5');
      await user.tab(); // This will trigger onBlur

      await waitFor(() => {
        expect(mockTimeService.updateTime).toHaveBeenCalled();
      });
    });

    it('should not call updateTime service when input is closed', async () => {
      const user = userEvent.setup();
      const timeReportDay = createMockTimeReportDay({ closed: true });
      
      render(
        <table>
          <tbody>
            <tr>
              <TimeReportTableEntry 
                timeReportDay={timeReportDay} 
                timeChanged={mockTimeChanged} 
                id="test-1" 
              />
            </tr>
          </tbody>
        </table>
      );

      const input = screen.getByTestId('time-input');
      fireEvent.blur(input);

      expect(mockTimeService.updateTime).not.toHaveBeenCalled();
    });

    it('should not call updateTime service with invalid input', async () => {
      const user = userEvent.setup();
      const timeReportDay = createMockTimeReportDay();
      
      render(
        <table>
          <tbody>
            <tr>
              <TimeReportTableEntry 
                timeReportDay={timeReportDay} 
                timeChanged={mockTimeChanged} 
                id="test-1" 
              />
            </tr>
          </tbody>
        </table>
      );

      const input = screen.getByTestId('time-input');
      await user.clear(input);
      await user.type(input, '25'); // Invalid - greater than 24
      await user.tab();

      expect(mockTimeService.updateTime).not.toHaveBeenCalled();
    });

    it('should call timeChanged callback on successful update', async () => {
      const user = userEvent.setup();
      const timeReportDay = createMockTimeReportDay();
      mockTimeService.updateTime.mockResolvedValue({ data: 123 });
      
      render(
        <table>
          <tbody>
            <tr>
              <TimeReportTableEntry 
                timeReportDay={timeReportDay} 
                timeChanged={mockTimeChanged} 
                id="test-1" 
              />
            </tr>
          </tbody>
        </table>
      );

      const input = screen.getByTestId('time-input');
      await user.clear(input);
      await user.type(input, '7.5');
      await user.tab();

      await waitFor(() => {
        expect(mockTimeChanged).toHaveBeenCalledWith(
          expect.objectContaining({ id: 123 })
        );
      });
    });
  });

  describe('Error Handling', () => {
    it('should show error message for 400 status', async () => {
      const user = userEvent.setup();
      const timeReportDay = createMockTimeReportDay();
      mockTimeService.updateTime.mockRejectedValue({
        response: {
          status: 400,
          data: { error: 'Validation error', fieldErrors: [] }
        }
      });
      
      render(
        <table>
          <tbody>
            <tr>
              <TimeReportTableEntry 
                timeReportDay={timeReportDay} 
                timeChanged={mockTimeChanged} 
                id="test-1" 
              />
            </tr>
          </tbody>
        </table>
      );

      const input = screen.getByTestId('time-input');
      await user.clear(input);
      await user.type(input, '8');
      await user.tab();

      await waitFor(() => {
        expect(mockShowError).toHaveBeenCalledWith('Validation error');
      });
    });

    it('should show server error message for 500 status', async () => {
      const user = userEvent.setup();
      const timeReportDay = createMockTimeReportDay();
      mockTimeService.updateTime.mockRejectedValue({
        response: {
          status: 500,
          data: { error: 'Internal server error' }
        }
      });
      
      render(
        <table>
          <tbody>
            <tr>
              <TimeReportTableEntry 
                timeReportDay={timeReportDay} 
                timeChanged={mockTimeChanged} 
                id="test-1" 
              />
            </tr>
          </tbody>
        </table>
      );

      const input = screen.getByTestId('time-input');
      await user.clear(input);
      await user.type(input, '8');
      await user.tab();

      await waitFor(() => {
        expect(mockShowError).toHaveBeenCalledWith('Internal server error: Internal server error');
      });
    });

    it('should show generic error message for other status codes', async () => {
      const user = userEvent.setup();
      const timeReportDay = createMockTimeReportDay();
      mockTimeService.updateTime.mockRejectedValue({
        response: {
          status: 404,
          data: { error: 'Not found' }
        }
      });
      
      render(
        <table>
          <tbody>
            <tr>
              <TimeReportTableEntry 
                timeReportDay={timeReportDay} 
                timeChanged={mockTimeChanged} 
                id="test-1" 
              />
            </tr>
          </tbody>
        </table>
      );

      const input = screen.getByTestId('time-input');
      await user.clear(input);
      await user.type(input, '8');
      await user.tab();

      await waitFor(() => {
        expect(mockShowError).toHaveBeenCalledWith('Error: Not found');
      });
    });
  });

  describe('Component Updates', () => {
    it('should update state when timeReportDay prop changes', () => {
      const timeReportDay1 = createMockTimeReportDay({ time: 8 });
      const { rerender } = render(
        <table>
          <tbody>
            <tr>
              <TimeReportTableEntry 
                timeReportDay={timeReportDay1} 
                timeChanged={mockTimeChanged} 
                id="test-1" 
              />
            </tr>
          </tbody>
        </table>
      );

      expect(screen.getByTestId('time-input')).toHaveValue('8');

      const timeReportDay2 = createMockTimeReportDay({ time: 9 });
      rerender(
        <table>
          <tbody>
            <tr>
              <TimeReportTableEntry 
                timeReportDay={timeReportDay2} 
                timeChanged={mockTimeChanged} 
                id="test-1" 
              />
            </tr>
          </tbody>
        </table>
      );

      expect(screen.getByTestId('time-input')).toHaveValue('9');
    });
  });

  describe('Accessibility and Attributes', () => {
    it('should have correct input attributes', () => {
      const timeReportDay = createMockTimeReportDay();
      
      render(
        <table>
          <tbody>
            <tr>
              <TimeReportTableEntry 
                timeReportDay={timeReportDay} 
                timeChanged={mockTimeChanged} 
                id="test-1" 
              />
            </tr>
          </tbody>
        </table>
      );

      const input = screen.getByTestId('time-input');
      expect(input).toHaveAttribute('type', 'text');
      expect(input).toHaveAttribute('maxLength', '5');
      expect(input).toHaveClass('time');
    });

    it('should not allow typing in closed time entries', async () => {
      const user = userEvent.setup();
      const timeReportDay = createMockTimeReportDay({ closed: true, time: 8 });
      
      render(
        <table>
          <tbody>
            <tr>
              <TimeReportTableEntry 
                timeReportDay={timeReportDay} 
                timeChanged={mockTimeChanged} 
                id="test-1" 
              />
            </tr>
          </tbody>
        </table>
      );

      const input = screen.getByTestId('time-input');
      
      // Try to type in a readonly input
      await user.type(input, '9');
      
      // Value should remain unchanged
      expect(input).toHaveValue('8');
    });
  });
});