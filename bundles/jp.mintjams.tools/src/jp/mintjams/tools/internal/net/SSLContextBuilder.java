/*
 * Copyright (c) 2021 MintJams Inc.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package jp.mintjams.tools.internal.net;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Objects;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

public class SSLContextBuilder {
	private String fProtocol;

	private SSLContextBuilder(String protocol) {
		fProtocol = protocol;
	}

	public static SSLContextBuilder create(String protocol) {
		return new SSLContextBuilder(protocol);
	}

	private KeyManager[] fKeyManagers;
	public SSLContextBuilder setKeyManager(KeyManager...keyManagers) {
		fKeyManagers = keyManagers;
		return this;
	}

	private TrustManager[] fTrustManagers;
	public SSLContextBuilder setTrustManager(TrustManager...trustManagers) {
		fTrustManagers = trustManagers;
		return this;
	}

	private SecureRandom fSecureRandom;
	public SSLContextBuilder setSecureRandom(SecureRandom secureRandom) {
		fSecureRandom = secureRandom;
		return this;
	}

	public SSLContext build() throws NoSuchAlgorithmException, KeyManagementException {
		Objects.requireNonNull(fProtocol);
		SSLContext ctx = SSLContext.getInstance(fProtocol);
		ctx.init(fKeyManagers, fTrustManagers, fSecureRandom);
		return ctx;
	}

}
