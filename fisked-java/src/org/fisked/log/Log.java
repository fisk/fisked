package org.fisked.log;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Log {
	public static void println(String string) {
		try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("fisked.log", true)))) {
		    out.println(string);
		}catch (IOException e) {
		    //exception handling left as an exercise for the reader
		}
	}
}
