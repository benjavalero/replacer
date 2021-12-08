export interface TypeCount {
  t: string; // Type
  l: SubtypeCount[];
}

export interface SubtypeCount {
  s: string; // Subtype
  c: number; // Count
}
