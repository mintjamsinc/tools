/*
 * Copyright (c) 2022 MintJams Inc.
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

package jp.mintjams.tools.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SimpleActionChain implements ActionChain {

	private final List<Action> fActions = new ArrayList<>();
	private int fIndex = 0;

	public SimpleActionChain(Action... actions) {
		if (actions != null) {
			fActions.addAll(Arrays.asList(actions));
		}
	}

	public SimpleActionChain(List<Action> actions) {
		this(actions.toArray(Action[]::new));
	}

	public void doAction(ActionContext context) throws ActionException {
		if (fIndex < fActions.size()) {
			Action action = fActions.get(fIndex);
			try {
				fIndex++;
				action.doAction(context, this);
			} finally {
				fIndex--;
			}
		}
	}

}
