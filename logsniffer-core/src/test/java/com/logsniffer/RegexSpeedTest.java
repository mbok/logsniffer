package com.logsniffer;

import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class RegexSpeedTest {
	Pattern p = Pattern.compile("(.+)z(.*)a");
	static String[] data = new String[1000000];
	static long size = 0;

	@BeforeClass
	public static void setUp() {
		for (int i = 0; i < data.length; i++) {
			data[i] = RandomStringUtils.random(50 + i % 200);
			size += data[i].length();
		}
	}

	@Test
	public void testSeq() {
		for (int z = 0; z < 2; z++) {
			final long start = System.currentTimeMillis();
			for (int i = 0; i < data.length; i++) {
				p.matcher(data[i]).matches();
			}
			final long time = System.currentTimeMillis() - start;
			System.out.println("Finished seq " + size + " in " + time + " => " + Math.round(size / time * 1000));
		}
	}

	@Test
	public void testPar() throws InterruptedException {
		for (int z = 0; z < 2; z++) {
			parPos = 0;
			final int parCount = 4;
			final Semaphore s = new Semaphore(parCount);
			final long start = System.currentTimeMillis();
			for (int i = 0; i < parCount; i++) {
				final Parser parser = new Parser(s);
				parser.start();
			}
			s.acquire(parCount);
			final long time = System.currentTimeMillis() - start;
			System.out.println("Finished parallel " + size + " in " + time + " => " + Math.round(size / time * 1000));
		}
	}

	private int parPos = 0;

	private synchronized String nextLine() {
		return parPos < data.length ? data[parPos++] : null;
	}

	public class Parser extends Thread {
		final Semaphore s;

		public Parser(final Semaphore s) throws InterruptedException {
			this.s = s;
			s.acquire(1);
		}

		@Override
		public void run() {
			String line;
			while ((line = nextLine()) != null) {
				p.matcher(line).matches();
			}
			s.release();
		}

	}
}
