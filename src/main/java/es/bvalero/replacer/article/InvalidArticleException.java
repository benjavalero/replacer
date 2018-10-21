package es.bvalero.replacer.article;

class InvalidArticleException extends Exception {

    InvalidArticleException(String message) {
        super(message);
    }

    InvalidArticleException(Throwable e) {
        super(e);
    }

}
