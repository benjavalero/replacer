export default class StringUtils {
  static compareString(v1: string, v2: string): number {
    return v1.localeCompare(v2, 'es', { sensitivity: 'base' });
  }

  static removeDiacritics(text: string): string {
    return text
      .trim()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toLowerCase();
  }
}
