export type SortDirection = 'asc' | 'desc' | '';

export const rotate: { [key: string]: SortDirection } = {
  '': 'asc',
  asc: 'desc',
  desc: 'asc'
};
