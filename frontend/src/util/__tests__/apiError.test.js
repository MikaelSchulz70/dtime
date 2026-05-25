import { getApiErrorMessage } from '../apiError';

describe('getApiErrorMessage', () => {
  it('maps invalid month backend message', () => {
    const error = {
      response: {
        status: 400,
        data: { message: 'Month is between 1 and 12' },
      },
    };
    expect(getApiErrorMessage(error)).toBe('Month must be between 1 and 12.');
  });

  it('maps long spring conversion errors to friendly text', () => {
    const error = {
      response: {
        status: 400,
        data: {
          message: "Parameter 'month' has invalid type: Failed to convert value of type 'java.lang.String' to required type 'int'",
        },
      },
    };
    expect(getApiErrorMessage(error)).toBe('The date in the URL is not valid. Check year, month, or week.');
  });

  it('truncates very long unknown messages', () => {
    const error = {
      response: {
        status: 400,
        data: { message: 'x'.repeat(300) },
      },
    };
    expect(getApiErrorMessage(error)).toBe('The date in the URL is not valid. Check year, month, or week.');
  });
});
