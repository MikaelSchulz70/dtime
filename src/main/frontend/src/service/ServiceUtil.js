import $ from 'jquery';


export function Headers() {
    const headerContentType = { 'Content-Type': 'application/json' };

    const meta = $("meta[name='_csrf']");
    if (meta == null) {
        return { headers: headerContentType };
    }

    const token = $("meta[name='_csrf']").attr("content");
    if (token == null) {
        return { headers: headerContentType };
    }

    const headers = {
        'Content-Type': 'application/json',
        'X-CSRF-TOKEN': token
    };

    return { headers: headers };
}