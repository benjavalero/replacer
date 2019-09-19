export interface ReplacementCountList {
  t: string; // Type
  l: ReplacementCount[];
}

export interface ReplacementCount {
  s: string; // Subtype
  c: number; // Count
}
