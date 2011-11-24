package com.rump;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

class RumpClient {
  private final String serverUrl;
	private static DefaultHttpClient httpClient;

	private int pauseCount;

  public RumpClient(String serverUrl) {
    this.serverUrl = serverUrl;
  }

	public void discardBackgroundTasks() {
		pauseCount++;
	}

	public void rump(final RumpInfo myInfo, RumpResultHandler handler) {
		new HttpMethod(handler, myInfo).execute();
	}

	private class HttpMethod extends AsyncTask<String, Integer, List<RumpInfo>> {
		private final RumpResultHandler handler;
		private final int myPauseCount = pauseCount;
		private final RumpInfo myInfo;

		public HttpMethod(RumpResultHandler handler, RumpInfo myInfo) {
			this.handler = handler;
			this.myInfo = myInfo;
		}

		protected List<RumpInfo> post(final String url, final String requestBody) throws IOException {
			Log.v("RUMP Client", "Executing http POST with body: " + requestBody);
			HttpPost httpPost = new HttpPost(url);
			StringEntity entity = new StringEntity(requestBody);
			httpPost.setEntity(entity);
			Type collectionType = new TypeToken<List<RumpInfo>>() {
			}.getType();
			return executeHttpRequest(httpPost, ListExtractor.forClass(RumpInfo.class, collectionType));
		}

		@Override
		protected List<RumpInfo> doInBackground(String... params) {
			try {
				return method();
			} catch (IOException e) {
				Log.v("RUMP client", "http action failed", e);
				throw new RumpCommunicationException(e);
			}
		}

		@Override
		protected void onPostExecute(List<RumpInfo> result) {
			if (pauseCount == myPauseCount) {
				handler.onResponse(result);
			}
		}

		protected List<RumpInfo> method() throws IOException {
			try {
				return post(serverUrl, new Gson().toJson(myInfo));
			} catch (Exception e) {
				return Collections.emptyList();
			}
		};

		private <T> T executeHttpRequest(final HttpUriRequest request, final ValueExtractor<T> extractor) throws ClientProtocolException, IOException {
			return httpClient().execute(request, new ResponseHandler<T>() {
				public T handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
					switch (response.getStatusLine().getStatusCode()) {
					case 404:
						throw new NotFoundException();
					case 200:
						BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
						StringBuilder builder = new StringBuilder();
						String line;
						while ((line = reader.readLine()) != null) {
							builder.append(line);
						}
						String json = builder.toString();
						Log.v("RUMP Client", "Executed http " + request.getMethod() + " request. Got response: " + json);
						return extractor.extract(json);
					default:
						throw new RuntimeException("Unexpected return code :" + response.getStatusLine().getStatusCode());
					}
				}
			});
		}
	}

	static class NotFoundException extends RuntimeException {
	}

	static interface ValueExtractor<T> {
		T extract(String s);
	}

	static class ListExtractor<T> implements ValueExtractor<List<T>> {
		private final Type t;

		private ListExtractor(Type t) {
			this.t = t;
		}

		static <T> ValueExtractor<List<T>> forClass(Class<T> k, Type t) {
			return new ListExtractor<T>(t);
		}

		public List<T> extract(String s) {
			return new Gson().fromJson(s, t);
		}
	}

	static class DefaultExtractor<T> implements ValueExtractor<T> {
		private final Class<T> k;

		private DefaultExtractor(Class<T> k) {
			this.k = k;
		}

		static <T> ValueExtractor<T> forClass(Class<T> k) {
			return new DefaultExtractor<T>(k);
		}

		public T extract(String s) {
			return new Gson().fromJson(s, k);
		}
	}

	private DefaultHttpClient httpClient() {
		if (httpClient == null) {
			HttpParams params = createHttpParams();
			SchemeRegistry registry = createSchemeRegistry();
			ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(params, registry);
			httpClient = new DefaultHttpClient(manager, params);
		}
		return httpClient;
	}

	private SchemeRegistry createSchemeRegistry() {
		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		final SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();
		sslSocketFactory.setHostnameVerifier(SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
		registry.register(new Scheme("https", sslSocketFactory, 443));
		return registry;
	}

	private HttpParams createHttpParams() {
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, "utf-8");
		params.setBooleanParameter("http.protocol.expect-continue", false);
		return params;
	}

	public class RumpCommunicationException extends RuntimeException {
		public RumpCommunicationException(IOException e) {
			super("Communication error with RUMP server.", e);
		}
	}
}
