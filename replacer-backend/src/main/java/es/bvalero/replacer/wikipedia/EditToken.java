package es.bvalero.replacer.wikipedia;

class EditToken {
    private String csrftoken;
    private String timestamp;

    EditToken(String csrftoken, String timestamp) {
        this.csrftoken = csrftoken;
        this.timestamp = timestamp;
    }

    String getCsrftoken() {
        return csrftoken;
    }

    String getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "EditToken{" +
                "csrftoken='" + csrftoken + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
