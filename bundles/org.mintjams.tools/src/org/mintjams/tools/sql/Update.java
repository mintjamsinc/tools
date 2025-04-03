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

package org.mintjams.tools.sql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.mintjams.tools.internal.sql.SQLStatement;

public class Update {

	private final String fStatement;
	private final Map<String, Object> fVariables = new HashMap<>();
	private final Connection fConnection;
	private final ParameterHandler fParameterHandler;

	private Update(Builder builder) {
		fStatement = builder.fStatement;
		fVariables.putAll(builder.fVariables);
		fConnection = builder.fConnection;
		fParameterHandler = builder.fParameterHandler;
	}

	private SQLStatement prepare() {
		return SQLStatement.newBuilder()
				.setSource(fStatement)
				.setVariables(fVariables)
				.setConnection(fConnection)
				.setParameterHandler(fParameterHandler)
				.build();
	}

	public int execute() throws SQLException {
		try (SQLStatement stmt = prepare()) {
			return stmt.prepare().executeUpdate();
		} catch (IOException ex) {
			throw (IllegalStateException) new IllegalStateException(ex.getMessage()).initCause(ex);
		}
	}

	public long executeLarge() throws SQLException {
		try (SQLStatement stmt = prepare()) {
			return stmt.prepare().executeLargeUpdate();
		} catch (IOException ex) {
			throw (IllegalStateException) new IllegalStateException(ex.getMessage()).initCause(ex);
		}
	}

	public static Builder newBuilder(Connection connection) {
		return Builder.create(connection);
	}

	@Deprecated
	public static Builder newBuilder() {
		return Builder.create(null);
	}

	public static class Builder {
		private Connection fConnection;

		private Builder(Connection connection) {
			fConnection = connection;
		}

		public static Builder create(Connection connection) {
			return new Builder(connection);
		}

		private String fStatement;
		public Builder setStatement(String statement) {
			fStatement = statement;
			return this;
		}

		private final Map<String, Object> fVariables = new HashMap<>();
		public Builder setVariables(Map<String, Object> variables) {
			fVariables.putAll(variables);
			return this;
		}
		public Builder setVariable(String key, Object value) {
			fVariables.put(key, value);
			return this;
		}

		@Deprecated
		public Builder setConnection(Connection connection) {
			fConnection = connection;
			return this;
		}

		private ParameterHandler fParameterHandler;
		public Builder setParameterHandler(ParameterHandler parameterHandler) {
			fParameterHandler = parameterHandler;
			return this;
		}

		public Update build() throws SQLException {
			Objects.requireNonNull(fStatement);
			Objects.requireNonNull(fConnection);
			return new Update(this);
		}
	}

}
