export interface PageReplacement {
  text: string;
  start: number;
  suggestions: Suggestion[];
}

export function getReplacementEnd(r: PageReplacement): number {
  return r.start + r.text.length;
}

export interface Suggestion {
  text: string;
  comment?: string;
}

export class FixedReplacement {
  index: number;
  start: number;
  oldText: string;
  newText: string | null;

  constructor(index: number, start: number, oldText: string, newText: string | null) {
    this.index = index;
    this.start = start;
    this.oldText = oldText;
    this.newText = newText;
  }
}

export class Snippet {
  start: number;
  text: string;

  constructor(start: number, text: string) {
    this.start = start;
    this.text = text;
  }
}
