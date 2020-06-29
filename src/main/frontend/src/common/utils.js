export function toDateString(date) {
    var dateStr = '';
    if (date instanceof Date) {
        dateStr = new Date(date.getTime() - (date.getTimezoneOffset() * 60000))
            .toISOString()
            .split("T")[0];
    }

    return dateStr;
}

export function formatAmount(amount) {
    if (amount == null) {
        return '';
    }

    return amount.toLocaleString('sv-Se', { minimumFractionDigits: 0 });
}