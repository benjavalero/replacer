package es.bvalero.replacer.wikipedia;

import com.github.scribejava.core.builder.api.DefaultApi10a;

public final class MediaWikiApi extends DefaultApi10a {

    private static final String REQUEST_TOKEN_ENDPOINT =
            "https://meta.wikimedia.org/w/index.php?title=Special:OAuth/initiate";
    private static final String ACCESS_TOKEN_ENDPOINT =
            "https://meta.wikimedia.org/w/index.php?title=Special:OAuth/token";
    private static final String AUTHORIZATION_URL =
            "https://meta.wikimedia.org/wiki/Special:OAuth/authorize";

    private MediaWikiApi() {
    }

    static MediaWikiApi instance() {
        return MediaWikiApi.InstanceHolder.INSTANCE;
    }

    @Override
    public String getRequestTokenEndpoint() {
        return REQUEST_TOKEN_ENDPOINT;
    }

    @Override
    public String getAccessTokenEndpoint() {
        return ACCESS_TOKEN_ENDPOINT;
    }

    @Override
    protected String getAuthorizationBaseUrl() {
        return AUTHORIZATION_URL;
    }

    private static class InstanceHolder {
        private static final MediaWikiApi INSTANCE = new MediaWikiApi();

        private InstanceHolder() {
        }
    }

}
