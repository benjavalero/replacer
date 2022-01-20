export class ReplacementKind {
  code: number;
  label: string;
  description: string;

  constructor(code: number, label: string, description: string) {
    this.code = code;
    this.label = label;
    this.description = description;
  }
}

export const CUSTOM = new ReplacementKind(1, 'Personalizado', 'Personalizado');
export const SIMPLE = new ReplacementKind(2, 'Ortografía', 'Palabras con ortografía potencialmente incorrecta');
export const COMPOSED = new ReplacementKind(3, 'Compuestos', 'Expresiones con más de una palabra con ortografía potencialmente incorrecta');
const DATES = new ReplacementKind(4, 'Fechas', 'Fechas con formato incorrecto');

export const REPLACEMENT_KINDS = new Map<number, ReplacementKind>();
REPLACEMENT_KINDS.set(1, CUSTOM);
REPLACEMENT_KINDS.set(2, SIMPLE);
REPLACEMENT_KINDS.set(3, COMPOSED);
REPLACEMENT_KINDS.set(4, DATES);
