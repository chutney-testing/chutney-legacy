export function isNullOrEmptyString(text: string) {
    return text == null || text.length === 0;
}

export function isNullOrBlankString(text: string) {
    return text == null || text.trim().length === 0;
}

export function escapeHtml(text: string): string {
  return text
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}
