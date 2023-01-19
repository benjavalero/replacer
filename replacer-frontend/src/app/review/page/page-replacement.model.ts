import { ReviewReplacement } from '../../api/models/review-replacement';

export function getReplacementEnd(r: ReviewReplacement): number {
  return r.start + r.text.length;
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

  isFixed(): boolean {
    return !!this.newText;
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
