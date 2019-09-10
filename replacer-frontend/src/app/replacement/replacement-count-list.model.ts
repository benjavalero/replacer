export interface ReplacementCountList {
  t: string; // Type
  l: ReplacementCountItem[];
}

interface ReplacementCountItem {
  s: string; // Subtype
  c: number; // Count
}
