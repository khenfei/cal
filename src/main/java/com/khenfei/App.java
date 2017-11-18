package com.khenfei;

import com.khenfei.executer.impl.AppExecuter;

public class App {
	public static void main(String[] args) {
		Console console = new Console(new AppExecuter());
		console.run(args);
	}
}
