package es.bvalero.replacer.wikipedia;

import com.github.scribejava.core.builder.api.DefaultApi10a;

public class MediaWikiApi extends DefaultApi10a {

    private MediaWikiApi() {
    }

    static MediaWikiApi instance() {
        return MediaWikiApi.InstanceHolder.INSTANCE;
    }

    @Override
    public String getRequestTokenEndpoint() {
        return "https://meta.wikimedia.org/w/index.php?title=Special:OAuth/initiate";
    }

    @Override
    public String getAccessTokenEndpoint() {
        return "https://meta.wikimedia.org/w/index.php?title=Special:OAuth/token";
    }

    @Override
    protected String getAuthorizationBaseUrl() {
        return "https://meta.wikimedia.org/wiki/Special:OAuth/authorize";
    }

    private static class InstanceHolder {
        private static final MediaWikiApi INSTANCE = new MediaWikiApi();

        private InstanceHolder() {
        }
    }

}
