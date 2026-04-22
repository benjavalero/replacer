import { ParamMap } from '@angular/router';
import { ReviewOptions } from './review-options.model';

export function buildReviewOptionsFromParamMap(paramMap: ParamMap): ReviewOptions {
  const kindParam = paramMap.get('kind');
  const subtypeParam = paramMap.get('subtype');
  const suggestionParam = paramMap.get('suggestion');
  const csParam = paramMap.get('cs');

  const options = {} as ReviewOptions;
  if (subtypeParam !== null) {
    options.subtype = subtypeParam;
    if (suggestionParam !== null) {
      options.kind = 1;
      options.suggestion = suggestionParam;
      options.cs = csParam === 'true';
    } else if (kindParam !== null) {
      options.kind = +kindParam;
    }
  }

  return options;
}

export function buildCustomReviewOptionsFromParamMap(paramMap: ParamMap): ReviewOptions {
  const subtypeParam = paramMap.get('subtype') ?? '';
  const suggestionParam = paramMap.get('suggestion') ?? '';
  const csParam = paramMap.get('cs');

  return {
    kind: 1,
    subtype: subtypeParam,
    suggestion: suggestionParam,
    cs: csParam === 'true'
  } as ReviewOptions;
}

export function getPageIdFromParamMap(paramMap: ParamMap): number | null {
  const idParam = paramMap.get('id');
  if (idParam === null) {
    return null;
  }
  return +idParam;
}
