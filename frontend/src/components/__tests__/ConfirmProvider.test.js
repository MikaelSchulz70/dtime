import React from 'react';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { ConfirmProvider, useConfirm } from '../ConfirmProvider';

jest.mock('react-i18next', () => {
  const en = require('../../locales/en.json');
  return {
    useTranslation: () => ({
      t: (key) => {
        const value = key.split('.').reduce((obj, part) => obj?.[part], en);
        return typeof value === 'string' ? value : key;
      },
    }),
  };
});

function ConfirmHarness({ onResult }) {
  const confirm = useConfirm();

  return (
    <button
      type="button"
      onClick={async () => {
        const ok = await confirm({
          message: 'Delete this item?',
          confirmLabel: 'Delete',
          variant: 'danger',
        });
        onResult(ok);
      }}
    >
      Open confirm
    </button>
  );
}

describe('ConfirmProvider', () => {
  it('resolves true when confirm is clicked', async () => {
    const user = userEvent.setup();
    const onResult = jest.fn();

    render(
      <ConfirmProvider>
        <ConfirmHarness onResult={onResult} />
      </ConfirmProvider>
    );

    await user.click(screen.getByRole('button', { name: 'Open confirm' }));
    expect(screen.getByText('Delete this item?')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Delete' }));

    expect(onResult).toHaveBeenCalledWith(true);
  });

  it('resolves false when cancel is clicked', async () => {
    const user = userEvent.setup();
    const onResult = jest.fn();

    render(
      <ConfirmProvider>
        <ConfirmHarness onResult={onResult} />
      </ConfirmProvider>
    );

    await user.click(screen.getByRole('button', { name: 'Open confirm' }));
    await user.click(screen.getByRole('button', { name: 'Cancel' }));

    expect(onResult).toHaveBeenCalledWith(false);
  });
});
