package com.ubot.web.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.util.regex.Pattern;

import org.glassfish.jersey.message.internal.ReaderWriter;
import org.glassfish.jersey.server.ContainerException;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.ext.Provider;

// 過濾跨網站指令碼
@Provider
@PreMatching
public class XSSFilter implements ContainerRequestFilter {
	@Override
	public void filter(ContainerRequestContext request) throws IOException {
		request.getHeaders().add("X-XSS-Protection", "1; mode=block");
		if (!request.getMethod().equals("GET")) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			InputStream in = request.getEntityStream();
			String result = null;
			try {
				if (in.available() > 0) {
					ReaderWriter.writeTo(in, out);

					byte[] requestEntity = out.toByteArray();
					if (requestEntity.length == 0) {
						result = "";
					} else {
						result = new String(requestEntity, "UTF-8");
					}

					result = cleanXSS(result);
					requestEntity = result.getBytes();

					request.setEntityStream(new ByteArrayInputStream(requestEntity));

				}
			} catch (IOException ex) {
				throw new ContainerException(ex);
			}
		} else {
			URI uri = request.getUriInfo().getRequestUri();
			String cleanUrl = cleanXSS(URLDecoder.decode(uri.toString(), "UTF-8"));
			request.setRequestUri(URI.create(cleanUrl));
		}
	}

	private String cleanXSS(String content) {
		if (content != null) {
			Pattern scriptPattern = Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE);
			content = scriptPattern.matcher(content).replaceAll("");

			scriptPattern = Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'",
					Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			content = scriptPattern.matcher(content).replaceAll("");

			scriptPattern = Pattern.compile("<script>", Pattern.CASE_INSENSITIVE);
			content = scriptPattern.matcher(content).replaceAll("");

			scriptPattern = Pattern.compile("<script(.*?)>",
					Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			content = scriptPattern.matcher(content).replaceAll("");

			scriptPattern = Pattern.compile("eval\\((.*?)\\)",
					Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			content = scriptPattern.matcher(content).replaceAll("");

			scriptPattern = Pattern.compile("e-xpression\\((.*?)\\)",
					Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			content = scriptPattern.matcher(content).replaceAll("");

			scriptPattern = Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE);
			content = scriptPattern.matcher(content).replaceAll("");

			scriptPattern = Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE);
			content = scriptPattern.matcher(content).replaceAll("");

			scriptPattern = Pattern.compile("onload(.*?)=",
					Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			content = scriptPattern.matcher(content).replaceAll("");
		}
		return content;
	}
}