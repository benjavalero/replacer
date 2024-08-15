package es.bvalero.replacer.page.save;

import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.page.find.WikipediaTimestamp;
import es.bvalero.replacer.user.AccessToken;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.api.WikipediaApiHelper;
import es.bvalero.replacer.wikipedia.api.WikipediaApiRequest;
import es.bvalero.replacer.wikipedia.api.WikipediaApiResponse;
import es.bvalero.replacer.wikipedia.api.WikipediaApiVerb;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@SuppressWarnings("java:S1192")
@Service
@Profile("!offline")
class WikipediaPageSaveApiRepository implements WikipediaPageSaveRepository {

    // Dependency injection
    private final WikipediaApiHelper wikipediaApiHelper;

    WikipediaPageSaveApiRepository(WikipediaApiHelper wikipediaApiHelper) {
        this.wikipediaApiHelper = wikipediaApiHelper;
    }

    @Override
    public WikipediaPageSaveResult save(WikipediaPageSaveCommand pageSave, AccessToken accessToken)
        throws WikipediaException {
        EditToken editToken = getEditToken(pageSave.getPageKey(), accessToken);
        validateEditTimestamp(pageSave.getPageKey(), editToken.getTimestamp(), pageSave.getQueryTimestamp());

        WikipediaApiRequest apiRequest = WikipediaApiRequest.builder()
            .verb(WikipediaApiVerb.POST)
            .lang(pageSave.getPageKey().getLang())
            .params(buildSavePageContentRequestParams(pageSave, editToken))
            .accessToken(accessToken)
            .build();
        WikipediaApiResponse apiResponse = wikipediaApiHelper.executeApiRequest(apiRequest);
        return extractEditResultFromJson(apiResponse.getEdit());
    }

    private void validateEditTimestamp(
        PageKey pageKey,
        WikipediaTimestamp editTimestamp,
        WikipediaTimestamp queryTimestamp
    ) throws WikipediaConflictException {
        // Pre-check of edit conflicts
        if (queryTimestamp.isBeforeOrEquals(editTimestamp)) {
            String message = String.format(
                "Page edited at the same time: %s - %s - %s",
                pageKey,
                queryTimestamp,
                editTimestamp
            );
            throw new WikipediaConflictException(message);
        }
    }

    private Map<String, String> buildSavePageContentRequestParams(
        WikipediaPageSaveCommand pageSave,
        EditToken editToken
    ) {
        Map<String, String> params = new HashMap<>();
        params.put("action", "edit");
        params.put("pageid", Integer.toString(pageSave.getPageKey().getPageId()));
        params.put("text", pageSave.getContent());
        if (pageSave.getSectionId() != null) {
            params.put("section", Integer.toString(pageSave.getSectionId()));
        }
        params.put("summary", pageSave.getEditSummary());
        params.put("watchlist", "nochange");
        params.put("bot", "true");
        params.put("minor", "true");
        params.put("token", editToken.getCsrfToken());
        // Timestamp when the editing process began
        params.put("starttimestamp", pageSave.getQueryTimestamp().toString());
        // Timestamp of the base revision
        params.put("basetimestamp", editToken.getTimestamp().toString());
        return params;
    }

    @VisibleForTesting
    EditToken getEditToken(PageKey id, AccessToken accessToken) throws WikipediaException {
        WikipediaApiRequest apiRequest = WikipediaApiRequest.builder()
            .verb(WikipediaApiVerb.POST)
            .lang(id.getLang())
            .params(buildEditTokenRequestParams(id.getPageId()))
            .accessToken(accessToken)
            .build();
        WikipediaApiResponse apiResponse = wikipediaApiHelper.executeApiRequest(apiRequest);
        return extractEditTokenFromJson(apiResponse);
    }

    private Map<String, String> buildEditTokenRequestParams(int pageId) {
        Map<String, String> params = new HashMap<>();
        params.put("action", "query");
        params.put("meta", "tokens");
        params.put("pageids", Integer.toString(pageId));
        params.put("prop", "revisions");
        params.put("rvprop", "timestamp");
        return params;
    }

    private EditToken extractEditTokenFromJson(WikipediaApiResponse response) {
        return EditToken.of(
            response.getQuery().getTokens().getCsrftoken(),
            WikipediaTimestamp.of(
                response
                    .getQuery()
                    .getPages()
                    .stream()
                    .findFirst()
                    .orElseThrow()
                    .getRevisions()
                    .stream()
                    .findFirst()
                    .orElseThrow()
                    .getTimestamp()
            )
        );
    }

    private WikipediaPageSaveResult extractEditResultFromJson(WikipediaApiResponse.Edit edit) {
        return WikipediaPageSaveResult.builder()
            .oldRevisionId(edit.getOldrevid())
            .newRevisionId(edit.getNewrevid())
            .newTimestamp(WikipediaTimestamp.of(edit.getNewtimestamp()))
            .build();
    }
}
