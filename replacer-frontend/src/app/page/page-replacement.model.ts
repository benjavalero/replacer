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

export interface FixedReplacement {
  index: number;
  start: number;
  oldText: string;
  newText: string;
}

export interface Snippet {
  start: number;
  text: string;
}
