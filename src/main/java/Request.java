import java.util.HashMap;
import java.util.Map;

final class Request {
	final String method;
	final String path;
	final String version;
	final Map<String, String> headers;
	final String body;

	public Request(Builder builder) {
		this.method = builder.method;
		this.path = builder.path;
		this.version = builder.version;
		this.headers = builder.headers;
		this.body = builder.body;
	}

	public Request() {
		this.method = "";
		this.path = "";
		this.version = "";
		this.headers = null;
		this.body = "";
	}

	public static class Builder {
		private String method;
		private String path;
		private String version;
		private Map<String, String> headers;
		private String body;

		public Builder() {}

		public static Builder newInstance() {
			return new Builder();
		}

		public Builder setStartLine(String[] startLine) {
			this.method = startLine[0];
			this.path = startLine[1];
			this.version = startLine[2];
			return this;
		}

		public Builder setHeaders(Map<String, String> headers) {
			this.headers = headers;
			return this;
		}

		public Builder setBody(String body) {
			this.body = body;
			return this;
		}

		public Request build() {
			return new Request(this);
		}
	}
}