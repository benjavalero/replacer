export default class StringUtils {
  /* Strings that do not have the same base letters are considered unequal */
  static compareStringBase(v1: string, v2: string): number {
    return v1.localeCompare(v2, 'es', { sensitivity: 'base' });
  }

  /* Strings that do not have the same base letters or accents are considered unequal */
  static compareStringAccent(v1: string, v2: string): number {
    return v1.localeCompare(v2, 'es', { sensitivity: 'accent' });
  }

  static removeDiacritics(text: string): string {
    return text
      .trim()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toLowerCase();
  }
}
