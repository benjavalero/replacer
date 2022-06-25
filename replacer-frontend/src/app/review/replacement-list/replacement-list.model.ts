export interface KindCount {
  k: number; // Kind code
  l: SubtypeCount[];
}

export interface SubtypeCount {
  s: string; // Subtype
  c: number; // Count
}